/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.imageio;

import java.io.*;
import java.util.LinkedList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import gabien.render.WSIImage;
import r48.tr.pages.TrRoot;

/**
 * 8-bit indexed PNG loader.
 * Created on April 14th 2018.
 */
public class PNG8IImageIOFormat extends ImageIOFormat {
    public PNG8IImageIOFormat(TrRoot tr) {
        super(tr, true);
    }
    
    public static boolean supports(ImageIOImage img) {
        if (img.palette != null)
            return img.palette.size() <= 256;
        return false;
    }

    private static byte[] getChunk(String ihdr, DataInputStream dis) throws IOException {
        int magicWanted = PNGChunk.encodeMagic(ihdr);
        int end = PNGChunk.encodeMagic("IEND");
        while (true) {
            PNGChunk pc = PNGChunk.getChunk(dis);
            if (pc.magic == end)
                return null;
            if (pc.magic == magicWanted)
                return pc.data;
        }
    }

    @Override
    public String saveName(ImageIOImage img) {
        if (supports(img))
            return tr.g.img_png8;
        return null;
    }

    @Override
    public byte[] saveFile(ImageIOImage img) throws IOException {
        if (!supports(img))
            throw new IOException("PNG8I not supported for this image");
        // Oh, this'll be fun. Not.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        int mark; // Used for CRC calc
        dos.write(new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A});

        dos.writeInt(0x0D);
        mark = baos.size();
        dos.write(new byte[] {(byte) 'I', (byte) 'H', (byte) 'D', (byte) 'R'});
        dos.writeInt(img.width);
        dos.writeInt(img.height);
        dos.writeInt(0x08030000);
        dos.write(0);
        performCRC(baos, dos, mark);

        dos.writeInt(img.palette.size() * 3);
        mark = baos.size();
        dos.write(new byte[] {(byte) 'P', (byte) 'L', (byte) 'T', (byte) 'E'});
        for (int i = 0; i < img.palette.size(); i++) {
            int c = img.palette.get(i);
            dos.write((c >> 16) & 0xFF);
            dos.write((c >> 8) & 0xFF);
            dos.write(c & 0xFF);
        }
        performCRC(baos, dos, mark);

        dos.writeInt(img.palette.size());
        mark = baos.size();
        dos.write(new byte[] {(byte) 't', (byte) 'R', (byte) 'N', (byte) 'S'});
        for (int i = 0; i < img.palette.size(); i++) {
            int c = img.palette.get(i);
            dos.write((c >> 24) & 0xFF);
        }
        performCRC(baos, dos, mark);

        // actual compressed data...
        byte[] compressed = compressPNG8IData(img);
        dos.writeInt(compressed.length);
        mark = baos.size();
        dos.write(new byte[] {(byte) 'I', (byte) 'D', (byte) 'A', (byte) 'T'});
        dos.write(compressed);
        performCRC(baos, dos, mark);

        // the end!
        dos.writeInt(0);
        mark = baos.size();
        dos.write(new byte[] {(byte) 'I', (byte) 'E', (byte) 'N', (byte) 'D'});
        if (performCRC(baos, dos, mark) != 0xAE426082)
            throw new IOException("CRC self-test failure!");

        return baos.toByteArray();
    }

    private int performCRC(ByteArrayOutputStream baos, DataOutputStream dos, int mark) throws IOException {
        // Everything's showing up as valid in R48 (because Java ignores the CRCs),
        //  but obviously they have to be calculated correctly to not explode. Grr.
        byte[] fullSet = baos.toByteArray();
        int checksum = 0xFFFFFFFF;
        for (int i = mark; i < fullSet.length; i++) {
            int xv = performCRCSub((checksum ^ fullSet[i]) & 0xFF);
            checksum = (checksum >> 8) & 0x00FFFFFF;
            checksum ^= xv;
        }
        // but *why*?
        checksum = ~checksum;
        dos.writeInt(checksum);
        return checksum;
    }

    private int performCRCSub(int i) {
        for (int j = 0; j < 8; j++) {
            boolean top = (i & 1) != 0;
            i = (i >> 1) & 0x7FFFFFFF;
            if (top)
                i ^= 0xEDB88320;
        }
        return i;
    }

    private byte[] compressPNG8IData(ImageIOImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        for (int i = 0; i < img.height; i++) {
            dos.write(0);
            for (int j = 0; j < img.width; j++)
                dos.write(img.getRaw(j, i));
        }
        dos.finish();
        return baos.toByteArray();
    }

    @Override
    public ImageIOImage loadFile(byte[] data, WSIImage im) throws IOException {
        // PNG8I reader.
        if (data[0] != (byte) 0x89)
            throw new IOException("bad magic byte 0");
        if (data[1] != (byte) 0x50)
            throw new IOException("bad magic byte 1");
        if (data[2] != (byte) 0x4E)
            throw new IOException("bad magic byte 2");
        if (data[3] != (byte) 0x47)
            throw new IOException("bad magic byte 3");
        if (data[4] != (byte) 0x0D)
            throw new IOException("bad magic byte 4");
        if (data[5] != (byte) 0x0A)
            throw new IOException("bad magic byte 5");
        if (data[6] != (byte) 0x1A)
            throw new IOException("bad magic byte 6");
        if (data[7] != (byte) 0x0A)
            throw new IOException("bad magic byte 7");
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        dis.skipBytes(8);
        byte[] hdr = getChunk("IHDR", dis);
        DataInputStream sdis = new DataInputStream(new ByteArrayInputStream(hdr));
        int width = sdis.readInt();
        int height = sdis.readInt();
        if (sdis.read() != 8)
            throw new IOException("Only handles 8-bit PNGs properly, try again later");
        if (sdis.read() != 3)
            throw new IOException("This is just to handle indexed-colour");
        if (sdis.read() != 0)
            throw new IOException("Apparently evil");
        if (sdis.read() != 0)
            throw new IOException("Apparently evil");
        if (sdis.read() != 0)
            throw new IOException("Interlacing...?");
        byte[] plte = getChunk("PLTE", dis);
        // we MIGHT have a tRNS chunk here, but might not, so use the more complicated API
        byte[] trns = null;
        ByteArrayOutputStream concatAll = new ByteArrayOutputStream();
        InflaterOutputStream concatAllIF = new InflaterOutputStream(concatAll);
        int cEnd = PNGChunk.encodeMagic("IEND");
        int cDat = PNGChunk.encodeMagic("IDAT");
        int cTrn = PNGChunk.encodeMagic("tRNS");
        while (true) {
            PNGChunk pc = PNGChunk.getChunk(dis);
            if (pc.magic == cTrn)
                trns = pc.data;
            if (pc.magic == cDat)
                concatAllIF.write(pc.data);
            if (pc.magic == cEnd)
                break;
        }
        concatAllIF.finish();
        byte[] scanlineData = concatAll.toByteArray();
        dis.close();
        return finallyAssembleTheData(width, height, scanlineData, plte, trns);
    }

    private ImageIOImage finallyAssembleTheData(int width, int height, byte[] scanlineData, byte[] plte, byte[] trns) throws IOException {
        int[] data = new int[width * height];
        LinkedList<Integer> pal = new LinkedList<Integer>();
        for (int i = 0; i < plte.length; i += 3) {
            int ti = i / 3;
            int c = 0xFF000000;
            c |= (plte[i] & 0xFF) << 16;
            c |= (plte[i + 1] & 0xFF) << 8;
            c |= plte[i + 2] & 0xFF;
            if (trns != null)
                if (trns.length > ti) {
                    c &= 0x00FFFFFF;
                    c |= (trns[ti] & 0xFF) << 24;
                }
            pal.add(c);
        }
        int idx = 0;
        int oidx = 0;
        for (int i = 0; i < height; i++) {
            int alg = scanlineData[idx++];
            if (alg != 0)
                throw new IOException("Need support for algs");
            for (int j = 0; j < width; j++) {
                data[oidx++] = scanlineData[idx++] & 0xFF;
            }
        }
        return new ImageIOImage(width, height, data, pal);
    }

    private static class PNGChunk {
        public final int magic;
        public final byte[] data;

        public PNGChunk(int m, byte[] d) {
            magic = m;
            data = d;
        }

        public static PNGChunk getChunk(DataInputStream dis) throws IOException {
            int len = dis.readInt();
            int maj = dis.readInt();
            byte[] data = new byte[len];
            if (dis.read(data) != len)
                throw new IOException("did not read all of chunk");
            dis.readInt(); // checksum
            return new PNGChunk(maj, data);
        }

        public static int encodeMagic(String m) {
            try {
                byte[] d = m.getBytes("UTF-8");
                return new DataInputStream(new ByteArrayInputStream(d)).readInt();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }
}

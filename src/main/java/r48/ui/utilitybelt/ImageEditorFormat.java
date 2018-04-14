/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.IImage;
import r48.dbs.TXDB;
import r48.io.PathUtils;
import r48.map.imaging.PNG8IImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

/**
 * Describes an image format (save/load buttons)
 * Created on April 13th 2018
 */
public abstract class ImageEditorFormat {
    // The 'save' button details (or null if no save system exists)
    public abstract String saveName(ImageEditorImage img);

    public abstract byte[] saveFile(ImageEditorImage img) throws IOException;

    // Tries to load the image, or returns null otherwise
    public abstract ImageEditorImage loadFile(String s);

    // The list of supported formats.
    public static final ImageEditorFormat[] supportedFormats = {
            new ImageEditorFormat() {
                @Override
                public String saveName(ImageEditorImage img) {
                    if (img.usesPalette()) {
                        if (img.paletteSize() > 256)
                            return null;
                        return TXDB.get("Save PNG-8I");
                    }
                    return TXDB.get("Save PNG");
                }

                @Override
                public byte[] saveFile(ImageEditorImage img) throws IOException {
                    if (img.usesPalette()) {
                        if (img.paletteSize() > 256)
                            throw new IOException("Impossible to save with > 256 colours!");
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
                        if (performCRC(baos, dos, mark) != 0x44A48AC6)
                            throw new IOException("CRC self-test failure!");

                        dos.writeInt(img.paletteSize() * 3);
                        mark = baos.size();
                        dos.write(new byte[] {(byte) 'P', (byte) 'L', (byte) 'T', (byte) 'E'});
                        for (int i = 0; i < img.paletteSize(); i++) {
                            int c = img.getPaletteRGB(i);
                            dos.write((c >> 16) & 0xFF);
                            dos.write((c >> 8) & 0xFF);
                            dos.write(c & 0xFF);
                        }
                        performCRC(baos, dos, mark);

                        dos.writeInt(img.paletteSize());
                        mark = baos.size();
                        dos.write(new byte[] {(byte) 't', (byte) 'R', (byte) 'N', (byte) 'S'});
                        for (int i = 0; i < img.paletteSize(); i++) {
                            int c = img.getPaletteRGB(i);
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
                    return img.rasterize().createPNG();
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
                            i ^= 0xedb88320;
                    }
                    return i;
                }

                private byte[] compressPNG8IData(ImageEditorImage img) throws IOException {
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
                public ImageEditorImage loadFile(String s) {
                    // PNG8I
                    int[] pal = PNG8IImageLoader.convPal(PNG8IImageLoader.getPalette(s));
                    // gabien general loading mechanism
                    GaBIEn.hintFlushAllTheCaches();
                    IImage im = GaBIEn.getImageEx(PathUtils.autoDetectWindows(s), true, false);
                    if (im == GaBIEn.getErrorImage())
                        return null;
                    boolean indexed = pal != null;
                    int[] pixies = im.getPixels();
                    boolean a1Lock = false;
                    if (indexed) {
                        a1Lock = (pal[0] & 0xFF000000) == 0;
                        for (int j = 1; j < pal.length; j++)
                            if ((pal[1] & 0xFF000000) != 0xFF000000)
                                a1Lock = false;
                        for (int i = 0; i < pixies.length; i++) {
                            int c = pixies[i];
                            pixies[i] = -1;
                            for (int j = 0; j < pal.length; j++) {
                                if (pal[j] == c) {
                                    pixies[i] = j;
                                    break;
                                }
                            }
                        }
                    }
                    ImageEditorImage iei = new ImageEditorImage(im.getWidth(), im.getHeight(), pixies, indexed, a1Lock);
                    if (indexed)
                        iei.changePalette(pal);
                    return iei;
                }
            }
    };
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.cs;

import gabien.GaBIEn;
import r48.RubyTable;
import r48.io.OldObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * This is now the only thing remaining out of the CSOEdit experiment.
 * Created on May 11th 2018.
 */
public class CSObjectBackend extends OldObjectBackend<IRIO, IRIO> {
    public final String pfx;
    public final Charset encoding;

    public CSObjectBackend(String prefix, Charset cs) {
        pfx = prefix;
        encoding = cs;
    }

    @Override
    public IRIOGeneric newObjectO(String nt) {
        return new IRIOGeneric(encoding);
    }

    @Override
    public IRIO loadObjectFromFile(String filename) {
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(pfx + filename));
        if (inp == null) {
            System.err.println("Couldn't load CS " + pfx + filename);
            return null;
        }
        try {
            IRIO res = null;
            String fnl = filename.toLowerCase();
            if (fnl.endsWith("pxm")) {
                res = loadPXM(inp);
            } else if (fnl.endsWith("pxa")) {
                res = loadPXA(inp);
            } else if (fnl.endsWith("stage.tbl")) {
                res = loadStageTBL(inp);
            }
            inp.close();
            return res;
        } catch (IOException ioe) {
            try {
                inp.close();
            } catch (IOException e) {
            }
            ioe.printStackTrace();
        }
        return null;
    }

    private IRIOGeneric loadStageTBL(InputStream inp) throws IOException {
        int stages = inp.available() / 200;
        IRIOGeneric rio = newObjectO("");
        rio.setArray(stages);
        for (int i = 0; i < stages; i++) {
            IRIO tileset = loadFixedFormatString(inp, 0x20);
            IRIO filename = loadFixedFormatString(inp, 0x20);
            int backgroundScroll = inp.read();
            backgroundScroll |= inp.read() << 8;
            backgroundScroll |= inp.read() << 16;
            backgroundScroll |= inp.read() << 24;
            IRIO bkg = loadFixedFormatString(inp, 0x20);
            IRIO npc1 = loadFixedFormatString(inp, 0x20);
            IRIO npc2 = loadFixedFormatString(inp, 0x20);
            int boss = inp.read();
            IRIO name = loadFixedFormatString(inp, 0x23);

            IRIO rio2 = rio.getAElem(i).setObject("Stage");
            rio2.addIVar("@tileset").setDeepClone(tileset);
            rio2.addIVar("@filename").setDeepClone(filename);
            rio2.addIVar("@background_scroll").setFX(backgroundScroll);
            rio2.addIVar("@sf_bkg").setDeepClone(bkg);
            rio2.addIVar("@sf_npc1").setDeepClone(npc1);
            rio2.addIVar("@sf_npc2").setDeepClone(npc2);
            rio2.addIVar("@boss").setFX(boss);
            rio2.addIVar("@name").setDeepClone(name);
        }
        return rio;
    }

    private IRIO loadFixedFormatString(InputStream inp, int i) throws IOException {
        byte[] bt = new byte[i];
        if (inp.read(bt) != i)
            throw new IOException("Insufficient data");
        for (int j = 0; j < bt.length; j++) {
            if (bt[j] == 0) {
                byte[] nbt = new byte[j];
                System.arraycopy(bt, 0, nbt, 0, j);
                bt = nbt;
                break;
            }
        }
        return newObjectO("").setString(bt, encoding);
    }

    private IRIO loadPXA(InputStream inp) throws IOException {
        return loadRT(inp, 16, 16);
    }

    private IRIO loadPXM(InputStream inp) throws IOException {
        if (inp.read() != 'P')
            throw new IOException("Magic PXM 0x10 incorrect");
        if (inp.read() != 'X')
            throw new IOException("Magic PXM 0x10 incorrect");
        if (inp.read() != 'M')
            throw new IOException("Magic PXM 0x10 incorrect");
        if (inp.read() != 0x10)
            throw new IOException("Magic PXM 0x10 incorrect");
        int w = inp.read();
        w |= inp.read() << 8;
        int h = inp.read();
        h |= inp.read() << 8;
        return loadRT(inp, w, h);
    }

    private IRIO loadRT(InputStream inp, int w, int h) throws IOException {
        RubyTable rt = new RubyTable(2, w, h, 1, new int[] {0});
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++)
                rt.setTiletype(i, j, 0, (short) inp.read());
        return newObjectO("").setUser("Table", rt.innerBytes);
    }

    @Override
    public void saveObjectToFile(String filename, IRIO object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fnl = filename.toLowerCase();
        if (fnl.endsWith("pxm")) {
            savePXM(baos, object);
        } else if (fnl.endsWith("pxa")) {
            savePXA(baos, object);
        } else if (fnl.endsWith("stage.tbl")) {
            saveStageTBL(baos, object);
        } else {
            throw new IOException("I don't know how to save that");
        }
        OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(pfx + filename));
        if (os == null)
            throw new IOException("Couldn't open file");
        try {
            baos.writeTo(os);
        } finally {
            os.close();
        }
    }

    private void saveStageTBL(ByteArrayOutputStream baos, IRIO o) throws IOException {
        for (IRIO rio : o.getANewArray()) {
            writeFixedFormatString(baos, rio.getIVar("@tileset"), 0x20);
            writeFixedFormatString(baos, rio.getIVar("@filename"), 0x20);
            int backgroundScroll = (int) o.getIVar("@background_scroll").getFX();
            baos.write(backgroundScroll);
            baos.write(backgroundScroll >> 8);
            baos.write(backgroundScroll >> 16);
            baos.write(backgroundScroll >> 24);
            writeFixedFormatString(baos, rio.getIVar("@sf_bkg"), 0x20);
            writeFixedFormatString(baos, rio.getIVar("@sf_npc1"), 0x20);
            writeFixedFormatString(baos, rio.getIVar("@sf_npc2"), 0x20);
            baos.write((int) rio.getIVar("@boss").getFX());
            writeFixedFormatString(baos, rio.getIVar("@name"), 0x23);
        }
    }

    private void writeFixedFormatString(ByteArrayOutputStream baos, IRIO strsym, int i) throws IOException {
        byte[] bt = new byte[i];
        byte[] nbt = strsym.getBufferInEncoding(encoding);
        System.arraycopy(nbt, 0, bt, 0, Math.min(nbt.length, bt.length - 1));
        baos.write(bt);
    }

    private void savePXA(ByteArrayOutputStream baos, IRIO o) throws IOException {
        RubyTable rt = new RubyTable(o.getBuffer());
        for (int j = 0; j < 16; j++)
            for (int i = 0; i < 16; i++)
                baos.write(rt.getTiletype(i, j, 0));
    }

    private void savePXM(ByteArrayOutputStream baos, IRIO o) throws IOException {
        baos.write('P');
        baos.write('X');
        baos.write('M');
        baos.write(0x10);
        RubyTable rt = new RubyTable(o.getBuffer());
        if (rt.width > 0xFFFF)
            throw new RuntimeException("Width > 0xFFFF!");
        if (rt.height > 0xFFFF)
            throw new RuntimeException("Height > 0xFFFF!");
        baos.write(rt.width & 0xFF);
        baos.write((rt.width >> 8) & 0xFF);
        baos.write(rt.height & 0xFF);
        baos.write((rt.height >> 8) & 0xFF);
        for (int j = 0; j < rt.height; j++)
            for (int i = 0; i < rt.width; i++)
                baos.write(rt.getTiletype(i, j, 0));
    }

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }
}

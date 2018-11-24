/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.cs;

import gabien.GaBIEn;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.OldObjectBackend;
import r48.io.PathUtils;
import r48.io.r2k.R2kUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is now the only thing remaining out of the CSOEdit experiment.
 * Created on May 11th 2018.
 */
public class CSObjectBackend extends OldObjectBackend<RubyIO> {
    public String pfx;

    public CSObjectBackend(String prefix) {
        pfx = prefix;
    }

    @Override
    public RubyIO newObject() {
        return new RubyIO().setNull();
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(pfx + filename));
        if (inp == null) {
            System.err.println("Couldn't load CS " + pfx + filename);
            return null;
        }
        try {
            RubyIO res = null;
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

    private RubyIO loadStageTBL(InputStream inp) throws IOException {
        int stages = inp.available() / 200;
        RubyIO rio = new RubyIO();
        rio.type = '[';
        RubyIO[] stageArray = new RubyIO[stages];
        rio.arrVal = stageArray;
        for (int i = 0; i < stageArray.length; i++) {
            RubyIO tileset = loadFixedFormatString(inp, 0x20);
            RubyIO filename = loadFixedFormatString(inp, 0x20);
            int backgroundScroll = inp.read();
            backgroundScroll |= inp.read() << 8;
            backgroundScroll |= inp.read() << 16;
            backgroundScroll |= inp.read() << 24;
            RubyIO bkg = loadFixedFormatString(inp, 0x20);
            RubyIO npc1 = loadFixedFormatString(inp, 0x20);
            RubyIO npc2 = loadFixedFormatString(inp, 0x20);
            int boss = inp.read();
            RubyIO name = loadFixedFormatString(inp, 0x23);

            RubyIO rio2 = new RubyIO().setSymlike("Stage", true);
            rio2.addIVar("@tileset", tileset);
            rio2.addIVar("@filename", filename);
            rio2.addIVar("@background_scroll", new RubyIO().setFX(backgroundScroll));
            rio2.addIVar("@sf_bkg", bkg);
            rio2.addIVar("@sf_npc1", npc1);
            rio2.addIVar("@sf_npc2", npc2);
            rio2.addIVar("@boss", new RubyIO().setFX(boss));
            rio2.addIVar("@name", name);
            stageArray[i] = rio2;
        }
        return rio;
    }

    private RubyIO loadFixedFormatString(InputStream inp, int i) throws IOException {
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
        return new RubyIO().setString(bt, Factory.encoding);
    }

    private RubyIO loadPXA(InputStream inp) throws IOException {
        return loadRT(inp, 16, 16);
    }

    private RubyIO loadPXM(InputStream inp) throws IOException {
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

    private RubyIO loadRT(InputStream inp, int w, int h) throws IOException {
        RubyTable rt = new RubyTable(2, w, h, 1, new int[] {0});
        for (int j = 0; j < h; j++)
            for (int i = 0; i < w; i++)
                rt.setTiletype(i, j, 0, (short) inp.read());
        return new RubyIO().setUser("Table", rt.innerBytes);
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {
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

    private void saveStageTBL(ByteArrayOutputStream baos, RubyIO o) throws IOException {
        for (RubyIO rio : o.arrVal) {
            writeFixedFormatString(baos, rio.getInstVarBySymbol("@tileset"), 0x20);
            writeFixedFormatString(baos, rio.getInstVarBySymbol("@filename"), 0x20);
            int backgroundScroll = (int) o.getInstVarBySymbol("@background_scroll").fixnumVal;
            baos.write(backgroundScroll);
            baos.write(backgroundScroll >> 8);
            baos.write(backgroundScroll >> 16);
            baos.write(backgroundScroll >> 24);
            writeFixedFormatString(baos, rio.getInstVarBySymbol("@sf_bkg"), 0x20);
            writeFixedFormatString(baos, rio.getInstVarBySymbol("@sf_npc1"), 0x20);
            writeFixedFormatString(baos, rio.getInstVarBySymbol("@sf_npc2"), 0x20);
            baos.write((int) rio.getInstVarBySymbol("@boss").fixnumVal);
            writeFixedFormatString(baos, rio.getInstVarBySymbol("@name"), 0x23);
        }
    }

    private void writeFixedFormatString(ByteArrayOutputStream baos, RubyIO strsym, int i) throws IOException {
        byte[] bt = new byte[i];
        byte[] nbt = R2kUtil.encodeLcfString(strsym.decString());
        System.arraycopy(nbt, 0, bt, 0, Math.min(nbt.length, bt.length - 1));
        baos.write(bt);
    }

    private void savePXA(ByteArrayOutputStream baos, RubyIO o) throws IOException {
        RubyTable rt = new RubyTable(o.userVal);
        for (int j = 0; j < 16; j++)
            for (int i = 0; i < 16; i++)
                baos.write(rt.getTiletype(i, j, 0));
    }

    private void savePXM(ByteArrayOutputStream baos, RubyIO o) throws IOException {
        baos.write('P');
        baos.write('X');
        baos.write('M');
        baos.write(0x10);
        RubyTable rt = new RubyTable(o.userVal);
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

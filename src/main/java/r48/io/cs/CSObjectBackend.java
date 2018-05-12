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
import r48.io.IObjectBackend;
import r48.io.PathUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Secretive juggling occurs to present the CSO backend out of this.
 * Created on May 11th 2018.
 */
public class CSObjectBackend implements IObjectBackend {
    public String pfx;

    public CSObjectBackend(String prefix) {
        pfx = prefix;
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(pfx + filename));
        if (inp == null) {
            System.err.println("Couldn't load CSO " + pfx + filename);
            return null;
        }
        try {
            RubyIO res = null;
            if (filename.endsWith("pxm")) {
                res = loadPXM(inp);
            } else if (filename.endsWith("pxa")) {
                res = loadPXA(inp);
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
        if (filename.endsWith("pxm")) {
            savePXM(baos, object);
        } else if (filename.endsWith("pxa")) {
            savePXA(baos, object);
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
        for (int j = 0; j < rt.width; j++)
            for (int i = 0; i < rt.height; i++)
                baos.write(rt.getTiletype(i, j, 0));
    }

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }
}

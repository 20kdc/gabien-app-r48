/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.cs;

import gabien.GaBIEn;
import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.io.JsonObjectBackend;
import r48.io.PathUtils;

import java.io.*;

/**
 * Performs juggling of CSObjectBackend data and adds support for the .psp format.
 * Created on May 11th 2018.
 */
public class CSOObjectBackend implements IObjectBackend {
    public CSObjectBackend cob;
    public JsonObjectBackend job;

    public CSOObjectBackend(String prefix) {
        cob = new CSObjectBackend(prefix);
        job = new JsonObjectBackend(prefix, "");
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        CSOParsedOP parsed = parseObjectName(filename);
        if (parsed == null)
            return null;
        if (parsed.subtype == CSOSubtype.MTD) {
            return job.loadObjectFromFile(parsed.fileName);
        } else if (parsed.subtype == CSOSubtype.ENT) {
            return job.loadObjectFromFile(parsed.fileName);
        } else if (parsed.subtype == CSOSubtype.PSP) {
            return loadPSPFromFile(parsed.fileName);
        } else if (parsed.subtype == CSOSubtype.Main) {
            RubyIO pxa = cob.loadObjectFromFile(parsed.fileName + ".pxa");
            if (pxa == null)
                return null;
            RubyIO pxm = cob.loadObjectFromFile(parsed.fileName + ".pxm");
            if (pxm == null)
                return null;
            RubyIO map = new RubyIO();
            map.setSymlike("CSOMap", true);
            map.addIVar("@pxa", pxa);
            map.addIVar("@pxm", pxm);
            return map;
        } else {
            return null;
        }
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {
        CSOParsedOP parsed = parseObjectName(filename);
        if (parsed == null)
            throw new IOException("Invalid object name");
        if (parsed.subtype == CSOSubtype.MTD) {
            job.saveObjectToFile(parsed.fileName, object);
        } else if (parsed.subtype == CSOSubtype.ENT) {
            job.saveObjectToFile(parsed.fileName, object);
        } else if (parsed.subtype == CSOSubtype.PSP) {
            savePSPToFile(parsed.fileName, object);
        } else if (parsed.subtype == CSOSubtype.Main) {
            cob.saveObjectToFile(parsed.fileName + ".pxa", object.getInstVarBySymbol("@pxa"));
            cob.saveObjectToFile(parsed.fileName + ".pxm", object.getInstVarBySymbol("@pxm"));
        } else {
            throw new IOException("Cannot handle this subtype: " + parsed.subtype);
        }
    }

    private RubyIO loadPSPFromFile(String s) {
        InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(cob.pfx + s));
        if (inp == null) {
            System.err.println("Couldn't load PSP " + cob.pfx + s);
            return null;
        }
        try {
            InputStreamReader isr = new InputStreamReader(inp, "UTF-8");
            StringWriter sw = new StringWriter();
            char[] buf = new char[2048];
            while (isr.ready())
                sw.write(buf, 0, isr.read(buf));
            inp.close();
            return parsePSP(sw.toString());
        } catch (IOException ioe) {
            try {
                inp.close();
            } catch (IOException e) {
            }
            ioe.printStackTrace();
        }
        return null;
    }

    private RubyIO parsePSP(String s) throws IOException {
        RubyIO rio = new RubyIO();
        rio.setHash();
        s = s.trim();
        if (s.equals(""))
            return rio;
        String[] st = s.split(";");
        for (int i = 0; i < st.length; i++) {
            RubyIO elem = new RubyIO();
            elem.setSymlike("SPEvent", true);
            String[] stp = st[i].trim().split(":");
            if (stp.length != 3)
                throw new IOException("Unexpected param count...");
            try {
                elem.addIVar("@x", new RubyIO().setFX(Long.parseLong(stp[0].trim())));
                elem.addIVar("@y", new RubyIO().setFX(Long.parseLong(stp[1].trim())));
                elem.addIVar("@type", new RubyIO().setFX(Long.parseLong(stp[2].trim())));
            } catch (NumberFormatException nfe) {
                throw new IOException(nfe);
            }
            rio.hashVal.put(new RubyIO().setFX(i), elem);
        }
        return rio;
    }

    private void savePSPToFile(String s, RubyIO instVarBySymbol) throws IOException {
        OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(cob.pfx + s));
        try {
            if (os == null)
                throw new IOException("Couldn't open PSP file");
            boolean first = true;
            for (RubyIO rio : instVarBySymbol.hashVal.values()) {
                if (!first)
                    os.write(';');
                first = false;
                writeNum(os, rio.getInstVarBySymbol("@x").fixnumVal);
                os.write(':');
                writeNum(os, rio.getInstVarBySymbol("@y").fixnumVal);
                os.write(':');
                writeNum(os, rio.getInstVarBySymbol("@type").fixnumVal);
            }
        } finally {
            os.close();
        }
    }

    private void writeNum(OutputStream os, long v) throws IOException {
        // Don't try this at home.
        for (char c : Long.toString(v).toCharArray())
            os.write(c & 0xFF);
    }

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }

    public static CSOParsedOP parseObjectName(String s) {
        CSOSubtype cst = CSOSubtype.Main;
        String g;
        String m;
        if (s.startsWith("mtd:")) {
            cst = CSOSubtype.MTD;
            s = s.substring(4);
        } else if (s.startsWith("psp:")) {
            cst = CSOSubtype.PSP;
            s = s.substring(4);
        } else if (s.startsWith("ent:")) {
            cst = CSOSubtype.ENT;
            s = s.substring(4);
        }
        if (s.contains("\\") || s.contains(":"))
            return null;
        String[] subs = s.split("/");
        if (subs.length != 2)
            return null;
        g = subs[0];
        m = subs[1];
        return new CSOParsedOP(g, m, cst);
    }

    public static class CSOParsedOP {
        public final String gameMode, mapName, fileName;
        public CSOSubtype subtype;

        public CSOParsedOP(String g, String m, CSOSubtype st) {
            gameMode = g;
            mapName = m;
            String pfx = "";
            if (st == CSOSubtype.MTD)
                pfx += ".mtd";
            if (st == CSOSubtype.PSP)
                pfx += ".psp";
            if (st == CSOSubtype.ENT)
                pfx += ".ent";
            fileName = g + "/" + m + "/" + m + pfx;
            subtype = st;
        }

        public CSOParsedOP asSubtype(CSOSubtype st) {
            return new CSOParsedOP(gameMode, mapName, st);
        }

        @Override
        public String toString() {
            String typ = "";
            if (subtype == CSOSubtype.MTD)
                typ = "mtd:";
            if (subtype == CSOSubtype.PSP)
                typ = "psp:";
            if (subtype == CSOSubtype.ENT)
                typ = "ent:";
            return typ + gameMode + "/" + mapName;
        }
    }

    public enum CSOSubtype {
        Main,
        MTD,
        PSP,
        ENT
    }
}

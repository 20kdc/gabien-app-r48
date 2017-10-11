/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.GaBIEn;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created on August 14th, 2017
 */
public class LuaInterface {
    public static RubyIO runLuaCall(RubyIO input, String code) {
        try {
            OutputStream ohs = GaBIEn.getOutFile("templuah.r48");
            InputStream ihs = GaBIEn.getFile("luahead.lua");
            byte[] databuf = new byte[ihs.available()];
            if (ihs.read(databuf) != databuf.length) {
                ohs.close();
                System.err.println("oh dear");
                cleanup();
                return null;
            }
            ohs.write(databuf);
            ohs.close();
            OutputStream os = GaBIEn.getOutFile("templuac.r48");
            PrintStream ps = new PrintStream(os, false, "UTF-8");
            ps.print(code);
            ps.close();
            AdHocSaveLoad.saveLua("templuat", input);
            System.err.println("Running Lua:");
            Process babysit = Runtime.getRuntime().exec("lua templuah.r48");
            double now = GaBIEn.getTime();
            while (true) {
                if (GaBIEn.getTime() > now + 10) {
                    System.err.println("Giving up :(");
                    try {
                        babysit.destroy();
                    } catch (Exception e) {
                    }
                    return null;
                }
                boolean alive = true;
                try {
                    babysit.exitValue();
                    alive = false;
                } catch (IllegalThreadStateException itse) {
                }
                InputStream out2 = babysit.getInputStream();
                while (out2.available() > 0)
                    System.err.write(out2.read());
                InputStream out1 = babysit.getErrorStream();
                while (out1.available() > 0)
                    System.err.write(out1.read());
                if (!alive)
                    break;
            }
            RubyIO res = AdHocSaveLoad.load("templuao");
            cleanup();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            cleanup();
            return null;
        }
    }

    private static void cleanup() {
        new File("templuah.r48").delete();
        new File("templuac.r48").delete();
        new File("templuat.r48").delete();
        new File("templuao.r48").delete();
    }

    public static boolean luaAvailable() {
        RubyIO testObject = new RubyIO();
        testObject.setSymlike("Test::Object", true);
        testObject.addIVar("@hash", new RubyIO().setHash());
        RubyIO a = new RubyIO();
        a.type = '[';
        a.arrVal = new RubyIO[512];
        for (int i = 0; i < 256; i++) {
            a.arrVal[i] = new RubyIO().setFX(i);
            a.arrVal[i + 256] = new RubyIO().setFX(-i);
        }
        testObject.addIVar("@bigint1", new RubyIO().setFX(0xFFFFFFFFL));
        testObject.addIVar("@bigint2", new RubyIO().setFX(-0xFFFFFFFFL));
        testObject.addIVar("@array", a);
        testObject.addIVar("@string", new RubyIO().setString("string"));
        RubyIO special1 = new RubyIO().setString("string2");
        special1.addIVar("@iv", new RubyIO().setString("Test"));
        testObject.addIVar("@stringWIV", special1);
        RubyIO rio = runLuaCall(testObject, "local object = ... return object");
        // need to test this better
        return rio != null;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.GaBIEn;
import gabienapp.Application;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * NOTE: This class, like most of R48, is not thread-safe.
 * This class is *particularly* not thread-safe because it uses a bunch of temp files to do it's job.
 * Created on August 14th, 2017
 */
public class LuaInterface {
    // Error details, if known, from Lua
    public static byte[] lastError = null;
    public static RubyIO runLuaCall(RubyIO input, String code) {
        lastError = null;
        try {
            OutputStream ohs = GaBIEn.getOutFile("templuah.r48");
            InputStream ihs = GaBIEn.getResource("luahead.lua");
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
            ByteArrayOutputStream lastErr = new ByteArrayOutputStream();
            while (true) {
                if (GaBIEn.getTime() > now + 10) {
                    System.err.println("Giving up :(");
                    try {
                        babysit.destroy();
                    } catch (Exception e) {
                    }
                    lastError = lastErr.toByteArray();
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
                    lastErr.write(out2.read());
                InputStream out1 = babysit.getErrorStream();
                while (out1.available() > 0)
                    lastErr.write(out1.read());
                if (!alive)
                    break;
            }
            RubyIO res = AdHocSaveLoad.load("templuao");
            cleanup();
            lastError = lastErr.toByteArray();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            cleanup();
            return null;
        }
    }

    private static void cleanup() {
        GaBIEn.rmFile("templuah.r48");
        GaBIEn.rmFile("templuac.r48");
        GaBIEn.rmFile(Application.BRAND + "/templuat.r48");
        GaBIEn.rmFile(Application.BRAND + "/templuao.r48");
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
        testObject.addIVar("@string", new RubyIO().setString("string", true));
        RubyIO special1 = new RubyIO().setString("string2", true);
        special1.addIVar("@iv", new RubyIO().setString("Test", true));
        testObject.addIVar("@stringWIV", special1);
        RubyIO rio = runLuaCall(testObject, "local object = ... return object");
        // need to test this better
        return rio != null;
    }
}

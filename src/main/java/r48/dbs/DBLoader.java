/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.GaBIEn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Handles the basic database syntax.
 * Created on 12/30/16.
 */
public class DBLoader {
    public static void readFile(String s, IDatabase db) {
        System.out.println(">>" + s + " as " + db);
        try {
            readFile(GaBIEn.getFile(s), db);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("<<" + s);
    }

    public static void readFile(InputStream helpStream, IDatabase db) {
        try {
            InputStreamReader fr = new InputStreamReader(helpStream, "UTF-8");
            new DBLoader(new BufferedReader(fr), db);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private DBLoader(BufferedReader br, IDatabase db) throws IOException {
        int ln = 1;
        try {
            while (br.ready()) {
                String l = br.readLine();
                if (l.length() > 0) {
                    char cmd = l.charAt(0);
                    String[] ll = l.substring(1).trim().split(" ");
                    if (cmd >= '0')
                        if (cmd <= '9') {
                            int a = l.indexOf(':');
                            if (a == -1)
                                throw new RuntimeException("Bad DB entry");
                            db.newObj(Integer.parseInt(l.substring(0, a)), l.substring(a + 1).trim());
                            continue;
                        }
                    db.execCmd(cmd, ll);
                }
                ln++;
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("at line " + ln, re);
        }
    }
}

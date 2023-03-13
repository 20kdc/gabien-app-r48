/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import r48.app.AppCore;
import r48.io.JsonStringIO;

import java.io.*;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Handles the basic database syntax.
 * Created on 12/30/16.
 */
public class DBLoader {

    public static void readFile(@Nullable AppCore app, @NonNull String s, @NonNull IDatabase db) {
        System.out.println(">>" + s + " as " + db);
        if (app != null)
            app.loadProgress.accept(app.t.g.loadingProgress.r(s));
        try {
            readFile(s, GaBIEn.getResource(s), db);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("<<" + s);
    }

    public static void readFile(String fn, InputStream helpStream, IDatabase db) {
        try {
            InputStreamReader fr = new InputStreamReader(helpStream, "UTF-8");
            new DBLoader(fn, new BufferedReader(fr), db);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private DBLoader(String fn, BufferedReader br, IDatabase db) throws IOException {
        int ln = 1;
        try {
            while (br.ready()) {
                db.updateSrcLoc(new DatumSrcLoc(fn, ln));
                String l = br.readLine();
                if (l.length() > 0) {
                    char cmd = l.charAt(0);
                    if (cmd != ' ') {
                        String[] ll = tokenize(new StringReader(l.substring(1).trim()));
                        if (cmd >= '0' && cmd <= '9') {
                            int a = l.indexOf(':');
                            if (a == -1)
                                throw new RuntimeException("Bad DB entry");
                            db.newObj(Integer.parseInt(l.substring(0, a)), l.substring(a + 1).trim());
                        } else {
                            db.execCmd(cmd, ll);
                        }
                    }
                }
                ln++;
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("at line " + ln, re);
        }
    }

    private String[] tokenize(Reader trim) {
        try {
            LinkedList<String> lls = new LinkedList<String>();
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = trim.read();
                if (ch < 0)
                    break;
                if (ch == ' ') {
                    if (sb.length() > 0) {
                        lls.add(sb.toString());
                        sb = new StringBuilder();
                    }
                } else if ((sb.length() == 0) && (ch == '"')) {
                    //System.err.println("Used SDB1.1 String");
                    String tx = JsonStringIO.readString(trim);
                    // Was used to port stuff to SDB1.1
                    //if (!tx.equals("\""))
                    //    throw new RuntimeException("Detected bad SDB1.1 string");
                    lls.add(tx);
                } else {
                    sb.append((char) ch);
                }
            }
            if (sb.length() > 0)
                lls.add(sb.toString());
            return lls.toArray(new String[0]);
        } catch (IOException ioe) {
            // only used with strings
            throw new RuntimeException(ioe);
        }
    }
}

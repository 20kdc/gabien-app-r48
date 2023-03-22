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
        if (DatumLoader.reportLoadSE)
            System.out.println(">>" + s + " as " + db);
        if (app != null)
            app.loadProgress.accept(app.t.g.loadingProgress.r(s));
        try {
            readFile(s, GaBIEn.getResource(s), db);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (DatumLoader.reportLoadSE)
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

    private DBLoader(String fn, Reader rRaw, IDatabase db) throws IOException {
        LineNumberTrackingReader r = new LineNumberTrackingReader(rRaw);
        try {
            while (true) {
                db.updateSrcLoc(new DatumSrcLoc(fn, r.lineNumber));
                int firstChar = r.read();
                if (firstChar == -1) {
                    break;
                } else if (firstChar <= 32) {
                    // comments/blank lines
                    while (firstChar != -1 && firstChar != 10)
                        firstChar = r.read();
                    continue;
                }
                if (firstChar >= '0' && firstChar <= '9') {
                    StringBuilder line = new StringBuilder();
                    line.append((char) firstChar);
                    while (true) {
                        int chr = r.read();
                        if (chr == 10 || chr == -1)
                            break;
                        line.append((char) chr);
                    }
                    String l = line.toString();
                    int a = l.indexOf(':');
                    if (a == -1)
                        throw new RuntimeException("Bad DB entry");
                    db.newObj(Integer.parseInt(l.substring(0, a)), l.substring(a + 1).trim());
                } else {
                    String[] ll = tokenize(r);
                    db.execCmd((char) firstChar, ll);
                }
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("at " + fn + " line " + r.lineNumber, re);
        }
    }

    private String[] tokenize(Reader trim) {
        try {
            LinkedList<String> lls = new LinkedList<String>();
            StringBuilder sb = new StringBuilder();
            boolean blockQuoteFlag = false;
            while (true) {
                int ch = trim.read();
                if (ch < 0) {
                    break;
                } else if (ch == '`') {
                    blockQuoteFlag = !blockQuoteFlag;
                } else if (blockQuoteFlag) {
                    sb.append((char) ch);
                } else if (ch == 10) {
                    break;
                } else if (ch <= 32) {
                    if (sb.length() > 0) {
                        lls.add(sb.toString());
                        sb = new StringBuilder();
                    }
                } else if ((sb.length() == 0) && (ch == '"')) {
                    lls.add(JsonStringIO.readString(trim));
                } else {
                    sb.append((char) ch);
                }
            }
            if (blockQuoteFlag)
                throw new RuntimeException("unfinished block quote");
            if (sb.length() > 0)
                lls.add(sb.toString());
            return lls.toArray(new String[0]);
        } catch (IOException ioe) {
            // only used with strings
            throw new RuntimeException(ioe);
        }
    }

    private static class LineNumberTrackingReader extends Reader {
        public final Reader reader;
        public int lineNumber = 1;
        public LineNumberTrackingReader(Reader r) {
            reader = r;
        }

        @Override
        public int read() throws IOException {
            int v = reader.read();
            if (v == 10)
                lineNumber++;
            return v;
        }

        @Override
        public int read(char[] var1, int ofs, int len) throws IOException {
            int res = reader.read(var1, ofs, len);
            if (res > 0)
                for (int i = 0; i < len; i++)
                    if (var1[ofs + i] == 10)
                        lineNumber++;
            return res;
        }

        @Override
        public void close() throws IOException {
            // shouldn't be the task of this code
        }
    }
}

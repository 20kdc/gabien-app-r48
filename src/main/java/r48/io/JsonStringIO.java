/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Utilties to parse and write out JSON strings.
 * Created on February 19th, 2018.
 */
public class JsonStringIO {
    public static String readString(Reader r) throws IOException {
        // String
        StringBuilder s = new StringBuilder();
        int c = r.read();
        while (c != -1) {
            if (c == '"')
                break;
            if (c == '\\') {
                c = r.read();
                if (c == -1)
                    break;
                if (c == 'u') {
                    int v = 0;
                    for (int i = 0; i < 4; i++) {
                        c = r.read();
                        v = (v << 4) | handleHexDig(c);
                        if (c == -1)
                            break;
                    }
                    s.append((char) v);
                } else if (c == '"') {
                    s.append('\"');
                } else if (c == '\\') {
                    s.append('\\');
                } else if (c == '/') {
                    s.append('/');
                } else if (c == 'b') {
                    s.append('\b');
                } else if (c == 'f') {
                    s.append('\f');
                } else if (c == 'n') {
                    s.append('\n');
                } else if (c == 'r') {
                    s.append('\r');
                } else if (c == 't') {
                    s.append('\t');
                } else {
                    throw new IOException("Unknown escape " + c);
                }
            } else {
                s.append((char) c);
            }
            c = r.read();
        }
        if (c == -1)
            throw new IOException("String terminated too early");
        return s.toString();
    }

    private static int handleHexDig(int c) throws IOException {
        if (c == '0')
            return 0;
        if (c == '1')
            return 1;
        if (c == '2')
            return 2;
        if (c == '3')
            return 3;
        if (c == '4')
            return 4;
        if (c == '5')
            return 5;
        if (c == '6')
            return 6;
        if (c == '7')
            return 7;
        if (c == '8')
            return 8;
        if (c == '9')
            return 9;
        if ((c == 'A') || (c == 'a'))
            return 10;
        if ((c == 'B') || (c == 'b'))
            return 11;
        if ((c == 'C') || (c == 'c'))
            return 12;
        if ((c == 'D') || (c == 'd'))
            return 13;
        if ((c == 'E') || (c == 'e'))
            return 14;
        if ((c == 'F') || (c == 'f'))
            return 15;
        throw new IOException("Unknown hex char");
    }

    // Escapes the string in such a way that writeBytes & getBytes should always give correct output.
    public static String getStringAsASCII(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('\"');
        for (char c : s.toCharArray()) {
            // can't let any messups happen
            if ((c < 32) || (c > 126)) {
                String pad4 = Integer.toHexString(c);
                while (pad4.length() < 4)
                    pad4 = "0" + pad4;
                sb.append("\\u" + pad4);
            } else if ((c == '\"') || (c == '\\')) {
                sb.append('\\');
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append('\"');
        return sb.toString();
    }
}

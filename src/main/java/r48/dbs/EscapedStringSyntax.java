/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

/**
 * For escaping strings.
 * The syntax is as follows.
 * Only \ makes any difference.
 * In this case, an additional character decides what kind of escape this is (and the \ isn't outputted):
 * <p>
 * _: Just writes ' '
 * <p>
 * \: Just writes '\'. You know how this works.
 * <p>
 * ?: Just writes '?' - this is for use in "optionals"
 * <p>
 * 1234567890-=
 * maps to
 * !"£$%^&*()_+
 * <p>
 * finally, {} map to [] (because PathSyntax)
 * <p>
 * Note that translators get the post-unescape version.
 * <p>
 * October 9th, 2017.
 */
public class EscapedStringSyntax {
    // Used as a debugging help.
    private final static boolean nonInterference = false;

    public static String unescape(String s) {
        String r = "";
        char[] sText = s.toCharArray();
        int state = 0;
        for (int i = 0; i < sText.length; i++) {
            char c = sText[i];
            switch (state) {
                case 0:
                    if (c == '\\') {
                        if (nonInterference)
                            throw new RuntimeException("Escape in " + s);
                        state = 1;
                    } else {
                        r += c;
                    }
                    break;
                case 1:
                    state = 0;
                    switch (c) {
                        case '_':
                            r += " ";
                            break;
                        case '\"':
                            r += "\\";
                            break;
                        case '1':
                            r += "!";
                            break;
                        case '2':
                            r += "\"";
                            break;
                        case '3':
                            r += "£";
                            break;
                        case '4':
                            r += "$";
                            break;
                        case '5':
                            r += "%";
                            break;
                        case '6':
                            r += "^";
                            break;
                        case '7':
                            r += "&";
                            break;
                        case '8':
                            r += "*";
                            break;
                        case '9':
                            r += "(";
                            break;
                        case '0':
                            r += ")";
                            break;
                        case '-':
                            r += "_";
                            break;
                        case '+':
                            r += "+";
                            break;
                        case '{':
                            r += "[";
                            break;
                        case '}':
                            r += "]";
                            break;
                        default:
                            throw new RuntimeException("Unknown escape " + c);
                    }
                    break;
            }
        }
        return r;
    }
}

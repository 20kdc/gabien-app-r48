/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

/**
 * For escaping strings.
 * The syntax is as follows.
 * Almost every character goes through unmodified unless a \ shows up.
 * In this case, an additional character decides what kind of escape this is (and the \ isn't outputted).
 * \: Just writes '\'. You know how this works.
 *
 * 1234567890-=:
 * !"£$%^&*()_+
 *
 * Note that translators get the post-unescape version.
 *
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
                        default:
                            throw new RuntimeException("Unknown escape " + c);
                    }
                    break;
            }
        }
        return r;
    }
}

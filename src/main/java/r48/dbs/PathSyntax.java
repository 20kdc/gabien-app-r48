/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import r48.RubyIO;
import r48.io.data.IRIO;

/**
 * NOTE: This uses escapes internally to escape from itself.
 * With that in mind, do not escape this w/EscapedStringSyntax. It's not necessary.
 * Created on 08/06/17.
 */
public class PathSyntax {
    // NOTE: This must not contain anything used in ValueSyntax.
    public static char[] breakersSDB2 = new char[] {':', '@', ']'};

    // break to next token.
    public static String[] breakToken(String full) {
        int plannedIdx = full.length();
        StringBuilder sb = new StringBuilder();
        char[] ch = full.toCharArray();
        boolean escape = false;
        for (int i = 0; i < ch.length; i++) {
            if (escape) {
                escape = false;
                sb.append(ch[i]);
            } else if (ch[i] == '#') {
                escape = true;
            } else {
                for (char c : breakersSDB2) {
                    if (c == ch[i]) {
                        plannedIdx = i;
                        break;
                    }
                }
                if (plannedIdx != full.length())
                    break;
                sb.append(ch[i]);
            }
        }
        if (plannedIdx == full.length()) {
            return new String[] {
                    sb.toString(),
                    ""
            };
        }
        return new String[] {
                sb.toString(),
                full.substring(plannedIdx)
        };
    }

    // Used for missing IV autodetect
    public static String getAbsoluteIVar(String iv) {
        if (iv.startsWith("@")) {
            String n = iv.substring(1);
            String[] ivb = breakToken(n);
            if (ivb[1].equals(""))
                return "@" + ivb[0];
        }
        if (iv.startsWith(":.")) {
            String n = iv.substring(2);
            String[] ivb = breakToken(n);
            if (ivb[1].equals(""))
                return ivb[0];
        }
        return null;
    }

    public static IRIO parse(IRIO res, String arg) {
        return parse(res, arg, 0);
    }

    // Parses the syntax.
    // mode 0: GET
    // mode 1: GET/ADD
    // mode 2: GET/DEL
    // Note that you detect creation by using a GET beforehand.
    public static IRIO parse(IRIO res, String arg, int mode) {
        String workingArg = arg;
        while (workingArg.length() > 0) {
            char f = workingArg.charAt(0);
            workingArg = workingArg.substring(1);
            String[] subcomA = breakToken(workingArg);
            String subcom = subcomA[0];
            workingArg = subcomA[1];
            boolean specialImmediate = (mode != 0) & (workingArg.length() == 0);
            if (f == ':') {
                if (subcom.startsWith("{")) {
                    String esc = subcom.substring(1);
                    IRIO hashVal = ValueSyntax.decode(esc);
                    IRIO root = res;
                    res = res.getHashVal(hashVal);
                    if (specialImmediate) {
                        if (mode == 1) {
                            if (res == null)
                                res = root.addHashVal(hashVal).setNull();
                        } else if (mode == 2) {
                            root.removeHashVal(hashVal);
                        }
                    }
                } else if (subcom.startsWith(".")) {
                    res = mapIV(res, subcom.substring(1), specialImmediate, mode);
                } else {
                    if (specialImmediate)
                        if (mode == 2)
                            throw new RuntimeException("Cannot delete this. Fix your schema.");
                    if (subcom.equals("length")) {
                        // This is used for length disambiguation.
                        if (res.getType() != '[') {
                            res = null;
                        } else {
                            res = new RubyIO().setFX(res.getALen());
                        }
                    } else if (subcom.equals("fail")) {
                        return null;
                    } else if (subcom.length() != 0) {
                        throw new RuntimeException("$-command must be '$' (self), '${\"someSFormatTextForHVal' (hash string), '${123' (hash number), '$:someIval' ('raw' iVar), '$length', '$fail'");
                    }
                }
            } else if (f == '@') {
                res = mapIV(res, "@" + subcom, specialImmediate, mode);
            } else if (f == ']') {
                if (specialImmediate)
                    if (mode == 2)
                        throw new RuntimeException("Cannot delete this. Fix your schema.");
                int atl = Integer.parseInt(subcom);
                if (atl < 0) {
                    res = null;
                    break;
                } else if (atl >= res.getALen()) {
                    res = null;
                    break;
                }
                res = res.getAElem(atl);
            } else {
                throw new RuntimeException("Bad pathsynt starter " + f + " (did root get separated properly?) code " + arg);
            }
            if (res == null)
                return null;
        }
        return res;
    }

    private static IRIO mapIV(IRIO res, String myst, boolean specialImmediate, int mode) {
        IRIO root = res;
        res = res.getIVar(myst);
        if (specialImmediate) {
            if (mode == 1) {
                if (res == null) {
                    res = root.addIVar(myst);
                    if (res != null) {
                        res.setNull();
                    } else {
                        System.err.println("Warning: Failed to create IVar " + myst + " in " + root);
                    }
                }
            } else if (mode == 2) {
                root.rmIVar(myst);
            }
        }
        return res;
    }

    // Used by SDB stuff that generates paths.
    public static String poundEscape(String arg) {
        StringBuilder res = new StringBuilder();
        for (char c : arg.toCharArray()) {
            boolean escape = false;
            if (c == '#')
                escape = true;
            for (char cb : breakersSDB2) {
                if (c == cb) {
                    escape = true;
                    break;
                }
            }
            if (escape)
                res.append('#');
            res.append(c);
        }
        return res.toString();
    }
}

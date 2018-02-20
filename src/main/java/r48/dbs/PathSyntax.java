/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import r48.RubyIO;

/**
 * NOTE: This uses escapes internally to escape from itself.
 * With that in mind, do not escape this w/EscapedStringSyntax. It's not necessary.
 * Created on 08/06/17.
 */
public class PathSyntax {
    // NOTE: This must not contain \, as that is used for EscapedStringSyntax embedded in hashes
    public static char[] breakers = new char[] {'$', '@', ']'};
    public static char[] breakersSDB2 = new char[] {':', '@', ']'};

    // break to next token.
    public static String[] breakToken(String full, boolean sdb2) {
        int plannedIdx = full.length();
        StringBuilder sb = new StringBuilder();
        if (sdb2) {
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
        } else {
            for (char c : breakers) {
                int idx = full.indexOf(c);
                if (idx >= 0)
                    if (idx < plannedIdx)
                        plannedIdx = idx;
            }
            if (plannedIdx == full.length()) {
                return new String[] {
                        full.substring(0, plannedIdx),
                        ""
                };
            }
            return new String[] {
                    full.substring(0, plannedIdx),
                    full.substring(plannedIdx)
            };
        }
    }

    // Used for missing IV autodetect
    public static String getAbsoluteIVar(String iv, boolean sdb2) {
        if (iv.startsWith("@")) {
            String n = iv.substring(1);
            if (breakToken(n, sdb2).equals(n))
                return iv;
        }
        if (iv.startsWith(sdb2 ? ":." : "$:")) {
            String n = iv.substring(2);
            if (breakToken(n, sdb2).equals(n))
                return n;
        }
        return null;
    }

    public static RubyIO parse(RubyIO res, String arg, boolean sdb2) {
        return parse(res, arg, 0, sdb2);
    }

    // Parses the syntax.
    // mode 0: GET
    // mode 1: GET/ADD
    // mode 2: GET/DEL
    // Note that you detect creation by checking if output type is 0 (which should otherwise never happen)
    public static RubyIO parse(RubyIO res, String arg, int mode, boolean sdb2) {
        String workingArg = arg;
        while (workingArg.length() > 0) {
            char f = workingArg.charAt(0);
            workingArg = workingArg.substring(1);
            String[] subcomA = breakToken(workingArg, sdb2);
            String subcom = subcomA[0];
            workingArg = subcomA[1];
            boolean specialImmediate = (mode != 0) & (workingArg.length() == 0);
            if (f == (sdb2 ? ':' : '$')) {
                if (subcom.startsWith("{")) {
                    String esc = subcom.substring(1);
                    if (!sdb2)
                        esc = EscapedStringSyntax.unescape(esc);
                    RubyIO hashVal = ValueSyntax.decode(esc, sdb2);
                    RubyIO root = res;
                    res = res.getHashVal(hashVal);
                    if (specialImmediate) {
                        if (mode == 1) {
                            if (res == null) {
                                res = new RubyIO();
                                root.hashVal.put(hashVal, res);
                            }
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
                        if (res.arrVal == null) {
                            res = null;
                        } else {
                            res = new RubyIO().setFX(res.arrVal.length);
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
                } else if (atl >= res.arrVal.length) {
                    res = null;
                    break;
                }
                res = res.arrVal[atl];
            } else {
                throw new RuntimeException("Bad pathsynt starter " + f + " (did root get separated properly?) code " + arg);
            }
            if (res == null)
                return null;
        }
        return res;
    }

    private static RubyIO mapIV(RubyIO res, String myst, boolean specialImmediate, int mode) {
        RubyIO root = res;
        res = res.getInstVarBySymbol(myst);
        if (specialImmediate) {
            if (mode == 1) {
                if (res == null) {
                    res = new RubyIO();
                    root.addIVar(myst, res);
                }
            } else if (mode == 2) {
                root.rmIVar(myst);
            }
        }
        return res;
    }
}

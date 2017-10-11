/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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

    // break to next token.
    public static String breakToken(String full) {
        int plannedIdx = full.length();
        for (char c : breakers) {
            int idx = full.indexOf(c);
            if (idx >= 0)
                if (idx < plannedIdx)
                    plannedIdx = idx;
        }
        return full.substring(0, plannedIdx);
    }

    // Used for missing IV autodetect
    public static String getAbsoluteIVar(String iv) {
        if (iv.startsWith("@")) {
            String n = iv.substring(1);
            if (breakToken(n).equals(n))
                return iv;
        }
        if (iv.startsWith("$:")) {
            String n = iv.substring(2);
            if (breakToken(n).equals(n))
                return n;
        }
        return null;
    }

    public static RubyIO parse(RubyIO res, String arg) {
        return parse(res, arg, 0);
    }
    // Parses the syntax.
    // mode 0: GET
    // mode 1: GET/ADD
    // mode 2: GET/DEL
    // Note that you detect creation by checking if output type is 0 (which should otherwise never happen)
    public static RubyIO parse(RubyIO res, String arg, int mode) {
        String workingArg = arg;
        while (workingArg.length() > 0) {
            char f = workingArg.charAt(0);
            workingArg = workingArg.substring(1);
            String subcom = breakToken(workingArg);
            workingArg = workingArg.substring(subcom.length());
            boolean specialImmediate = (mode != 0) & (workingArg.length() == 0);
            switch (f) {
                case '$':
                    if (subcom.startsWith("{")) {
                        RubyIO hashVal = new RubyIO();
                        if (subcom.startsWith("{:")) {
                            hashVal.setString(EscapedStringSyntax.unescape(subcom.substring(2)));
                        } else {
                            int i = Integer.parseInt(subcom.substring(1));
                            hashVal.setFX(i);
                        }
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
                    } else if (subcom.startsWith(":")) {
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
                    break;
                case '@':
                    res = mapIV(res, "@" + subcom, specialImmediate, mode);
                    break;
                case ']':
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
                    break;
                default:
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

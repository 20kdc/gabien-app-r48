/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

import r48.RubyIO;

/**
 * Created on 08/06/17.
 */
public class PathSyntax {
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

    // Parses the syntax.
    public static RubyIO parse(RubyIO res, String arg) {
        String workingArg = arg;
        while (workingArg.length() > 0) {
            char f = workingArg.charAt(0);
            workingArg = workingArg.substring(1);
            String subcom = breakToken(workingArg);
            workingArg = workingArg.substring(subcom.length());
            switch (f) {
                case '$':
                    if (subcom.startsWith(":")) {
                        RubyIO hashVal = new RubyIO();
                        if (subcom.startsWith(":\"")) {
                            hashVal.setString(subcom.substring(2));
                        } else {
                            int i = Integer.parseInt(subcom.substring(1));
                            hashVal.setFX(i);
                        }
                        return res.getHashVal(hashVal);
                    } else {
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
                            throw new RuntimeException("$-command must be '', ':\"someS1FormatTextForHVal', ':123' (hash number), 'length', 'fail'");
                        }
                    }
                    break;
                case '@':
                    res = res.getInstVarBySymbol("@" + subcom);
                    break;
                case ']':
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
}

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
                    if (subcom.length() != 0)
                        throw new RuntimeException("unsure what to do here, $ doesn't accept additional");
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

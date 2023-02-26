/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.minivm.MVMCArrayGetImm;
import r48.minivm.MVMCArrayLength;
import r48.minivm.MVMCContext;
import r48.minivm.MVMCError;
import r48.minivm.MVMCExpr;
import r48.minivm.MVMCGetHashValImm;
import r48.minivm.MVMCGetIVar;
import r48.minivm.MVMCPathHashAdd;
import r48.minivm.MVMCPathHashDel;

/**
 * NOTE: This uses escapes internally to escape from itself.
 * With that in mind, do not escape this w/EscapedStringSyntax. It's not necessary.
 * Created on 08/06/17, heavily refactored 26 February 2023.
 */
public final class PathSyntax {
    // MiniVM programs for the various PathSyntax operations.
    private final MVMCExpr getProgram, addProgram, delProgram;

    // NOTE: This must not contain anything used in ValueSyntax.
    public static char[] breakersSDB2 = new char[] {':', '@', ']'};

    private PathSyntax(MVMCExpr g, MVMCExpr a, MVMCExpr d) {
        getProgram = g;
        assert g.isPure;
        addProgram = a;
        delProgram = d;
    }

    public final RORIO get(RORIO ro) {
        return get((IRIO) ro);
    }

    public final IRIO get(IRIO v) {
        return getProgram.execute(null, v, null, null, null, null, null, null, null);
    }

    public final IRIO add(IRIO v) {
        return addProgram.execute(null, v, null, null, null, null, null, null, null);
    }

    public final IRIO del(IRIO v) {
        return delProgram.execute(null, v, null, null, null, null, null, null, null);
    }

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
    public static String getAbsoluteIVar(PathSyntax iv) {
        if (iv.getProgram instanceof MVMCGetIVar)
            return ((MVMCGetIVar) iv.getProgram).key;
        return null;
    }

    /**
     * Deprecated because PathSyntax is being moved to a compilation-based system.
     */
    @Deprecated
    public static IRIO parse(IRIO res, String arg) {
        PathSyntax ps = compile(MVMCExpr.getL0, arg);
        return ps.get(res);
    }

    public static PathSyntax compile(String arg) {
        return compile(MVMCExpr.getL0, arg);
    }

    public static PathSyntax compile(PathSyntax basePS, String arg) {
        return compile(basePS.getProgram, arg);
    }

    public static PathSyntax compile(MVMCExpr base, String arg) {
        // System.out.println("compiled pathsyntax " + arg);
        String workingArg = arg;
        while (workingArg.length() > 0) {
            char f = workingArg.charAt(0);
            workingArg = workingArg.substring(1);
            String[] subcomA = breakToken(workingArg);
            String subcom = subcomA[0];
            workingArg = subcomA[1];
            boolean lastElement = (workingArg.length() == 0);
            String queuedIV = null;
            if (f == ':') {
                if (subcom.startsWith("{")) {
                    String esc = subcom.substring(1);
                    IRIO hashVal = ValueSyntax.decode(esc);
                    MVMCExpr currentGet = new MVMCGetHashValImm(base, hashVal);
                    if (lastElement)
                        return new PathSyntax(currentGet, new MVMCPathHashAdd(base, hashVal), new MVMCPathHashDel(base, hashVal));
                    base = currentGet;
                } else if (subcom.startsWith(".")) {
                    queuedIV = subcom.substring(1);
                } else {
                    if (subcom.equals("length")) {
                        base = new MVMCArrayLength(base);
                        if (lastElement)
                            return new PathSyntax(base, base, new MVMCError("Cannot delete array length. Fix your schema."));
                    } else if (subcom.equals("fail")) {
                        base = new MVMCExpr.Const(null);
                        if (lastElement)
                            return new PathSyntax(base, base, base);
                    } else if (subcom.length() != 0) {
                        throw new RuntimeException("$-command must be '$' (self), '${\"someSFormatTextForHVal' (hash string), '${123' (hash number), '$:someIval' ('raw' iVar), '$length', '$fail'");
                    }
                }
            } else if (f == '@') {
                queuedIV = "@" + subcom;
            } else if (f == ']') {
                final int atl = Integer.parseInt(subcom);
                base = new MVMCArrayGetImm(base, atl);
                if (lastElement)
                    return new PathSyntax(base, base, new MVMCError("Cannot delete array element. Fix your schema."));
            } else {
                throw new RuntimeException("Bad pathsynt starter " + f + " (did root get separated properly?) code " + arg);
            }
            if (queuedIV != null) {
                final String iv = queuedIV;
                MVMCExpr currentGet = new MVMCGetIVar(base, queuedIV);
                final MVMCExpr parent = base;
                if (lastElement)
                    return new PathSyntax(currentGet, new MVMCExpr(false) {
                        @Override
                        public IRIO execute(MVMCContext ctx, IRIO l0, IRIO l1, IRIO l2, IRIO l3, IRIO l4, IRIO l5, IRIO l6, IRIO l7) {
                            IRIO res = parent.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                            if (res == null)
                                return null;
                            IRIO ivv = res.getIVar(iv);
                            // As of DM2 this is guaranteed to create a defined value,
                            //  and setting it to null will break things.
                            if (ivv == null)
                                ivv = res.addIVar(iv);
                            if (ivv == null)
                                System.err.println("Warning: Failed to create IVar " + iv + " in " + res);
                            return ivv;
                        }
                    }, new MVMCExpr(false) {
                        @Override
                        public IRIO execute(MVMCContext ctx, IRIO l0, IRIO l1, IRIO l2, IRIO l3, IRIO l4, IRIO l5, IRIO l6, IRIO l7) {
                            IRIO res = parent.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                            if (res == null)
                                return null;
                            IRIO ivv = res.getIVar(iv);
                            res.rmIVar(iv);
                            return ivv;
                        }
                    });
                base = currentGet;
            }
        }
        return new PathSyntax(base, base, new MVMCError("Cannot delete empty/self path. Fix your schema."));
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

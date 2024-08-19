/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;

import datum.DatumSymbol;
import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.minivm.expr.MVMCDMArrayGetImm;
import r48.minivm.expr.MVMCDMArrayLength;
import r48.minivm.expr.MVMCError;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCDMGetHashDefVal;
import r48.minivm.expr.MVMCDMGetHashValImm;
import r48.minivm.expr.MVMCDMGetIVar;
import r48.minivm.expr.MVMCPathHashAdd;
import r48.minivm.expr.MVMCPathHashDel;

/**
 * NOTE: This uses escapes internally to escape from itself.
 * With that in mind, do not escape this w/EscapedStringSyntax. It's not necessary.
 * Created on 08/06/17, heavily refactored 26 February 2023.
 */
public final class PathSyntax implements Function<IRIO, IRIO> {
    // MiniVM programs for the various PathSyntax operations.
    public final MVMCExpr getProgram, addProgram, delProgram;
    public final String decompiled;
    public final MVMEnv parentContext;
    /**
     * Used by tests to make sure more issues are caught.
     */
    public final boolean strict;

    // NOTE: This must not contain anything used in ValueSyntax.
    public static char[] breakersSDB2 = new char[] {':', '@', ']'};

    private PathSyntax(MVMEnv parentContext, boolean strict, MVMCExpr g, MVMCExpr a, MVMCExpr d, String dc) {
        this.parentContext = parentContext;
        this.strict = strict;
        getProgram = g;
        addProgram = a;
        delProgram = d;
        decompiled = dc;
    }

    @Override
    public IRIO apply(IRIO a) {
        return getRW(a);
    }

    /**
     * Translates the input IRIO to the target RORIO, or null if an issue was encountered.
     */
    public final RORIO getRO(RORIO v) {
        try {
            return (RORIO) getProgram.exc(MVMScope.ROOT, v);
        } catch (Exception ex) {
            if (strict)
                throw ex;
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Translates the input IRIO to the target IRIO, or null if an issue was encountered.
     */
    public final IRIO getRW(IRIO v) {
        try {
            return (IRIO) getProgram.exc(MVMScope.ROOT, v);
        } catch (Exception ex) {
            if (strict)
                throw ex;
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Translates the input IRIO to the output.
     * If it does not exist, adds the hash key/ivar.
     * Returns null if an issue was encountered.
     */
    public final IRIO add(IRIO v) {
        try {
            return (IRIO) addProgram.exc(MVMScope.ROOT, v);
        } catch (Exception ex) {
            if (strict)
                throw ex;
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Translates the input IRIO to the output.
     * Then removes the "final component", i.e. specific ivar/hash entry.
     * Returns null if an issue was encountered.
     */
    public final IRIO del(IRIO v) {
        try {
            return (IRIO) delProgram.exc(MVMScope.ROOT, v);
        } catch (Exception ex) {
            if (strict)
                throw ex;
            ex.printStackTrace();
            return null;
        }
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
        if (iv.getProgram instanceof MVMCDMGetIVar) {
            MVMCDMGetIVar sb = ((MVMCDMGetIVar) iv.getProgram);
            if (sb.base != MVMCExpr.getL0)
                return null;
            return sb.key;
        }
        return null;
    }

    public static PathSyntax compile(App parentContext, String arg) {
        return compile(parentContext.vmCtx, parentContext.ilg.strict, MVMCExpr.getL0, arg);
    }

    public static PathSyntax compile(MVMEnvR48 parentContext, String arg) {
        return compile(parentContext, parentContext.strict, MVMCExpr.getL0, arg);
    }

    public static PathSyntax compile(MVMEnv parentContext, boolean strict, String arg) {
        return compile(parentContext, strict, MVMCExpr.getL0, arg);
    }

    public static PathSyntax compile(PathSyntax basePS, String arg) {
        return compile(basePS.parentContext, basePS.strict, basePS.getProgram, arg);
    }

    public static PathSyntax compile(MVMEnv parentContext, boolean strict, MVMCExpr base, String arg) {
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
                    DMKey hashVal = ValueSyntax.decode(esc);
                    MVMCExpr currentGet = new MVMCDMGetHashValImm(base, hashVal);
                    if (lastElement)
                        return new PathSyntax(parentContext, strict, currentGet, new MVMCPathHashAdd(base, hashVal), new MVMCPathHashDel(base, hashVal), arg);
                    base = currentGet;
                } else if (subcom.startsWith(".")) {
                    queuedIV = subcom.substring(1);
                } else {
                    if (subcom.equals("length")) {
                        base = new MVMCDMArrayLength(base);
                        if (lastElement)
                            return new PathSyntax(parentContext, strict, base, base, new MVMCError("Cannot delete array length. Fix your schema."), arg);
                    } else if (subcom.equals("defVal")) {
                        base = new MVMCDMGetHashDefVal(base);
                        if (lastElement)
                            return new PathSyntax(parentContext, strict, base, base, new MVMCError("Cannot delete hash default value. Fix your schema."), arg);
                    } else if (subcom.equals("fail")) {
                        base = new MVMCExpr.Const(null, MVMType.ANY);
                        if (lastElement)
                            return new PathSyntax(parentContext, strict, base, base, base, arg);
                    } else if (subcom.length() != 0) {
                        throw new RuntimeException("$-command must be '$' (self), '${\"someSFormatTextForHVal' (hash string), '${123' (hash number), '$:someIval' ('raw' iVar), '$length', '$fail'");
                    }
                }
            } else if (f == '@') {
                queuedIV = "@" + subcom;
            } else if (f == ']') {
                final int atl = Integer.parseInt(subcom);
                base = new MVMCDMArrayGetImm(base, atl);
                if (lastElement)
                    return new PathSyntax(parentContext, strict, base, base, new MVMCError("Cannot delete array element. Fix your schema."), arg);
            } else {
                throw new RuntimeException("Bad pathsynt starter " + f + " (did root get separated properly?) code " + arg);
            }
            if (queuedIV != null) {
                final String iv = queuedIV;
                MVMCExpr currentGet = new MVMCDMGetIVar(base, queuedIV);
                final MVMCExpr parent = base;
                if (lastElement)
                    return new PathSyntax(parentContext, strict, currentGet, new MVMCExpr(MVMEnvR48.IRIO_TYPE) {
                        @Override
                        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                            IRIO res = (IRIO) parent.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
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

                        @Override
                        public Object disasm() {
                            return MVMU.l(new DatumSymbol("pathAddIVar"), parent.disasm(), iv);
                        }
                    }, new MVMCExpr(MVMEnvR48.IRIO_TYPE) {
                        @Override
                        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                            IRIO res = (IRIO) parent.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                            if (res == null)
                                return null;
                            IRIO ivv = res.getIVar(iv);
                            res.rmIVar(iv);
                            return ivv;
                        }

                        @Override
                        public Object disasm() {
                            return MVMU.l(new DatumSymbol("pathDelIVar"), parent.disasm(), iv);
                        }
                    }, arg);
                base = currentGet;
            }
        }
        return new PathSyntax(parentContext, strict, base, base, new MVMCError("Cannot delete empty/self path. Fix your schema."), arg);
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

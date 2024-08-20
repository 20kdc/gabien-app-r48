/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import java.util.HashSet;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMScope;
import r48.minivm.expr.MVMCDMAddIVar;
import r48.minivm.expr.MVMCDMArrayGetImm;
import r48.minivm.expr.MVMCDMArrayLength;
import r48.minivm.expr.MVMCDMDelIVar;
import r48.minivm.expr.MVMCError;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCLinear;
import r48.minivm.expr.MVMCDMGetHashDefVal;
import r48.minivm.expr.MVMCDMGetHashValImm;
import r48.minivm.expr.MVMCDMGetIVar;
import r48.minivm.expr.MVMCPathHashAdd;
import r48.minivm.expr.MVMCPathHashDel;

/**
 * NOTE: This uses escapes internally to escape from itself.
 * Created on 08/06/17, heavily refactored 26 February 2023.
 */
public final class PathSyntax implements Function<IRIO, IRIO> {
    /*
     * MiniVM programs for the various PathSyntax operations.
     */
    public final MVMCLinear getProgram, addProgram, delProgram;
    public final String decompiled;
    public final MVMEnv parentContext;
    /**
     * Used by tests to make sure more issues are caught.
     */
    public final boolean strict;

    // NOTE: This must not contain anything used in ValueSyntax.
    public static char[] breakersSDB2 = new char[] {':', '@', ']'};

    private PathSyntax(MVMEnv parentContext, boolean strict, MVMCLinear g, MVMCLinear a, MVMCLinear d, String dc) {
        this.parentContext = parentContext;
        this.strict = strict;
        getProgram = g;
        addProgram = a;
        delProgram = d;
        decompiled = dc;
    }

    /**
     * Appends a step onto this PathSyntax, returning a new one.
     */
    public PathSyntax withStep(@NonNull MVMCLinear.Step get, @NonNull MVMCLinear.Step add, @NonNull MVMCLinear.Step del, String dc) {
        // The use of the getProgram as the base is intentional.
        // The get/add/del differentiation is only for the last step.

        MVMCLinear.Step[] steps = new MVMCLinear.Step[getProgram.steps.length + 1];
        System.arraycopy(getProgram.steps, 0, steps, 0, getProgram.steps.length);

        steps[steps.length - 1] = get;
        MVMCLinear getProgram = new MVMCLinear(this.getProgram.source, steps.clone());
        steps[steps.length - 1] = add;
        MVMCLinear addProgram = new MVMCLinear(this.getProgram.source, steps.clone());
        steps[steps.length - 1] = del;
        MVMCLinear delProgram = new MVMCLinear(this.getProgram.source, steps);

        return new PathSyntax(parentContext, strict, getProgram, addProgram, delProgram, dc);
    }

    /**
     * Appends a step onto this PathSyntax, returning a new one.
     */
    public PathSyntax withStepRO(@NonNull MVMCLinear.Step get, @NonNull String error, String dc) {
        return withStep(get, get, new MVMCError(error), dc);
    }

    /**
     * With instance variable.
     */
    public PathSyntax withIVar(String iv, String arg) {
        MVMCLinear.Step currentGet = new MVMCDMGetIVar(iv);
        return withStep(currentGet, new MVMCDMAddIVar(iv), new MVMCDMDelIVar(iv), arg);
    }

    /**
     * Appends another PathSyntax onto this PathSyntax.
     * Kind of horrific memory-thrashing-wise but it'll work.
     * It's intended for very small uses in 'navigate to' buttons, that kinda deal.
     */
    public PathSyntax concatWith(@NonNull PathSyntax other) {
        PathSyntax workingOn = this;
        String decomp2 = decompiled + other.decompiled;
        for (int i = 0; i < other.getProgram.steps.length; i++)
            workingOn = workingOn.withStep(other.getProgram.steps[i], other.addProgram.steps[i], other.delProgram.steps[i], decomp2);
        return workingOn;
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
     * Traces the execution of a get to find the involved objects.
     * Used in SchemaPath.tracePathRoute().
     */
    public final @Nullable HashSet<RORIO> traceRO(RORIO v) {
        HashSet<RORIO> set = new HashSet<>();
        try {
            Object[] res = getProgram.executeWithIntrospection(MVMScope.ROOT, v, null, null, null, null, null, null, null);
            for (int i = 0; i < res.length; i++)
                if (res[i] instanceof RORIO)
                    set.add((RORIO) res[i]);
            return set;
        } catch (Exception ex) {
            if (strict)
                throw ex;
            ex.printStackTrace();
            return set;
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
        if (iv.getProgram.steps.length != 1)
            return null;
        MVMCLinear.Step step = iv.getProgram.steps[0];
        if (step instanceof MVMCDMGetIVar)
            return ((MVMCDMGetIVar) step).key;
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
        PathSyntax workingOn = new PathSyntax(parentContext, strict, new MVMCLinear(base), new MVMCLinear(base), new MVMCLinear(base), arg);
        // System.out.println("compiled pathsyntax " + arg);
        String workingArg = arg;
        while (workingArg.length() > 0) {
            char f = workingArg.charAt(0);
            workingArg = workingArg.substring(1);
            String[] subcomA = breakToken(workingArg);
            String subcom = subcomA[0];
            workingArg = subcomA[1];
            if (f == ':') {
                if (subcom.startsWith("{")) {
                    String esc = subcom.substring(1);
                    DMKey hashVal = ValueSyntax.decode(esc);
                    workingOn = workingOn.withStep(new MVMCDMGetHashValImm(hashVal), new MVMCPathHashAdd(hashVal), new MVMCPathHashDel(hashVal), arg);
                } else if (subcom.startsWith(".")) {
                    workingOn = workingOn.withIVar(subcom.substring(1), arg);
                } else {
                    if (subcom.equals("length")) {
                        MVMCLinear.Step currentGet = new MVMCDMArrayLength();
                        workingOn = workingOn.withStepRO(currentGet, "Cannot delete array length. Fix your schema.", arg);
                    } else if (subcom.equals("defVal")) {
                        MVMCLinear.Step currentGet = new MVMCDMGetHashDefVal();
                        workingOn = workingOn.withStepRO(currentGet, "Cannot delete hash default value. Fix your schema.", arg);
                    } else if (subcom.equals("fail")) {
                        MVMCLinear.Step currentGet = new MVMCLinear.Const(null, MVMEnvR48.IRIO_TYPE);
                        workingOn = workingOn.withStep(currentGet, currentGet, currentGet, arg);
                    } else if (subcom.length() != 0) {
                        throw new RuntimeException("$-command must be '$' (self), '${\"someSFormatTextForHVal' (hash string), '${123' (hash number), '$:someIval' ('raw' iVar), '$length', '$fail'");
                    }
                }
            } else if (f == '@') {
                workingOn = workingOn.withIVar("@" + subcom, arg);
            } else if (f == ']') {
                final int atl = Integer.parseInt(subcom);
                MVMCLinear.Step currentGet = new MVMCDMArrayGetImm(atl);
                workingOn = workingOn.withStepRO(currentGet, "Cannot delete array element. Fix your schema.", arg);
            } else {
                throw new RuntimeException("Bad pathsynt starter " + f + " (did root get separated properly?) code " + arg);
            }
        }
        return workingOn;
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

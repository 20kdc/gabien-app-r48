/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import java.util.HashSet;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

import r48.io.data.DMKey;
import r48.io.data.DMPath;
import r48.io.data.IRIO;
import r48.io.data.RORIO;

/**
 * NOTE: This uses escapes internally to escape from itself.
 * Created on 08/06/17, heavily refactored 26 February 2023.
 */
public final class PathSyntax implements Function<IRIO, IRIO> {
    /*
     * The actual path.
     */
    public final DMPath path;
    /**
     * The source to this path, as entered.
     * Can be DYNAMIC_PLACEHOLDER.
     */
    public final String decompiled;

    public static String DYNAMIC_PLACEHOLDER = "(generated)";

    // NOTE: This must not contain anything used in ValueSyntax.
    public static char[] breakersSDB2 = new char[] {':', '@', ']'};

    public PathSyntax(DMPath path) {
        this(path, DYNAMIC_PLACEHOLDER);
    }

    private PathSyntax(DMPath path, String dc) {
        this.path = path;
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
        return path.getRO(v);
    }

    /**
     * Traces the execution of a get to find the involved objects.
     * Used in SchemaPath.tracePathRoute().
     */
    public final @Nullable HashSet<RORIO> traceRO(RORIO v) {
        HashSet<RORIO> set = new HashSet<>();
        RORIO[] res = path.traceRouteComplete(v);
        for (int i = 0; i < res.length; i++)
            if (res[i] instanceof RORIO)
                set.add((RORIO) res[i]);
        return set;
    }

    /**
     * Translates the input IRIO to the target IRIO, or null if an issue was encountered.
     */
    public final IRIO getRW(IRIO v) {
        return path.getRW(v);
    }

    /**
     * Translates the input IRIO to the output.
     * If it does not exist, adds the hash key/ivar.
     * Returns null if an issue was encountered.
     */
    public final IRIO add(IRIO v) {
        return path.add(v);
    }

    /**
     * Translates the input IRIO to the output.
     * Then removes the "final component", i.e. specific ivar/hash entry.
     * Returns null if an issue was encountered.
     */
    public final IRIO del(IRIO v) {
        return path.del(v);
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
        if (iv.path instanceof DMPath.IVar)
            return ((DMPath.IVar) iv.path).key;
        return null;
    }

    public static PathSyntax compile(PathSyntax basePS, String arg) {
        return new PathSyntax(basePS.path.with(compile(basePS.path.strict, arg).path), basePS.decompiled + arg);
    }

    public static PathSyntax compile(boolean strict) {
        return new PathSyntax(strict ? DMPath.EMPTY_STRICT : DMPath.EMPTY_RELAXED, "");
    }

    public static PathSyntax compile(boolean strict, String arg) {
        DMPath workingOn = strict ? DMPath.EMPTY_STRICT : DMPath.EMPTY_RELAXED;
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
                    workingOn = workingOn.withHash(hashVal);
                } else if (subcom.startsWith(".")) {
                    workingOn = workingOn.withIVar(subcom.substring(1));
                } else {
                    if (subcom.equals("length")) {
                        workingOn = workingOn.withArrayLength();
                    } else if (subcom.equals("defVal")) {
                        workingOn = workingOn.withDefVal();
                    } else if (subcom.equals("fail")) {
                        workingOn = workingOn.withFail();
                    } else if (subcom.length() != 0) {
                        throw new RuntimeException("$-command must be '$' (self), '${\"someSFormatTextForHVal' (hash string), '${123' (hash number), '$:someIval' ('raw' iVar), '$length', '$fail'");
                    }
                }
            } else if (f == '@') {
                workingOn = workingOn.withIVar("@" + subcom);
            } else if (f == ']') {
                final int atl = Integer.parseInt(subcom);
                workingOn = workingOn.withArray(atl);
            } else {
                throw new RuntimeException("Bad pathsynt starter " + f + " (did root get separated properly?) code " + arg);
            }
        }
        return new PathSyntax(workingOn, arg);
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

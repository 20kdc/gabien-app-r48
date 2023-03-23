/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import r48.dbs.FormatSyntax;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.fn.MVMFn;

/**
 * Dynamic translation slot base.
 * Split from DynTrSlot 23rd March 2023.
 */
public abstract class DynTrBase extends MVMFn.Fixed implements TrPage.FF0, TrPage.FF1, TrPage.FF2, TrPage.FF3, TrPage.FF4 {
    public final String id;
    public final DatumSrcLoc sourceLoc;

    public DynTrBase(String i, DatumSrcLoc srcLoc) {
        super(i);
        id = i;
        sourceLoc = srcLoc;
    }

    @Override
    public String toString() {
        return id + "@" + sourceLoc;
    }

    public static Object compileValue(@NonNull MVMEnv env, @Nullable DatumSymbol mode, DatumSrcLoc srcLoc, Object v) {
        if (mode == null) {
            return env.evalObject(v, srcLoc);
        } else {
            return ((MVMFn) env.getSlot(mode).v).clDirect(v);
        }
    }

    /**
     * Gets the compiled value.
     */
    public abstract Object getCompiledValue();

    private String resolve(int ac, Object a0, Object a1, Object a2, Object a3) {
        Object valueCompiled = getCompiledValue();
        try {
            if (valueCompiled instanceof String) {
                return (String) valueCompiled;
            } else if (valueCompiled instanceof MVMFn) {
                Object res = null;
                switch (ac) {
                case 0:
                    res = ((MVMFn) valueCompiled).clDirect();
                    break;
                case 1:
                    res = ((MVMFn) valueCompiled).clDirect(a0);
                    break;
                case 2:
                    res = ((MVMFn) valueCompiled).clDirect(a0, a1);
                    break;
                case 3:
                    res = ((MVMFn) valueCompiled).clDirect(a0, a1, a2);
                    break;
                case 4:
                    res = ((MVMFn) valueCompiled).clDirect(a0, a1, a2, a3);
                    break;
                }
                if (res == null)
                    return "!!!(null DynTrSlot return @ " + id + ")!!!";
                return res.toString();
            } else if (valueCompiled instanceof FormatSyntax.ICompiledFormatSyntax) {
                if (ac != 1)
                    return "!!!(FormatSyntax args bad @ " + id + ")!!!";
                return ((FormatSyntax.ICompiledFormatSyntax) valueCompiled).r((RORIO) a0);
            } else if (ac == 0 && valueCompiled instanceof TrPage.FF0) {
                return ((TrPage.FF0) valueCompiled).r();
            } else if (ac == 1 && valueCompiled instanceof TrPage.FF1) {
                return ((TrPage.FF1) valueCompiled).r(a0);
            } else if (ac == 2 && valueCompiled instanceof TrPage.FF2) {
                return ((TrPage.FF2) valueCompiled).r(a0, a1);
            } else if (ac == 3 && valueCompiled instanceof TrPage.FF3) {
                return ((TrPage.FF3) valueCompiled).r(a0, a1, a2);
            } else if (ac == 4 && valueCompiled instanceof TrPage.FF4) {
                return ((TrPage.FF4) valueCompiled).r(a0, a1, a2, a3);
            }
            return valueCompiled.toString();
        } catch (Exception ex) {
            System.err.println("at " + id + ":");
            ex.printStackTrace();
            return "!!!" + id + "!!!";
        }
    }

    @Override
    public String r() {
        return resolve(0, null, null, null, null);
    }

    @Override
    public String r(Object a0) {
        return resolve(1, a0, null, null, null);
    }

    @Override
    public String r(Object a0, Object a1) {
        return resolve(2, a0, a1, null, null);
    }

    @Override
    public String r(Object a0, Object a1, Object a2) {
        return resolve(3, a0, a1, a2, null);
    }

    @Override
    public String r(Object a0, Object a1, Object a2, Object a3) {
        return resolve(4, a0, a1, a2, a3);
    }

    @Override
    public Object callDirect() {
        return r();
    }

    @Override
    public Object callDirect(Object a0) {
        return r(a0);
    }

    @Override
    public Object callDirect(Object a0, Object a1) {
        return r(a0, a1);
    }

    @Override
    public Object callDirect(Object a0, Object a1, Object a2) {
        return r(a0, a1, a2);
    }

    @Override
    public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
        return r(a0, a1, a2, a3);
    }
}

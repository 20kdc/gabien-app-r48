/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import gabien.uslx.append.IFunction;
import r48.dbs.FormatSyntax;
import r48.io.data.RORIO;
import r48.minivm.MVMEnvR48;
import r48.minivm.fn.MVMFn;
import r48.schema.SchemaElement;

/**
 * Dynamic translation slot.
 * Created 12th March 2023.
 */
public final class DynTrSlot implements IDynTr {
    public static final DatumSymbol DYNTR_FF1 = new DatumSymbol("tr-dyn-compiler-ff1");
    public static final DatumSymbol DYNTR_FF2 = new DatumSymbol("tr-dyn-compiler-ff2");
    // indirect binding to FormatSyntax, see MVMDMAppLibrary
    public static final DatumSymbol FORMATSYNTAX = new DatumSymbol("dm-formatsyntax");
    public static final DatumSymbol CMSYNTAX_NEW = new DatumSymbol("dm-cmsyntax-new");

    public final MVMEnvR48 env;
    public final DatumSrcLoc sourceLoc;
    public final String id;
    public final String originalSrc;
    public final @Nullable DatumSymbol mode;
    // The source of the value is cached so that dynamic translation can work properly.
    private String valueSrc;
    private Object valueCompiled;

    public DynTrSlot(MVMEnvR48 e, DatumSrcLoc sl, String i, @Nullable DatumSymbol m, Object base) {
        env = e;
        sourceLoc = sl;
        id = i;
        mode = m;
        setValue(base);
        originalSrc = valueSrc;
    }

    public void setValue(Object v) {
        valueSrc = DatumWriter.objectToString(v);
        valueCompiled = mode != null ? ((MVMFn) env.getSlot(mode).v).clDirect(v) : v;
    }

    @Override
    public String toString() {
        return id + "@" + sourceLoc;
    }

    /**
     * Attempts to dump source of the value.
     */
    public String sourceDump() {
        return valueSrc;
    }

    @SuppressWarnings("unchecked")
    private String resolve(int ac, Object a0, Object a1, Object a2, Object a3) {
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
                if (ac == 1)
                    return ((FormatSyntax.ICompiledFormatSyntax) valueCompiled).r((RORIO) a0, null);
                // ideally, the parameter schema grabbers are forwarded at compile-time as a list
                // however, that world doesn't exist yet (#justCMDBThings), so this is in place in the meantime
                if (ac != 2)
                    return "!!!(FormatSyntax args bad @ " + id + ")!!!";
                return ((FormatSyntax.ICompiledFormatSyntax) valueCompiled).r((RORIO) a0, (IFunction<RORIO, SchemaElement>[]) a2);
            }
            return valueCompiled.toString();
        } catch (Exception ex) {
            System.err.println("at " + id + ":");
            ex.printStackTrace();
            return "!!!" + id + "!!!";
        }
    }

    public String r() {
        return resolve(0, null, null, null, null);
    }

    public String r(Object a0) {
        return resolve(1, a0, null, null, null);
    }

    public String r(Object a0, Object a1) {
        return resolve(2, a0, a1, null, null);
    }

    public String r(Object a0, Object a1, Object a2) {
        return resolve(3, a0, a1, a2, null);
    }

    public String r(Object a0, Object a1, Object a2, Object a3) {
        return resolve(4, a0, a1, a2, a3);
    }
}

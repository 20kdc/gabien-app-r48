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
import r48.minivm.MVMEnvR48;

/**
 * Dynamic translation slot.
 * Created 12th March 2023.
 */
public final class DynTrSlot extends DynTrBase {
    public static final DatumSymbol DYNTR_FF1 = new DatumSymbol("tr-dyn-compiler-ff1");
    public static final DatumSymbol DYNTR_FF2 = new DatumSymbol("tr-dyn-compiler-ff2");
    // indirect binding to FormatSyntax, see MVMDMAppLibrary
    public static final DatumSymbol FORMATSYNTAX = new DatumSymbol("dm-formatsyntax");
    public static final DatumSymbol CMSYNTAX_NEW = new DatumSymbol("dm-cmsyntax-new");

    public final MVMEnvR48 env;
    public final String originalSrc;
    public final @Nullable DatumSymbol mode;
    // The source of the value is cached so that dynamic translation can work properly.
    private String valueSrc;
    private Object valueCompiled;

    public DynTrSlot(MVMEnvR48 e, DatumSrcLoc sl, String i, @Nullable DatumSymbol m, Object base) {
        super(i, sl);
        env = e;
        mode = m;
        setValue(base);
        originalSrc = valueSrc;
    }

    public void setValue(Object v) {
        valueSrc = DatumWriter.objectToString(v);
        valueCompiled = compileValue(env, mode, sourceLoc, v);
    }

    /**
     * Attempts to dump source of the value.
     */
    public String sourceDump() {
        return valueSrc;
    }

    @Override
    public Object getCompiledValue() {
        return valueCompiled;
    }
}

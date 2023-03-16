/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import r48.minivm.MVMEnvR48;
import r48.minivm.fn.MVMFn;

/**
 * Dynamic translation slot.
 * Created 12th March 2023.
 */
public final class DynTrSlot implements IDynTr {
    private final DatumSymbol DYNTR_CALL_API = new DatumSymbol("tr-dyn-compiler");

    public final MVMEnvR48 env;
    public final DatumSrcLoc sourceLoc;
    public final String id;
    public final String originalSrc;
    // The source of the value is cached so that dynamic translation can work properly.
    private String valueSrc;
    private Object valueCompiled;

    public DynTrSlot(MVMEnvR48 e, DatumSrcLoc sl, String i, Object base) {
        env = e;
        sourceLoc = sl;
        id = i;
        setValue(base);
        originalSrc = valueSrc;
    }

    public void setValue(Object v) {
        valueSrc = DatumWriter.objectToString(v);
        valueCompiled = ((MVMFn) env.getSlot(DYNTR_CALL_API).v).clDirect(v);
    }

    /**
     * Attempts to dump source of the value.
     */
    public String sourceDump() {
        return valueSrc;
    }

    private String resolve(int ac, Object a0, Object a1, Object a2, Object a3) {
        try {
            if (valueCompiled instanceof String)
                return (String) valueCompiled;
            if (valueCompiled instanceof MVMFn) {
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
            }
            return valueCompiled.toString();
        } catch (Exception ex) {
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

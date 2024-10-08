/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSrcLoc;
import datum.DatumSymbol;
import r48.tr.TrPage.FF0;
import r48.tr.TrPage.FF1;
import r48.tr.TrPage.FF2;

/**
 * Dynamic translation proxy.
 * Created 13th March 2023.
 */
public interface IDynTrProxy {
    /**
     * Dynamically translate something.
     */
    DynTrBase dynTrBase(DatumSrcLoc srcLoc, String id, @Nullable DatumSymbol mode, Object text, boolean isNLS);

    /**
     * Dynamically translate a string.
     */
    default FF0 dTr(DatumSrcLoc srcLoc, String id, String text) {
        return dynTrBase(srcLoc, id, null, text, false);
    }

    /**
     * Dynamically translate a Datum object (as usual, for compilation)
     */
    default FF1 dTrFF1(DatumSrcLoc srcLoc, String id, Object text) {
        return dynTrBase(srcLoc, id, DynTrSlot.DYNTR_FF1, text, false);
    }

    /**
     * Dynamically translate a Datum object (as usual, for compilation)
     */
    default FF2 dTrFF2(DatumSrcLoc srcLoc, String id, Object text) {
        return dynTrBase(srcLoc, id, DynTrSlot.DYNTR_FF2, text, false);
    }

    /**
     * For defining name routines.
     */
    default FF1 dTrName(DatumSrcLoc srcLoc, String id, Object text, boolean isNLS) {
        return dynTrBase(srcLoc, TrNames.nameRoutine(id), DynTrSlot.DYNTR_FF1, text, isNLS);
    }
}

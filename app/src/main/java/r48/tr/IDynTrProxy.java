/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import java.util.List;

import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import r48.dbs.DatumLoader;

/**
 * Dynamic translation proxy.
 * Created 13th March 2023.
 */
public interface IDynTrProxy {
    /**
     * Dynamically translate something.
     */
    DynTrSlot dynTrBase(DatumSrcLoc srcLoc, String id, DatumSymbol mode, Object text);

    /**
     * Dynamically translate a string.
     */
    default DynTrSlot dTr(DatumSrcLoc srcLoc, String id, String text) {
        return dynTrBase(srcLoc, id, DynTrSlot.DYNTR_CALL_API, text);
    }

    /**
     * Dynamically translate a Datum object (as usual, for compilation)
     */
    default DynTrSlot dTrCode(DatumSrcLoc srcLoc, String id, String text) {
        return dynTrBase(srcLoc, id, DynTrSlot.DYNTR_CALL_API, DatumLoader.readInline(srcLoc, text));
    }

    /**
     * Legacy
     */
    default DynTrSlot dTrFmtSyn(DatumSrcLoc srcLoc, String id, List<String> text) {
        return dynTrBase(srcLoc, id, DynTrSlot.FORMATSYNTAX, text);
    }

    /**
     * Legacy 2
     */
    default DynTrSlot dTrFmtSynCM(DatumSrcLoc srcLoc, String id, String text) {
        return dynTrBase(srcLoc, id, DynTrSlot.CMSYNTAX, text);
    }
}

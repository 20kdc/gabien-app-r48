/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;

import gabien.datum.DatumSymbol;
import r48.dbs.PathSyntax;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 8th March 2023.
 */
public class MVMDMLibrary {
    public static void add(MVMEnv ctx) {
        // path
        ctx.defineSlot(new DatumSymbol("dm-at")).v = new DMAt(0)
            .attachHelp("(dm-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-add-at")).v = new DMAt(1)
            .attachHelp("(dm-add-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), adds entry if possible, #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-del-at")).v = new DMAt(2)
            .attachHelp("(dm-del-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), deletes entry, #nil on failure");
        // array
        ctx.defLib("dm-a-len", (a) -> {
            if (a == null)
                return null;
            if (((RORIO) a).getType() != '[')
                return null;
            return (long) ((RORIO) a).getALen();
        }).attachHelp("(dm-a-len TARGET) : Gets array length of TARGET. Returns #nil if not-an-array.");
        ctx.defLib("dm-a-ref", (a, i) -> {
            if (a == null)
                return null;
            RORIO ro = (RORIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            if (idx < 0 || idx >= ro.getALen())
                return null;
            return ro.getAElem(idx);
        }).attachHelp("(dm-a-ref TARGET INDEX) : Gets an array element of TARGET. Returns #nil if not-an-array or index invalid.");
        // ivars
        ctx.defLib("dm-ivars", (a) -> {
            LinkedList<String> lls = new LinkedList<>();
            if (a == null)
                return lls;
            RORIO ro = (RORIO) a;
            for (String iv : ro.getIVars())
                lls.add(iv);
            return lls;
        }).attachHelp("(dm-ivars TARGET) : Gets ivar names of TARGET (as strings).");
        ctx.defLib("dm-ivar-ref", (a, i) -> {
            if (a == null)
                return null;
            return ((RORIO) a).getIVar(MVMU.coerceToString(i));
        }).attachHelp("(dm-ivar-ref TARGET IV) : Retrieves ivar of TARGET (IV can be string or symbol).");
        ctx.defLib("dm-ivar-add!", (a, i) -> {
            if (a == null)
                return null;
            return ((IRIO) a).addIVar(MVMU.coerceToString(i));
        }).attachHelp("(dm-ivar-add! TARGET IV) : Adds ivar of TARGET (IV can be string or symbol).");
        ctx.defLib("dm-ivar-rm!", (a, i) -> {
            ((IRIO) a).rmIVar(MVMU.coerceToString(i));
            return null;
        }).attachHelp("(dm-ivar-rm! TARGET IV) : Removes ivar of TARGET (IV can be string or symbol).");
        // hash
        ctx.defLib("dm-hash-keys", (a) -> {
            LinkedList<DMKey> lls = new LinkedList<>();
            if (a == null)
                return lls;
            RORIO ro = (RORIO) a;
            for (DMKey iv : ro.getHashKeys())
                lls.add(iv);
            return lls;
        }).attachHelp("(dm-hash-keys TARGET) : Gets hash keys of TARGET (as DMKey).");
        ctx.defLib("dm-hash-ref", (a, i) -> {
            if (a == null)
                return null;
            return ((RORIO) a).getHashVal(dmKeyify(i));
        }).attachHelp("(dm-hash-ref TARGET IV) : Retrieves hash key of TARGET");
        ctx.defLib("dm-hash-add!", (a, i) -> {
            if (a == null)
                return null;
            return ((IRIO) a).addHashVal(dmKeyify(i));
        }).attachHelp("(dm-hash-add! TARGET IV) : Adds hash key of TARGET");
        ctx.defLib("dm-hash-rm!", (a, i) -> {
            ((IRIO) a).removeHashVal(dmKeyify(i));
            return null;
        }).attachHelp("(dm-hash-rm! TARGET IV) : Removes hash key of TARGET");
        // decode/encode
        ctx.defLib("dm-decode", (a) -> {
            if (a == null)
                return null;
            RORIO ro = (RORIO) a;
            switch (ro.getType()) {
            case 'T':
                return Boolean.TRUE;
            case 'F':
                return Boolean.FALSE;
            case '"':
                return ro.decString();
            case ':':
                return ro.getSymbol();
            case 'i':
                return ro.getFX();
            }
            return null;
        }).attachHelp("(dm-decode TARGET) : Converts TARGET from DM terms into Datum terms. Returns #nil on failure.");
        ctx.defLib("dm-encode", (a, v) -> {
            IRIO io = (IRIO) a;
            if (v instanceof Boolean) {
                io.setBool((Boolean) v);
            } else if (v instanceof String) {
                io.setString((String) v);
            } else if (v instanceof DatumSymbol) {
                io.setSymbol(((DatumSymbol) v).id);
            } else if (v instanceof Long) {
                io.setFX((Long) v);
            } else {
                throw new RuntimeException("Cannot convert " + v + " to DMKey");
            }
            return io;
        }).attachHelp("(dm-encode TARGET VAL) : Converts VAL from Datum terms into DM terms, returning TARGET.");
        ctx.defLib("dm-key", MVMDMLibrary::dmKeyify).attachHelp("(dm-key VAL) : Coerces VAL into a DMKey.");
    }
    private static DMKey dmKeyify(Object v) {
        if (v instanceof DMKey) {
            return (DMKey) v;
        } else if (v instanceof Boolean) {
            return DMKey.of((Boolean) v);
        } else if (v instanceof String) {
            return DMKey.ofStr((String) v);
        } else if (v instanceof DatumSymbol) {
            return DMKey.ofSym(((DatumSymbol) v).id);
        } else if (v instanceof Long) {
            return DMKey.of((Long) v);
        } else if (v instanceof RORIO) {
            return DMKey.of((RORIO) v);
        } else {
            throw new RuntimeException("Cannot convert " + v + " to DMKey");
        }
    }
    public static final class DMAt extends MVMMacro {
        public final int mode;
        private static String tName(int mode) {
            String name = "dm-at";
            if (mode == 1)
                name = "dm-add-at";
            if (mode == 2)
                name = "dm-del-at";
            return name;
        }
        public DMAt(int mode) {
            super(tName(mode));
            this.mode = mode;
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException(nameHint + " expects exactly 2 args (target path)");
            PathSyntax ps = PathSyntax.compile(cs.context, cs.compile(call[0]), MVMU.coerceToString(call[1]));
            if (mode == 1)
                return ps.addProgram;
            if (mode == 2)
                return ps.delProgram;
            return ps.getProgram;
        }
    }
}

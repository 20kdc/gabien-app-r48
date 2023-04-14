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
        ctx.defineSlot(new DatumSymbol("dm-add-at!")).v = new DMAt(1)
            .attachHelp("(dm-add-at! TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), adds entry if possible, #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-del-at!")).v = new DMAt(2)
            .attachHelp("(dm-del-at! TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), deletes entry, #nil on failure");
        // array
        ctx.defLib("dm-a-init", (a) -> {
            return ((IRIO) a).setArray();
        }).attachHelp("(dm-a-init TARGET) : TARGET.setArray");
        ctx.defLib("dm-a-init-size", (a, v) -> {
            return ((IRIO) a).setArray(MVMU.cInt(v));
        }).attachHelp("(dm-a-init-size TARGET LEN) : TARGET.setArray with specific length");
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
        ctx.defLib("dm-a-add!", (a, i) -> {
            if (a == null)
                return null;
            IRIO ro = (IRIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            return ro.addAElem(idx);
        }).attachHelp("(dm-a-add! TARGET INDEX) : Inserts an array element into TARGET. Returns #nil if not-an-array.");
        ctx.defLib("dm-a-rm!", (a, i) -> {
            if (a == null)
                return null;
            IRIO ro = (IRIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            ro.rmAElem(idx);
            return null;
        }).attachHelp("(dm-a-rm! TARGET INDEX) : Removes an array element from TARGET. Returns #nil if not-an-array.");
        // ivars
        ctx.defLib("dm-i-keys", (a) -> {
            LinkedList<String> lls = new LinkedList<>();
            if (a == null)
                return lls;
            RORIO ro = (RORIO) a;
            for (String iv : ro.getIVars())
                lls.add(iv);
            return lls;
        }).attachHelp("(dm-i-keys TARGET) : Gets ivar names of TARGET (as strings).");
        ctx.defLib("dm-i-ref", (a, i) -> {
            if (a == null)
                return null;
            return ((RORIO) a).getIVar(MVMU.coerceToString(i));
        }).attachHelp("(dm-i-ref TARGET IV) : Retrieves ivar of TARGET (IV can be string or symbol).");
        ctx.defLib("dm-i-add!", (a, i) -> {
            if (a == null)
                return null;
            return ((IRIO) a).addIVar(MVMU.coerceToString(i));
        }).attachHelp("(dm-i-add! TARGET IV) : Adds ivar of TARGET (IV can be string or symbol).");
        ctx.defLib("dm-i-rm!", (a, i) -> {
            ((IRIO) a).rmIVar(MVMU.coerceToString(i));
            return null;
        }).attachHelp("(dm-i-rm! TARGET IV) : Removes ivar of TARGET (IV can be string or symbol).");
        // hash
        ctx.defLib("dm-h-init", (a) -> {
            return ((IRIO) a).setHash();
        }).attachHelp("(dm-h-init TARGET) : TARGET.setHash");
        ctx.defLib("dm-h-keys", (a) -> {
            LinkedList<DMKey> lls = new LinkedList<>();
            if (a == null)
                return lls;
            RORIO ro = (RORIO) a;
            for (DMKey iv : ro.getHashKeys())
                lls.add(iv);
            return lls;
        }).attachHelp("(dm-h-keys TARGET) : Gets hash keys of TARGET (as DMKey).");
        ctx.defLib("dm-h-ref", (a, i) -> {
            if (a == null)
                return null;
            return ((RORIO) a).getHashVal(dmKeyify(i));
        }).attachHelp("(dm-h-ref TARGET IV) : Retrieves hash key of TARGET");
        ctx.defLib("dm-h-add!", (a, i) -> {
            if (a == null)
                return null;
            return ((IRIO) a).addHashVal(dmKeyify(i));
        }).attachHelp("(dm-h-add! TARGET IV) : Adds hash key of TARGET");
        ctx.defLib("dm-h-rm!", (a, i) -> {
            ((IRIO) a).removeHashVal(dmKeyify(i));
            return null;
        }).attachHelp("(dm-h-rm! TARGET IV) : Removes hash key of TARGET");
        // decode/encode
        ctx.defLib("dm-dec", (a) -> {
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
            case '0':
                return null;
            }
            return null;
        }).attachHelp("(dm-dec TARGET) : Converts TARGET from DM terms into Datum terms. Returns #nil on failure (or literally null).");
        ctx.defLib("dm-enc!", (a, v) -> {
            IRIO io = (IRIO) a;
            if (v == null) {
                io.setNull();
            } else if (v instanceof Boolean) {
                io.setBool((Boolean) v);
            } else if (v instanceof String) {
                io.setString((String) v);
            } else if (v instanceof DatumSymbol) {
                io.setSymbol(((DatumSymbol) v).id);
            } else if (v instanceof Long) {
                io.setFX((Long) v);
            } else if (v instanceof RORIO) {
                io.setDeepClone((RORIO) v);
            } else {
                throw new RuntimeException("Cannot convert " + v + " in dm-enc!");
            }
            return io;
        }).attachHelp("(dm-enc! TARGET VAL) : Converts VAL from Datum terms into DM terms, returning TARGET. Also acts as deep-clone.");
        ctx.defLib("dm-key", MVMDMLibrary::dmKeyify).attachHelp("(dm-key VAL) : Coerces VAL into a DMKey.");
        // theoretically could add more, but this isn't really necessary for the current role
        // mainly figure out:
        // setFloat
        // setHashWithDef
        // setArray
        // setObject
        // setUser
        // setBignum
        //  and...
        // putBuffer
        // setDeepClone
        // getBuffer
    }
    private static DMKey dmKeyify(Object v) {
        if (v == null) {
            return DMKey.NULL;
        } else if (v instanceof DMKey) {
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
                name = "dm-add-at!";
            if (mode == 2)
                name = "dm-del-at!";
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

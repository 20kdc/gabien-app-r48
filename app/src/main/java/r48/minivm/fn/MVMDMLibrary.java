/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import datum.DatumSymbol;
import datum.DatumTreeUtils;
import r48.dbs.PathSyntax;
import r48.io.data.DMChangeTracker;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM standard library.
 * Created 8th March 2023.
 */
public class MVMDMLibrary {
    public static void add(MVMEnv ctx, boolean strict) {
        // workspace
        ctx.defLib("dm-new", MVMEnvR48.IRIO_TYPE, () -> {
            return new IRIOGeneric(new DMContext(DMChangeTracker.Null.DISPOSABLE, StandardCharsets.UTF_8));
        }, "(dm-new) : Creates a disposable UTF-8 IRIOGeneric. Manipulate it however you like!");
        // equality
        ctx.defLib("dm-eq?", MVMType.BOOL, MVMEnvR48.RORIO_TYPE, MVMEnvR48.RORIO_TYPE, (a, b) -> {
            if (a == null || b == null)
                return false;
            return RORIO.rubyEquals((RORIO) a, (RORIO) b);
        }, "(dm-eq? A B) : Checks equality between two RORIOs (returning false if either are #nil)");
        // path
        ctx.defineSlot(new DatumSymbol("dm-at"), new DMAt(0, strict))
            .help("(dm-at TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-add-at!"), new DMAt(1, strict))
            .help("(dm-add-at! TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), adds entry if possible, #nil on failure");
        ctx.defineSlot(new DatumSymbol("dm-del-at!"), new DMAt(2, strict))
            .help("(dm-del-at! TARGET PATH) : Looks up PATH (must be literal PathSyntax) from TARGET (must be IRIO or #nil), deletes entry, #nil on failure");

        // path manip
        ctx.defineSlot(new DatumSymbol("ps-empty"), PathSyntax.compile(ctx, strict), MVMEnvR48.PATHSYNTAX_TYPE)
            .help("ps-empty : Empty PathSyntax, from which others may be created.");
        ctx.defLib("ps-a-ref", MVMEnvR48.PATHSYNTAX_TYPE, MVMEnvR48.PATHSYNTAX_TYPE, MVMType.I64, (p, i) -> {
            return ((PathSyntax) p).withArray(MVMU.cInt(i), "(generated)");
        }, "(ps-a-ref PATH INDEX) : Extends PathSyntax PATH with an array index operation.");
        ctx.defLib("ps-h-ref", MVMEnvR48.PATHSYNTAX_TYPE, MVMEnvR48.PATHSYNTAX_TYPE, MVMType.ANY, (p, i) -> {
            return ((PathSyntax) p).withHash(dmKeyify(i), "(generated)");
        }, "(ps-h-ref PATH INDEX) : Extends PathSyntax PATH with a hash index operation.");
        ctx.defLib("ps-i-ref", MVMEnvR48.PATHSYNTAX_TYPE, MVMEnvR48.PATHSYNTAX_TYPE, MVMType.ANY, (p, i) -> {
            return ((PathSyntax) p).withIVar(MVMU.coerceToString(i), "(generated)");
        }, "(ps-i-ref PATH INDEX) : Extends PathSyntax PATH with an ivar index operation.");
        ctx.defLib("ps-a-len", MVMEnvR48.PATHSYNTAX_TYPE, MVMEnvR48.PATHSYNTAX_TYPE, (p) -> {
            return ((PathSyntax) p).withArrayLength("(generated)");
        }, "(ps-a-len PATH) : Extends PathSyntax PATH with an array length operation.");
        ctx.defLib("ps-h-defval", MVMEnvR48.PATHSYNTAX_TYPE, MVMEnvR48.PATHSYNTAX_TYPE, (p) -> {
            return ((PathSyntax) p).withDefVal("(generated)");
        }, "(ps-h-defval PATH) : Extends PathSyntax PATH with a hash defval operation.");

        // array
        ctx.defLib("dm-a-init", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, (a) -> {
            return ((IRIO) a).setArray();
        }, "(dm-a-init TARGET) : TARGET.setArray");
        ctx.defLib("dm-a-init-size", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.I64, (a, v) -> {
            return ((IRIO) a).setArray(MVMU.cInt(v));
        }, "(dm-a-init-size TARGET LEN) : TARGET.setArray with specific length");
        ctx.defLib("dm-a-len", MVMType.I64, MVMEnvR48.RORIO_TYPE, (a) -> {
            if (a == null)
                return null;
            if (((RORIO) a).getType() != '[')
                return null;
            return (long) ((RORIO) a).getALen();
        }, "(dm-a-len TARGET) : Gets array length of TARGET. Returns #nil if not-an-array.");
        ctx.defLib("dm-a-ref", MVMEnvR48.IRIO_TYPE, MVMEnvR48.RORIO_TYPE, MVMType.I64, (a, i) -> {
            if (a == null)
                return null;
            RORIO ro = (RORIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            if (idx < 0 || idx >= ro.getALen())
                return null;
            return ro.getAElem(idx);
        }, "(dm-a-ref TARGET INDEX) : Gets an array element of TARGET. Returns #nil if not-an-array or index invalid.");
        ctx.defLib("dm-a-add!", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.I64, (a, i) -> {
            if (a == null)
                return null;
            IRIO ro = (IRIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            return ro.addAElem(idx);
        }, "(dm-a-add! TARGET INDEX) : Inserts an array element into TARGET. Returns #nil if not-an-array.");
        ctx.defLib("dm-a-rm!", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.I64, (a, i) -> {
            if (a == null)
                return null;
            IRIO ro = (IRIO) a;
            if (ro.getType() != '[')
                return null;
            int idx = MVMU.cInt(i);
            ro.rmAElem(idx);
            return null;
        }, "(dm-a-rm! TARGET INDEX) : Removes an array element from TARGET. Returns #nil if not-an-array.");
        // ivars
        ctx.defLib("dm-i-keys", new MVMType.TypedList(MVMType.STR), MVMEnvR48.RORIO_TYPE, (a) -> {
            LinkedList<String> lls = new LinkedList<>();
            if (a == null)
                return lls;
            RORIO ro = (RORIO) a;
            for (String iv : ro.getIVars())
                lls.add(iv);
            return lls;
        }, "(dm-i-keys TARGET) : Gets ivar names of TARGET (as strings).");
        ctx.defLib("dm-i-ref", MVMEnvR48.IRIO_TYPE, MVMEnvR48.RORIO_TYPE, MVMType.ANY, (a, i) -> {
            if (a == null)
                return null;
            return ((RORIO) a).getIVar(MVMU.coerceToString(i));
        }, "(dm-i-ref TARGET IV) : Retrieves ivar of TARGET (IV can be string or symbol). Passes through #nil TARGET.");
        ctx.defLib("dm-i-add!", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.ANY, (a, i) -> {
            if (a == null)
                return null;
            return ((IRIO) a).addIVar(MVMU.coerceToString(i));
        }, "(dm-i-add! TARGET IV) : Adds ivar of TARGET (IV can be string or symbol). Passes through #nil TARGET.");
        ctx.defLib("dm-i-rm!", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.ANY, (a, i) -> {
            ((IRIO) a).rmIVar(MVMU.coerceToString(i));
            return null;
        }, "(dm-i-rm! TARGET IV) : Removes ivar of TARGET (IV can be string or symbol).");
        // hash
        ctx.defLib("dm-h-init", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, (a) -> {
            return ((IRIO) a).setHash();
        }, "(dm-h-init TARGET) : TARGET.setHash");
        ctx.defLib("dm-h-keys", new MVMType.TypedList(MVMEnvR48.RORIO_TYPE), MVMEnvR48.RORIO_TYPE, (a) -> {
            LinkedList<DMKey> lls = new LinkedList<>();
            if (a == null)
                return lls;
            RORIO ro = (RORIO) a;
            for (DMKey iv : ro.getHashKeys())
                lls.add(iv);
            return lls;
        }, "(dm-h-keys TARGET) : Gets hash keys of TARGET (as DMKey).");
        ctx.defLib("dm-h-ref", MVMEnvR48.IRIO_TYPE, MVMEnvR48.RORIO_TYPE, MVMType.ANY, (a, i) -> {
            if (a == null)
                return null;
            return ((RORIO) a).getHashVal(dmKeyify(i));
        }, "(dm-h-ref TARGET IV) : Retrieves hash key of TARGET. Passes through #nil TARGET.");
        ctx.defLib("dm-h-add!", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.ANY, (a, i) -> {
            if (a == null)
                return null;
            return ((IRIO) a).addHashVal(dmKeyify(i));
        }, "(dm-h-add! TARGET IV) : Adds hash key of TARGET. Passes through #nil TARGET.");
        ctx.defLib("dm-h-rm!", MVMType.ANY, MVMEnvR48.IRIO_TYPE, MVMType.ANY, (a, i) -> {
            ((IRIO) a).removeHashVal(dmKeyify(i));
            return null;
        }, "(dm-h-rm! TARGET IV) : Removes hash key of TARGET");
        // decode/encode
        ctx.defLib("dm-dec", MVMType.ANY, MVMEnvR48.RORIO_TYPE, (a) -> {
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
                return new DatumSymbol(ro.getSymbol());
            case 'i':
                return ro.getFX();
            case '0':
                return null;
            }
            return null;
        }, "(dm-dec TARGET) : Converts TARGET from DM terms into Datum terms. Returns #nil on failure (or literally null).");
        ctx.defLib("dm-enc!", MVMEnvR48.IRIO_TYPE, MVMEnvR48.IRIO_TYPE, MVMType.ANY, (a, v) -> {
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
        }, "(dm-enc! TARGET VAL) : Converts VAL from Datum terms into DM terms, returning TARGET. Also acts as deep-clone.");
        ctx.defLib("dm-key", MVMEnvR48.RORIO_TYPE, MVMType.ANY, MVMDMLibrary::dmKeyify,
                "(dm-key VAL) : Coerces VAL into a DMKey.");
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
            return ((RORIO) v).asKey();
        } else {
            throw new RuntimeException("Cannot convert " + v + " to DMKey");
        }
    }
    public static final class DMAt extends MVMMacro {
        public final int mode;
        public final boolean strict;
        private static String tName(int mode) {
            String name = "dm-at";
            if (mode == 1)
                name = "dm-add-at!";
            if (mode == 2)
                name = "dm-del-at!";
            return name;
        }
        public DMAt(int mode, boolean strict) {
            super(tName(mode));
            this.mode = mode;
            this.strict = strict;
        }

        @Override
        public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
            if (call.length != 2)
                throw new RuntimeException(nameHint + " expects exactly 2 args (target path)");
            PathSyntax ps = PathSyntax.compile(cs.context, strict, MVMU.coerceToString(call[1]));
            MVMCExpr res;
            if (mode == 1)
                res = ps.addProgram;
            else if (mode == 2)
                res = ps.delProgram;
            else
                res = ps.getProgram;
            MVMCExpr base = cs.compile(call[0]);
            return new MVMCExpr(res.returnType) {
                @Override
                public Object execute(MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
                    // set things up for the PathSyntax ABI
                    l0 = base.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                    return res.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
                }
                
                @Override
                public Object disasm() {
                    return DatumTreeUtils.sym("dm-at-invoker");
                }
            };
        }
    }
}

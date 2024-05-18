/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumReaderTokenSource;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMToplevelScope;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.fn.MVMFn;
import r48.minivm.fn.MVMJLambdaConv;
import static gabien.datum.DatumTreeUtils.decVisitor;

/**
 * MiniVM environment.
 * Created 26th February 2023 but only fleshed out 28th.
 */
public class MVMEnv {
    /**
     * Value namespace.
     */
    private final ConcurrentHashMap<DatumSymbol, MVMSlot> values = new ConcurrentHashMap<>();
    /**
     * Separate type namespace.
     */
    private final ConcurrentHashMap<DatumSymbol, MVMType> types = new ConcurrentHashMap<>();
    private final AtomicLong gensymCounter = new AtomicLong();

    public MVMEnv() {
        defineType(new DatumSymbol("any"), MVMType.ANY);
        defineType(new DatumSymbol("obj"), MVMType.OBJ);
        defineType(new DatumSymbol("list"), MVMType.LIST);
        defineType(new DatumSymbol("i64"), MVMType.I64);
        defineType(new DatumSymbol("f64"), MVMType.F64);
        defineType(new DatumSymbol("str"), MVMType.STR);
        defineType(new DatumSymbol("sym"), MVMType.SYM);
        defineType(new DatumSymbol("bool"), MVMType.BOOL);
        defineType(new DatumSymbol("fn"), MVMType.FN);
        defineType(new DatumSymbol("num"), MVMType.NUM);
        defineType(new DatumSymbol("env"), MVMType.ENV);
        defineType(new DatumSymbol("char"), MVMType.CHAR);
    }

    public final DatumSymbol gensym() {
        return new DatumSymbol(" g" + (gensymCounter.getAndIncrement()));
    }

    /**
     * Evaluates a string (for REPL)
     */
    public Object evalString(String str) {
        return evalString(str, "REPL");
    }

    /**
     * Evaluates a string
     */
    public Object evalString(String str, String src) {
        AtomicReference<Object> ar = new AtomicReference<>();
        new DatumReaderTokenSource(src, str).visit(decVisitor((obj, srcLoc) -> {
            ar.set(evalObject(obj, srcLoc));
        }));
        return ar.get();
    }

    /**
     * Evaluates an object
     */
    public Object evalObject(Object obj, DatumSrcLoc srcLoc) {
        try {
            MVMCompileScope mcs = new MVMToplevelScope(this, srcLoc);
            MVMCExpr exp = mcs.compile(obj);
            return exp.exc(MVMScope.ROOT);
        } catch (Exception ex) {
            throw new RuntimeException("at " + srcLoc, ex);
        }
    }

    /**
     * Attempts to parse a type.
     */
    public @NonNull MVMType getType(Object type, String name) {
        if (type instanceof DatumSymbol) {
            MVMType mt = types.get((DatumSymbol) type);
            if (mt == null)
                throw new RuntimeException("Unknown type: " + type);
            return mt;
        } else if (type instanceof List) {
            List<?> tdef = (List<?>) type;
            if (tdef.size() > 0) {
                Object inst = tdef.get(0);
                if (inst instanceof DatumSymbol) {
                    String cmd = ((DatumSymbol) inst).id;
                    if (cmd.equals("S") && tdef.size() == 2)
                        return new MVMType.Subtype(getType(tdef.get(1), name + "-base"), name);
                }
            }
        }
        throw new RuntimeException("Cannot parse type: " + DatumWriter.objectToString(type));
    }

    public void defineType(DatumSymbol d, MVMType type) {
        types.put(d, type);
    }

    public @Nullable MVMSlot getSlot(DatumSymbol d) {
        return values.get(d);
    }

    /**
     * Ensures a slot is present (like define does)
     */
    public @NonNull MVMSlot ensureSlot(DatumSymbol d) {
        MVMSlot res = getSlot(d);
        if (res != null)
            return res;
        return defineSlot(d);
    }

    /**
     * Beware: This defines the slot "here and now", with shadowing, which is not how (define) works. Use ensureSlot.
     */
    public @NonNull MVMSlot defineSlot(DatumSymbol d) {
        return defineSlot(d, null, MVMType.ANY);
    }

    /**
     * Beware: This defines the slot "here and now", with shadowing, which is not how (define) works. Use ensureSlot.
     */
    public @NonNull MVMSlot defineSlot(DatumSymbol d, Object v) {
        return defineSlot(d, v, MVMType.typeOf(v));
    }

    /**
     * Beware: This defines the slot "here and now", with shadowing, which is not how (define) works. Use ensureSlot.
     */
    public @NonNull MVMSlot defineSlot(DatumSymbol d, Object v, MVMType t) {
        if (values.containsKey(d))
            throw new RuntimeException("Cannot redefine: " + d);
        MVMSlot s = new MVMSlot(d, t);
        s.v = v;
        values.put(d, s);
        return s;
    }

    public Collection<MVMSlot> listSlots() {
        return values.values();
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMType rt, Supplier<Object> fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, rt, fn);
        defineSlot(new DatumSymbol(s), f2);
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMType rt, MVMType t0, Function<Object, Object> fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, rt, t0, fn);
        defineSlot(new DatumSymbol(s), f2);
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMType rt, MVMType t0, MVMType t1, MVMJLambdaConv.F2 fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, rt, t0, t1, fn);
        defineSlot(new DatumSymbol(s), f2);
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMType rt, MVMType t0, MVMType t1, MVMType t2, MVMJLambdaConv.F3 fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, rt, t0, t1, t2, fn);
        defineSlot(new DatumSymbol(s), f2);
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMType rt, MVMType t0, MVMType t1, MVMType t2, MVMType t3, MVMJLambdaConv.F4 fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, rt, t0, t1, t2, t3, fn);
        defineSlot(new DatumSymbol(s), f2);
        return f2;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumReaderTokenSource;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.uslx.append.IFunction;
import gabien.uslx.append.ISupplier;
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
    private final @Nullable MVMEnv parent;
    private final HashMap<DatumSymbol, MVMSlot> values = new HashMap<>();
    private final AtomicLong gensymCounter;

    public MVMEnv() {
        parent = null;
        gensymCounter = new AtomicLong();
    }

    protected MVMEnv(MVMEnv p) {
        parent = p;
        gensymCounter = p.gensymCounter;
    }

    public MVMEnv extend() {
        return new MVMEnv(this);
    }

    public final DatumSymbol gensym() {
        return new DatumSymbol(" g" + (gensymCounter.getAndIncrement()));
    }

    /**
     * Evaluates a string (for REPL)
     */
    public Object evalString(String str) {
        AtomicReference<Object> ar = new AtomicReference<>();
        new DatumReaderTokenSource("REPL", str).visit(decVisitor((obj, srcLoc) -> {
            ar.set(evalObject(obj, srcLoc));
        }));
        return ar.get();
    }

    /**
     * Evaluates an object
     */
    public Object evalObject(Object obj, DatumSrcLoc srcLoc) {
        MVMCompileScope mcs = new MVMToplevelScope(this, srcLoc);
        MVMCExpr exp = mcs.compile(obj);
        return exp.exc(MVMScope.ROOT);
    }

    public @Nullable MVMSlot getSlot(DatumSymbol d) {
        if (parent != null) {
            MVMSlot at = parent.getSlot(d);
            if (at != null)
                return at;
        }
        return values.get(d);
    }

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
        if (values.containsKey(d))
            throw new RuntimeException("Cannot redefine: " + d);
        MVMSlot s = new MVMSlot(d);
        values.put(d, s);
        return s;
    }

    public Collection<MVMSlot> listSlots() {
        return values.values();
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, ISupplier<Object> fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, fn);
        defineSlot(new DatumSymbol(s)).v = f2;
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, IFunction<Object, Object> fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, fn);
        defineSlot(new DatumSymbol(s)).v = f2;
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMJLambdaConv.F2 fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, fn);
        defineSlot(new DatumSymbol(s)).v = f2;
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMJLambdaConv.F3 fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, fn);
        defineSlot(new DatumSymbol(s)).v = f2;
        return f2;
    }

    /**
     * Quickly defines a library function.
     */
    public MVMFn defLib(String s, MVMJLambdaConv.F4 fn) {
        MVMFn f2 = MVMJLambdaConv.c(s, fn);
        defineSlot(new DatumSymbol(s)).v = f2;
        return f2;
    }
}

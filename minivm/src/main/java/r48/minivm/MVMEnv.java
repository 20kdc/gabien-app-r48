/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import static gabien.datum.DatumTreeUtils.sym;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumDecodingVisitor;
import gabien.datum.DatumReaderTokenSource;
import gabien.datum.DatumSymbol;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.compiler.MVMToplevelScope;
import r48.minivm.expr.MVMCExpr;

/**
 * MiniVM environment.
 * Created 26th February 2023 but only fleshed out 28th.
 */
public class MVMEnv {
    private final @Nullable MVMEnv parent;
    private final HashMap<DatumSymbol, Slot> values = new HashMap<>();
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
        DatumDecodingVisitor ddv = new DatumDecodingVisitor() {
            @Override
            public void visitTree(Object obj) {
                ar.set(evalObject(obj));
            }
            @Override
            public void visitEnd() {
            }
        };
        new DatumReaderTokenSource(str).visit(ddv);
        return ar.get();
    }

    /**
     * Evaluates an object
     */
    public Object evalObject(Object obj) {
        MVMCompileScope mcs = new MVMToplevelScope(this);
        MVMCExpr exp = mcs.compile(obj);
        return exp.exc(MVMScope.ROOT);
    }

    public @Nullable Slot getSlot(DatumSymbol d) {
        if (parent != null) {
            Slot at = parent.getSlot(d);
            if (at != null)
                return at;
        }
        return values.get(d);
    }

    public @NonNull Slot ensureSlot(DatumSymbol d) {
        Slot res = getSlot(d);
        if (res != null)
            return res;
        return defineSlot(d);
    }

    public @NonNull Slot defineSlot(DatumSymbol d) {
        if (values.containsKey(d))
            throw new RuntimeException("Cannot redefine: " + d);
        Slot s = new Slot(d);
        values.put(d, s);
        return s;
    }

    public Collection<Slot> listSlots() {
        return values.values();
    }

    /**
     * A Slot represents a stored value in the context.
     * Slots are also the expressions for retrieving them for execution efficiency reasons.
     */
    public final static class Slot extends MVMCExpr {
        public final DatumSymbol s;
        public Object v;

        public Slot(DatumSymbol s) {
            this.s = s;
        }

        @Override
        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
            return v;
        }

        @Override
        public Object disasm() {
            return Arrays.asList(sym("slot"), s);
        }
    }
}

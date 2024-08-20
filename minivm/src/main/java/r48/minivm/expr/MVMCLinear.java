/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

import datum.DatumTreeUtils;
import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * Linear expressions are expressions which operate on a single value in a series of otherwise-independent steps.
 * This can allow the linear expression to be single-stepped in cases where inspection is required.
 * Created 20th August, 2024.
 */
public class MVMCLinear extends MVMCExpr {
    public final MVMCExpr source;
    public final Step[] steps;

    private static MVMType getReturnTypeOf(MVMType inputType, Step[] steps) {
        for (Step v : steps)
            inputType = v.getTypeForInput(inputType);
        return inputType;
    }

    public MVMCLinear(MVMCExpr source, Step... steps) {
        super(getReturnTypeOf(source.returnType, steps));
        this.source = source;
        this.steps = steps;
    }

    @Override
    public Object disasm() {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(DatumTreeUtils.sym("linear"));
        ll.add(source.disasm());
        for (Step v : steps)
            ll.add(v.disasm());
        return ll;
    }

    @Override
    public Object execute(MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        Object value = source.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        for (Step v : steps)
            value = v.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7, value);
        return value;
    }

    /**
     * Executes the sequence, returning an array for all the steps.
     * Make sure this is what you want, because it's (relatively) costly (allocation)
     */
    public Object[] executeWithIntrospection(MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7) {
        Object[] data = new Object[steps.length + 1];
        data[0] = source.execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7);
        for (int i = 0; i < steps.length; i++)
            data[i + 1] = steps[i].execute(ctx, l0, l1, l2, l3, l4, l5, l6, l7, data[i]);
        return data;
    }

    public static abstract class Step {
        /**
         * Get the output type for the input type.
         * If the type is incompatible, throws!
         */
        public abstract MVMType getTypeForInput(MVMType inputType);

        public abstract Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7, Object input);

        public abstract Object disasm();
    }

    /**
     * Constant. Overrides the result. Side-effects still execute.
     */
    public static class Const extends Step {
        public final Object value;
        public final MVMType type;
        public Const(Object v, MVMType type) {
            value = v;
            this.type = type;
        }
        @Override
        public MVMType getTypeForInput(MVMType inputType) {
            return type;
        }
        @Override
        public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7, Object value) {
            // override it.
            return this.value;
        }
        @Override
        public Object disasm() {
            return MVMU.userStr(value);
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv;

/**
 * MiniVM standard library.
 * Created 1st March 2023.
 */
public class MVMMathsLibrary {
    private static final Long resM1 = -1L;
    private static final Long res0 = 0L;
    private static final Long res1 = 1L;
    public static void add(MVMEnv ctx) {
        // Scheme library
        ctx.defineSlot(new DatumSymbol("+")).v = new Add()
                .attachHelp("(+ V...) : Adds various values. If none given, returns 0.");
        ctx.defineSlot(new DatumSymbol("number-compare")).v = new MVMFn.Fixed("number-compare") {
            @Override
            public Object callDirect(Object a0, Object a1) {
                int res;
                if (a0 instanceof Float || a0 instanceof Double || a1 instanceof Float || a1 instanceof Double)
                    res = Double.compare(((Number) a0).doubleValue(), ((Number) a1).doubleValue());
                else
                    res = Long.compare(((Number) a0).longValue(), ((Number) a1).longValue());
                if (res < 0)
                    return resM1;
                else if (res > 0)
                    return res1;
                else
                    return res0;
            }
        }
                .attachHelp("(number-compare A B) : Compares two numbers, returning integers -1 (A < B) to 1 (A > B).");
    }

    public static final class Add extends MVMFn.ChainOp<Number> {
        public Add() {
            super("+");
        }

        @Override
        public Number checkParticipant(Object v) {
            return (Number) v;
        }

        @Override
        public Object loneOp(Number a) {
            return a;
        }

        @Override
        public Number twoOp(Number a, Number b) {
            if (a instanceof Double || b instanceof Double || a instanceof Float || b instanceof Float)
                return a.doubleValue() + b.doubleValue();
            return a.longValue() + b.longValue();
        }

        @Override
        protected Object callDirect() {
            return 0L;
        }
    }
}

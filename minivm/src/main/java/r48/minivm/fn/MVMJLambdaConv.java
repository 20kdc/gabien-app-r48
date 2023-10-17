/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Conversions.
 * Created 10th March 2023.
 */
public class MVMJLambdaConv {
    public static MVMFn c(String s, Supplier<Object> fn) {
        return new MVMFn.Fixed(s) {
            @Override
            public Object callDirect() {
                return fn.get();
            }
        };
    }
    public static MVMFn c(String s, Function<Object, Object> fn) {
        return new MVMFn.Fixed(s) {
            @Override
            public Object callDirect(Object a0) {
                return fn.apply(a0);
            }
        };
    }
    public static MVMFn c(String s, F2 fn) {
        return new MVMFn.Fixed(s) {
            @Override
            public Object callDirect(Object a0, Object a1) {
                return fn.apply(a0, a1);
            }
        };
    }
    public static MVMFn c(String s, F3 fn) {
        return new MVMFn.Fixed(s) {
            @Override
            public Object callDirect(Object a0, Object a1, Object a2) {
                return fn.apply(a0, a1, a2);
            }
        };
    }
    public static MVMFn c(String s, F4 fn) {
        return new MVMFn.Fixed(s) {
            @Override
            public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
                return fn.apply(a0, a1, a2, a3);
            }
        };
    }

    public interface F2 {
        Object apply(Object a0, Object a1);
    }

    public interface F3 {
        Object apply(Object a0, Object a1, Object a2);
    }

    public interface F4 {
        Object apply(Object a0, Object a1, Object a2, Object a3);
    }
}

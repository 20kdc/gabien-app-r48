/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.function.Function;
import java.util.function.Supplier;

import r48.minivm.MVMType;

/**
 * Conversions.
 * Created 10th March 2023.
 */
public class MVMJLambdaConv {
    public static MVMFn c(String s, MVMType rt, Supplier<Object> fn) {
        MVMType.Fn ft = new MVMType.Fn(rt, 0, new MVMType[] {}, null);
        return new MVMFn.Fixed(ft, s) {
            @Override
            public Object callDirect() {
                return fn.get();
            }
        };
    }
    public static MVMFn c(String s, MVMType rt, MVMType t0, Function<Object, Object> fn) {
        MVMType.Fn ft = new MVMType.Fn(rt, 1, new MVMType[] {t0}, null);
        return new MVMFn.Fixed(ft, s) {
            @Override
            public Object callDirect(Object a0) {
                return fn.apply(a0);
            }
        };
    }
    public static MVMFn c(String s, MVMType rt, MVMType t0, MVMType t1, F2 fn) {
        MVMType.Fn ft = new MVMType.Fn(rt, 2, new MVMType[] {t0, t1}, null);
        return new MVMFn.Fixed(ft, s) {
            @Override
            public Object callDirect(Object a0, Object a1) {
                return fn.apply(a0, a1);
            }
        };
    }
    public static MVMFn c(String s, MVMType rt, MVMType t0, MVMType t1, MVMType t2, F3 fn) {
        MVMType.Fn ft = new MVMType.Fn(rt, 3, new MVMType[] {t0, t1, t2}, null);
        return new MVMFn.Fixed(ft, s) {
            @Override
            public Object callDirect(Object a0, Object a1, Object a2) {
                return fn.apply(a0, a1, a2);
            }
        };
    }
    public static MVMFn c(String s, MVMType rt, MVMType t0, MVMType t1, MVMType t2, MVMType t3, F4 fn) {
        MVMType.Fn ft = new MVMType.Fn(rt, 4, new MVMType[] {t0, t1, t2, t3}, null);
        return new MVMFn.Fixed(ft, s) {
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

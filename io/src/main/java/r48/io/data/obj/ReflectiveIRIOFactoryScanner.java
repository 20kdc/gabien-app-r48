/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data.obj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.io.data.DMContext;
import r48.io.data.IRIO;

/**
 * Reflection logic behind factory.
 * Creates the factory lambdas used to fill object fields.
 * Created 8th May, 2024.
 */
class ReflectiveIRIOFactoryScanner {
    @SuppressWarnings("unchecked")
    public static @Nullable Consumer<IRIO> createIVarAddFor(@NonNull Field f) {
        try {
            Field m = null;
            try {
                // find name-bound lambda
                m = f.getDeclaringClass().getDeclaredField(f.getName() + "_add");
            } catch (Exception e2) {
                // oh well
            }
            if (m != null) {
                // if we got this far, it's intended
                if (!Modifier.isStatic(m.getModifiers()))
                    throw new RuntimeException("Name-bound lambda " + f.getDeclaringClass() + "." + m.getName() + " is supposed to be static, and it isn't.");
                return (Consumer<IRIO>) m.get(null);
            }
            Method m2 = null;
            try {
                // find name-bound method
                m2 = f.getDeclaringClass().getDeclaredMethod(f.getName() + "_add");
            } catch (Exception e2) {
                // oh well
            }
            if (m2 != null) {
                // if we got this far, it's intended
                final Method m2f = m2;
                return (v) -> {
                    try {
                        m2f.invoke(v);
                    } catch (Exception e) {
                        throw new RuntimeException("At field: " + f, e);
                    }
                };
            }
            // -- no name-bound lambda factory, trying annotation factories --
            Function<DMContext, Object> factory = createFactoryFor(f);
            if (factory == null)
                return null;
            return (obj) -> {
                try {
                    f.set(obj, factory.apply(obj.context));
                } catch (Exception e) {
                    throw new RuntimeException("At field: " + f, e);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("At field: " + f, e);
        }
    }
    private static @Nullable Function<DMContext, Object> createFactoryFor(@NonNull Field f) throws NoSuchMethodException {
        final DMCXSupplier fxd = f.getAnnotation(DMCXSupplier.class);
        DMCXInteger fxi = f.getAnnotation(DMCXInteger.class);
        DMCXBoolean fxb = f.getAnnotation(DMCXBoolean.class);
        boolean fxo = f.isAnnotationPresent(DMCXObject.class);
        return createFactoryFor(f, f.getType(), fxd, fxi, fxb, fxo);
    }
    private static @Nullable Function<DMContext, Object> createFactoryFor(Object errorCtx, Class<?> f, @Nullable DMCXSupplier fxd, @Nullable DMCXInteger fxi, @Nullable DMCXBoolean fxb, boolean fxo) throws NoSuchMethodException {
        if (fxd != null) {
            Function<DMContext, Object> innerFactory = createFactoryFor(errorCtx, fxd.value(), null, null, null, true);
            if (innerFactory == null)
                throw new RuntimeException("innerFactory returned null for interior value somehow (target " + fxd.value() + ")");
            Constructor<?> constructor = f.getConstructor(DMContext.class, Supplier.class);
            return (context) -> {
                try {
                    Supplier<?> outerFactory = () -> innerFactory.apply(context);
                    return constructor.newInstance(context, outerFactory);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        if (fxi != null) {
            int value = fxi.value();
            Constructor<?> constructor = f.getConstructor(DMContext.class, int.class);
            return (context) -> {
                try {
                    return constructor.newInstance(context, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        if (fxb != null) {
            boolean value = fxb.value();
            Constructor<?> constructor = f.getConstructor(DMContext.class, boolean.class);
            return (context) -> {
                try {
                    return constructor.newInstance(context, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        if (fxo) {
            Constructor<?> constructor = f.getConstructor(DMContext.class);
            return (context) -> {
                try {
                    return constructor.newInstance(context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return null;
    }
}

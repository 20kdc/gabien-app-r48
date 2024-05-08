/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data.obj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.io.data.DMContext;

/**
 * Implementation detail.
 * Creates the factory lambdas used to fill object fields.
 * Created 8th May, 2024.
 */
class DMFactory {
    public static @Nullable Function<DMContext, Object> createFactoryFor(@NonNull Field f) {
        try {
            final DMCXSupplier fxd = f.getAnnotation(DMCXSupplier.class);
            DMCXInteger fxi = f.getAnnotation(DMCXInteger.class);
            DMCXBoolean fxb = f.getAnnotation(DMCXBoolean.class);
            boolean fxo = f.isAnnotationPresent(DMCXObject.class);
            return createFactoryFor(f, f.getType(), fxd, fxi, fxb, fxo);
        } catch (Exception e) {
            throw new RuntimeException("At field: " + f, e);
        }
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
                    throw new RuntimeException("At field: " + errorCtx, e);
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
                    throw new RuntimeException("At field: " + errorCtx, e);
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
                    throw new RuntimeException("At field: " + errorCtx, e);
                }
            };
        }
        if (fxo) {
            Constructor<?> constructor = f.getConstructor(DMContext.class);
            return (context) -> {
                try {
                    return constructor.newInstance(context);
                } catch (Exception e) {
                    throw new RuntimeException("At field: " + errorCtx, e);
                }
            };
        }
        return null;
    }
}

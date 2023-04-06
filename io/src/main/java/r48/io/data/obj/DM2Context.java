/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data.obj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import gabien.uslx.append.ISupplier;
import r48.io.data.IRIO;

/**
 * Responsible for setting up initialization of DM2 fields.
 * Created 5th April 2023 in response to needing to shuffle data around.
 */
public class DM2Context {
    public final Charset encoding;

    public DM2Context(Charset cs) {
        encoding = cs;
    }

    /**
     * Create an object based on a field definition. Does not actually set that field.
     */
    public Object createObjectFor(final Field f) {
        try {
            final DM2CXSupplier fxd = f.getAnnotation(DM2CXSupplier.class);
            if (fxd != null) {
                Object i = f.getType().getConstructor(ISupplier.class).newInstance((ISupplier<IRIO>) () -> {
                    try {
                        return (IRIO) createObjectOfClass(f, fxd.value());
                    } catch (Exception e) {
                        throw new RuntimeException("At field: " + f, e);
                    }
                });
                return i;
            }
            DMCXInteger fxi = f.getAnnotation(DMCXInteger.class);
            if (fxi != null) {
                Object i = f.getType().getConstructor(int.class).newInstance(fxi.value());
                return i;
            }
            DMCXBoolean fxb = f.getAnnotation(DMCXBoolean.class);
            if (fxb != null) {
                Object i = f.getType().getConstructor(boolean.class).newInstance(fxb.value());
                return i;
            }
            if (f.isAnnotationPresent(DMCXObject.class)) {
                return createObjectOfClass(f, f.getType());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("At field: " + f, e);
        }
    }

    private Constructor<?> gcon(Class<?> c, Class<?>... classes) {
        try {
            return c.getConstructor(classes);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Create an object by class without further parameterization. Field is kept solely for reference.
     */
    public Object createObjectOfClass(final Field f, Class<?> c) {
        Constructor<?> ctx = gcon(c, DM2Context.class);
        try {
            if (ctx != null)
                return ctx.newInstance(this);
            return c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("At field: " + f, e);
        }
    }
}

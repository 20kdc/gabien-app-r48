/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.append.Entity;
import r48.io.data.obj.DM2CXSupplier;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;

/**
 * Responsible for setting up initialization of DM2 fields.
 * Created 5th April 2023 in response to needing to shuffle data around.
 * Moved to EntityType and renamed DMContext 8th May 2024.
 */
public final class DMContext extends Entity<DMContext> {
    public static final Entity.Registrar<DMContext> I = newRegistrar();

    public final @NonNull IDMChangeTracker changes;
    public final Charset encoding;

    public DMContext(@NonNull IDMChangeTracker changes, @NonNull Charset encoding) {
        this.changes = changes;
        this.encoding = encoding;
    }

    /**
     * Create an object based on a field definition. Does not actually set that field.
     */
    public Object createObjectFor(final Field f) {
        try {
            final DM2CXSupplier fxd = f.getAnnotation(DM2CXSupplier.class);
            if (fxd != null) {
                Object i = f.getType().getConstructor(DMContext.class, Supplier.class).newInstance(this, (Supplier<IRIO>) () -> {
                    try {
                        return (IRIO) createFactoryForClass(f, fxd.value()).get();
                    } catch (Exception e) {
                        throw new RuntimeException("At field: " + f, e);
                    }
                });
                return i;
            }
            DMCXInteger fxi = f.getAnnotation(DMCXInteger.class);
            if (fxi != null) {
                Object i = f.getType().getConstructor(DMContext.class, int.class).newInstance(this, fxi.value());
                return i;
            }
            DMCXBoolean fxb = f.getAnnotation(DMCXBoolean.class);
            if (fxb != null) {
                Object i = f.getType().getConstructor(DMContext.class, boolean.class).newInstance(this, fxb.value());
                return i;
            }
            if (f.isAnnotationPresent(DMCXObject.class)) {
                Object i = f.getType().getConstructor(DMContext.class).newInstance(this);
                return i;
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
    public Supplier<?> createFactoryForClass(final Object at, Class<?> c) {
        try {
            final Constructor<?> ctx = gcon(c, DMContext.class);
            if (ctx != null)
                return () -> {
                    try {
                        return ctx.newInstance(DMContext.this);
                    } catch (Exception e) {
                        throw new RuntimeException("At factory: " + at, e);
                    }
                };
            final Constructor<?> nv = gcon(c);
            if (nv != null)
                return () -> {
                    try {
                        return nv.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("At factory: " + at, e);
                    }
                };
                throw new Exception("Unable to find a suitable factory constructor");
        } catch (Exception e) {
            throw new RuntimeException("While preparing factory: " + at, e);
        }
    }

    public static final class Key<T> extends Entity.Key<DMContext, T> {
        public Key() {
            super(I);
        }
    }
}

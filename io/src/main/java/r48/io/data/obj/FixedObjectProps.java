/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.io.data.obj;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.io.data.IRIO;

/**
 * Contains reflected property data.
 * Created 8th May, 2024.
 */
public final class FixedObjectProps {
    private final static ConcurrentHashMap<Class<?>, FixedObjectProps> cache = new ConcurrentHashMap<>();
    private final HashMap<String, FXOBinding> fxoBindings = new HashMap<>();
    private final HashMap<String, Consumer<IRIO>> fieldNameToIVarAdd = new HashMap<>();
    public final FXOBinding[] fxoBindingsArray;
    public final Field[] fieldsArray;

    public FixedObjectProps(Class<?> clazz) {
        fieldsArray = clazz.getFields();
        LinkedList<FXOBinding> bindings = new LinkedList<>();
        for (Field f : fieldsArray) {
            // factory
            Consumer<IRIO> factory = ReflectiveIRIOFactoryScanner.createIVarAddFor(f);
            if (factory != null)
                fieldNameToIVarAdd.put(f.getName(), factory);
            // fxo
            DMFXOBinding dmx = f.getAnnotation(DMFXOBinding.class);
            if (dmx != null) {
                // by keeping this 'use-case' invalid it should simplify things down the line
                if (factory == null)
                    throw new RuntimeException("Property " + dmx.value() + " of " + clazz + " has an FXO binding without a factory.");
                FXOBinding res = new FXOBinding(f, dmx.value(), factory);
                bindings.add(res);
                fxoBindings.put(res.iVar, res);
            }
        }
        fxoBindingsArray = bindings.toArray(new FXOBinding[0]);
    }

    /**
     * gets FXOBinding efficiently by iVar
     */
    public @Nullable FXOBinding byIVar(String iVar) {
        return fxoBindings.get(iVar);
    }

    public @Nullable Consumer<IRIO> iVarAddByFieldName(String field) {
        return fieldNameToIVarAdd.get(field);
    }

    public static FixedObjectProps forClass(Class<?> clazz) {
        FixedObjectProps data = cache.get(clazz);
        if (data == null) {
            data = new FixedObjectProps(clazz);
            cache.put(clazz, data);
        }
        return data;
    }

    public static final class FXOBinding {
        /**
         * instance variable for this binding
         */
        public final @NonNull String iVar;

        /**
         * field it is bound to
         */
        public final @NonNull Field field;

        /**
         * addIVar handler for this FXO, assuming all wrapping has been dealt with
         */
        public final @Nullable Consumer<IRIO> iVarAdd;

        /**
         * if the instance variable is optional
         */
        public final boolean optional;

        public FXOBinding(@NonNull Field field, @NonNull String prop, @Nullable Consumer<IRIO> iva) {
            this.field = field;
            this.iVar = prop;
            this.iVarAdd = iva;
            optional = field.isAnnotationPresent(DMOptional.class);
        }
    }
}

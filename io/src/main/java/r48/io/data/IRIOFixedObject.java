/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An IRIO describing a fixed-layout object.
 * Created on November 22, 2018.
 */
public abstract class IRIOFixedObject extends IRIOFixed {
    private final String objType;
    private final static ConcurrentHashMap<Class<?>, Field[]> classFields = new ConcurrentHashMap<>();
    protected final Field[] cachedFields;
    public final DM2Context context;

    public IRIOFixedObject(DM2Context ctx, String sym) {
        super('o');
        context = ctx;
        objType = sym;
        Class<?> c = getClass();
        Field[] data = classFields.get(c);
        if (data != null) {
            cachedFields = data;
        } else {
            cachedFields = c.getFields();
            classFields.put(c, cachedFields);
        }
        initialize();
    }

    @Override
    public IRIO setObject(String symbol) {
        if (!symbol.equals(objType))
            return super.setObject(symbol);
        initialize();
        return this;
    }

    protected void initialize() {
        try {
            for (Field f : cachedFields) {
                DM2FXOBinding dmx = f.getAnnotation(DM2FXOBinding.class);
                if (dmx != null) {
                    if (!f.isAnnotationPresent(DM2Optional.class)) {
                        addIVar(dmx.value());
                    } else {
                        f.set(this, null);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSymbol() {
        return objType;
    }

    @Override
    public String[] getIVars() {
        LinkedList<String> s = new LinkedList<String>();
        for (Field f : cachedFields) {
            DM2FXOBinding dmx = f.getAnnotation(DM2FXOBinding.class);
            if (dmx != null) {
                if (f.isAnnotationPresent(DM2Optional.class)) {
                    try {
                        if (f.get(this) == null)
                            continue;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                s.add(dmx.value());
            }
        }
        return s.toArray(new String[0]);
    }

    @Override
    public IRIO getIVar(String sym) {
        for (Field f : cachedFields) {
            DM2FXOBinding dmx = f.getAnnotation(DM2FXOBinding.class);
            if (dmx != null) {
                if (dmx.value().equals(sym)) {
                    try {
                        return (IRIO) f.get(this);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void rmIVar(String sym) {
        for (Field f : cachedFields) {
            DM2FXOBinding dmx = f.getAnnotation(DM2FXOBinding.class);
            if (dmx != null) {
                if (dmx.value().equals(sym) && f.isAnnotationPresent(DM2Optional.class)) {
                    try {
                        f.set(this, null);
                        return;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        super.rmIVar(sym);
    }
}

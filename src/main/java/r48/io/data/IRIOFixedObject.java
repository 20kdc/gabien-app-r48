/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
    private final static ConcurrentHashMap<Class, Field[]> classFields = new ConcurrentHashMap<Class, Field[]>();
    protected final Field[] cachedFields;

    public IRIOFixedObject(String sym) {
        super('o');
        objType = sym;
        Class c = getClass();
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
                    if (!dmx.optional()) {
                        addIVar(dmx.iVar());
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
                if (dmx.optional()) {
                    try {
                        if (f.get(this) == null)
                            continue;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                s.add(dmx.iVar());
            }
        }
        return s.toArray(new String[0]);
    }

    @Override
    public IRIO getIVar(String sym) {
        for (Field f : cachedFields) {
            DM2FXOBinding dmx = f.getAnnotation(DM2FXOBinding.class);
            if (dmx != null) {
                if (dmx.iVar().equals(sym)) {
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
                if (dmx.optional() && dmx.iVar().equals(sym)) {
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

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data.obj;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.function.Consumer;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedData;
import r48.io.data.obj.FixedObjectProps.FXOBinding;

/**
 * An IRIO describing a fixed-layout object.
 * Created on November 22, 2018.
 */
public abstract class IRIOFixedObject extends IRIOFixedData {
    public final String objType;
    protected final FixedObjectProps cachedFields;

    public IRIOFixedObject(DMContext ctx, String sym) {
        super(ctx, 'o');
        objType = sym;
        Class<?> c = getClass();
        cachedFields = FixedObjectProps.forClass(c);
        initialize();
    }

    @Override
    public Runnable saveState() {
        final Object myClone;
        try {
            myClone = clone();
        } catch (Exception ex) {
            // impossible as Cloneable is implemented here, right?
            throw new RuntimeException(ex);
        }
        return () -> {
            try {
                for (Field f : FixedObjectProps.forClass(IRIOFixedObject.this.getClass()).fieldsArray) {
                    if ((f.getModifiers() & Modifier.FINAL) != 0)
                        continue;
                    f.set(IRIOFixedObject.this, f.get(myClone));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @Override
    public IRIO setObject(String symbol) {
        if (!symbol.equals(objType))
            return super.setObject(symbol);
        reAddAllIVars();
        return this;
    }

    /**
     * This resets the contents of all FXO bindings.
     */
    protected final void reAddAllIVars() {
        try {
            for (FXOBinding f : cachedFields.fxoBindingsArray) {
                if (!f.optional) {
                    addIVar(f.iVar);
                } else {
                    trackingWillChange();
                    f.field.set(this, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initialize() {
        reAddAllIVars();
    }

    @Override
    public String getSymbol() {
        return objType;
    }

    @Override
    public String[] getIVars() {
        LinkedList<String> s = new LinkedList<String>();
        for (FXOBinding f : cachedFields.fxoBindingsArray) {
            if (f.optional) {
                try {
                    if (f.field.get(this) == null)
                        continue;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            s.add(f.iVar);
        }
        return s.toArray(new String[0]);
    }

    @Override
    public IRIO getIVar(String sym) {
        FXOBinding f = cachedFields.byIVar(sym);
        if (f != null) {
            try {
                return (IRIO) f.field.get(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * This should only be overridden by IRIOFixedObjectPacked, or if it is overridden otherwise, with great care.
     * Mainly because trackingWillChange() will need to be called when relevant.
     */
    @Override
    public IRIO addIVar(String sym) {
        FXOBinding f = cachedFields.byIVar(sym);
        if (f != null)
            return (IRIO) addFieldImpl(f.field, f.iVarAdd);
        return null;
    }

    /**
     * This function is like addIVar, but it works by Field.
     * This should only be overridden by IRIOFixedObjectPacked, or if it is overridden otherwise, with great care.
     * Mainly because trackingWillChange() will need to be called when relevant.
     */
    public Object addField(Field f) {
        return addFieldImpl(f, cachedFields.iVarAddByFieldName(f.getName()));
    }

    // Must not handle translation into addIVar because it's called from there.
    private Object addFieldImpl(Field f, Consumer<IRIO> factory) {
        if (factory == null)
            return null;
        trackingWillChange();
        factory.accept(this);
        try {
            Object res = f.get(this);
            if (res == null)
                throw new RuntimeException("Factory did not actually set a non-null value");
            return res;
        } catch (Exception ex) {
            throw new RuntimeException("At field: " + f, ex);
        }
    }

    @Override
    public void rmIVar(String sym) {
        FXOBinding f = cachedFields.byIVar(sym);
        if (f != null) {
            if (f.optional) {
                trackingWillChange();
                try {
                    f.field.set(this, null);
                    return;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.rmIVar(sym);
    }
}

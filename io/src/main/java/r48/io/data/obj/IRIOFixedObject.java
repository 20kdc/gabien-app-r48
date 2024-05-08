/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data.obj;

import java.util.LinkedList;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;
import r48.io.data.obj.FixedObjectProps.FXOBinding;

/**
 * An IRIO describing a fixed-layout object.
 * Created on November 22, 2018.
 */
public abstract class IRIOFixedObject extends IRIOFixed {
    private final String objType;
    protected final FixedObjectProps cachedFields;
    public final DMContext dm2Ctx;

    public IRIOFixedObject(DMContext ctx, String sym) {
        super(ctx, 'o');
        dm2Ctx = ctx;
        objType = sym;
        Class<?> c = getClass();
        cachedFields = FixedObjectProps.forClass(c);
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
            for (FXOBinding f : cachedFields.fxoBindingsArray) {
                if (!f.optional) {
                    addIVar(f.iVar);
                } else {
                    f.field.set(this, null);
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

    @Override
    public void rmIVar(String sym) {
        FXOBinding f = cachedFields.byIVar(sym);
        if (f != null) {
            if (f.optional) {
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

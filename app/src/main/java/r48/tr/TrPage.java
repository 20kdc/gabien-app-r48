/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv.Slot;
import r48.minivm.MVMEnvR48;

/**
 * Translation page base.
 * Created 2nd March 2023.
 */
public class TrPage {
    public final void fillFromVM(MVMEnvR48 env) {
        for (Field f : getClass().getFields()) {
            try {
                Type ty = f.getGenericType();
                if (ty instanceof Class) {
                    if (TrPage.class.isAssignableFrom((Class<?>) ty)) {
                        ((TrPage) f.get(this)).fillFromVM(env);
                        continue;
                    }
                }
                f.set(this, calculateValueFor(env, f.getDeclaringClass().getSimpleName(), f.getName(), ty));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Object calculateValueFor(MVMEnvR48 env, String cName, String name, Type type) {
        name = cName + "." + name;
        Slot s = env.getSlot(new DatumSymbol(name));
        if (type == String.class) {
            if (s != null)
                if (s.v instanceof String)
                    return s.v;
            System.err.println("TrPage: Missing term " + name);
            return "!!!" + name + "!!!";
        } else {
            throw new RuntimeException("TrPage: Unable to handle " + name + " type " + type);
        }
    }
}

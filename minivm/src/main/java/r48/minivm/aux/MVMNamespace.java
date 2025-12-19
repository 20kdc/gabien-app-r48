/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.aux;

import java.util.ArrayList;
import java.util.HashMap;

import datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMSlot;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMCompileScope;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.fn.MVMFn;
import r48.minivm.fn.MVMMacro;

/**
 * Used for generic namespaces that need to be expandable.
 * As a best practice, this class should be subclassed.
 * Created 19th December, 2025.
 */
public class MVMNamespace<T> {
    public final String slotPrefix;
    public final MVMEnv env;
    public final MVMType myType;
    public final HashMap<String, T> values = new HashMap<>();

    /**
     * @param slotPrefix The resulting slots have this prefix.
     * @param env The environment this namespace works with.
     * @param typeName The type name to register.
     * @param typeClass The type of things in this namespace.
     */
    public MVMNamespace(String slotPrefix, String noun, MVMEnv env, String typeName, Class<T> typeClass) {
        myType = MVMType.typeOfClass(typeClass);
        String apiPrefix = typeName + "-";
        env.defineType(new DatumSymbol(typeName), myType);
        this.slotPrefix = slotPrefix;
        this.env = env;
        env.defLib(apiPrefix + "list", MVMType.LIST, () -> {
            return new ArrayList<>(values.keySet());
        }, "Returns a list of strings, one per " + noun + ". The corresponding variables are prefixed with '" + slotPrefix + "'.");
        env.defLib(apiPrefix + "get", myType, MVMType.STR, (key) -> {
            return values.get(key);
        }, "Gets the " + noun + " by this name.");
        if (supportsNew()) {
            final MVMFn newFn = env.defLib(apiPrefix + "new", myType, MVMType.ANY, (key) -> {
                T thing = createNew((String) key);
                add(MVMU.coerceToString(key), thing);
                return thing;
            }, "Creates a new " + noun + " by the given name (as something string-ish) and registers it.");
            env.defineSlot(new DatumSymbol(apiPrefix + "define"), new MVMMacro(apiPrefix + "-define") {
                @Override
                public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
                    if (call.length != 1)
                        throw new RuntimeException(apiPrefix + "define expects exactly 1 arg");
                    return cs.compile(new Object[] {
                        newFn,
                        MVMU.coerceToString(call[0])
                    });
                }
            }).help("See " + apiPrefix + "define (but with a constant symbol)");
        } else {
            @SuppressWarnings("unchecked")
            final MVMFn regFn = env.defLib(apiPrefix + "register", myType, MVMType.ANY, myType, (key, thing) -> {
                add(MVMU.coerceToString(key), (T) thing);
                return thing;
            }, "Registers a[n] " + noun + " by the given name (as something string-ish)(.");
            env.defineSlot(new DatumSymbol(apiPrefix + "define"), new MVMMacro(apiPrefix + "-define") {
                @Override
                public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
                    if (call.length != 2)
                        throw new RuntimeException(apiPrefix + "define expects exactly 2 args");
                    return cs.compile(new Object[] {
                        regFn,
                        MVMU.coerceToString(call[0]),
                        call[1]
                    });
                }
            }).help("See " + apiPrefix + "define (but with a constant symbol)");
        }
    }

    protected boolean supportsNew() {
        return false;
    }
    protected T createNew(String name) {
        throw new RuntimeException("Cannot create new " + name);
    }

    /**
     * Adds a Thing.
     */
    public MVMSlot add(String name, T thing) {
        MVMSlot res = env.defineSlot(new DatumSymbol(slotPrefix + name), thing, myType);
        values.put(name, thing);
        return res;
    }
}

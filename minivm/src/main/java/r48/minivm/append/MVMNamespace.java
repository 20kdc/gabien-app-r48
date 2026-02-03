/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.append;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
    public final @Nullable String slotPrefix;
    public final @NonNull String noun;
    public final @NonNull MVMEnv env;
    public final @NonNull MVMType myType;
    public final @NonNull HashMap<String, T> values = new HashMap<>();

    /**
     * @param slotPrefix The resulting slots have this prefix.
     * @param apiPrefixBase "-" is added and APIs are prefixed accordingly.
     * @param env The environment this namespace works with.
     * @param noun The noun used where applicable.
     * @param type The MVMType.
     */
    public MVMNamespace(@Nullable String slotPrefix, @NonNull String apiPrefixBase, @NonNull MVMEnv env, @NonNull String noun, @NonNull MVMType type) {
        myType = type;
        this.noun = noun;
        String apiPrefix = apiPrefixBase + "-";
        this.slotPrefix = slotPrefix;
        this.env = env;
        String moreInfo = "";
        if (slotPrefix != null)
            moreInfo = "The corresponding variables are prefixed with '" + slotPrefix + "'.";
        env.defLib(apiPrefix + "list", MVMType.LIST, () -> {
            return new ArrayList<>(values.keySet());
        }, "(" + apiPrefix + "list): Returns a list of strings, one per " + noun + "." + moreInfo);
        env.defLib(apiPrefix + "get", myType, MVMType.STR, (key) -> {
            return values.get(key);
        }, "(" + apiPrefix + "get K): Gets the " + noun + " by this name.");
        if (supportsNew()) {
            final MVMFn newFn = env.defLib(apiPrefix + "new", myType, MVMType.ANY, (key) -> {
                T thing = createNew((String) key);
                add(MVMU.coerceToString(key), thing);
                return thing;
            }, "(" + apiPrefix + "new K): Creates a new " + noun + " by the given name (as something string-ish) and registers it.");
            env.defineSlot(new DatumSymbol(apiPrefix + "define"), new MVMMacro(apiPrefix + "define") {
                @Override
                public MVMCExpr compile(MVMCompileScope cs, Object[] call) {
                    if (call.length != 1)
                        throw new RuntimeException(apiPrefix + "define expects exactly 1 arg");
                    return cs.compile(new Object[] {
                        newFn,
                        MVMU.coerceToString(call[0])
                    });
                }
            }).help("(" + apiPrefix + "define K): See " + apiPrefix + "new (but with a constant symbol)");
        } else if (!selfInstalling()) {
            @SuppressWarnings("unchecked")
            final MVMFn regFn = env.defLib(apiPrefix + "register", myType, MVMType.ANY, myType, (key, thing) -> {
                add(MVMU.coerceToString(key), (T) thing);
                return thing;
            }, "(" + apiPrefix + "register K V): Registers a[n] " + noun + " by the given name (as something string-ish)(.");
            env.defineSlot(new DatumSymbol(apiPrefix + "define"), new MVMMacro(apiPrefix + "define") {
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
            }).help("(" + apiPrefix + "define K V): See " + apiPrefix + "define (but with a constant symbol)");
        }
    }

    /**
     * @param slotPrefix The resulting slots have this prefix.
     * @param apiPrefixBase "-" is added and APIs are prefixed accordingly.
     * @param env The environment this namespace works with.
     * @param typeName The type name to register. Also the noun.
     * @param typeClass The type of things in this namespace.
     */
    public MVMNamespace(String slotPrefix, String apiPrefixBase, MVMEnv env, String typeName, Class<T> typeClass) {
        this(slotPrefix, apiPrefixBase, env, typeName, MVMType.typeOfClass(typeClass));
        env.defineType(new DatumSymbol(typeName), myType);
    }

    /**
     * If your instances install themselves, return true here.
     * This removes the APIs for registration.
     * Does not work if supportsNew is true.
     */
    protected boolean selfInstalling() {
        return false;
    }

    /*
     * If you implemented createNew, return true here.
     * This changes the API for registration.
     */
    protected boolean supportsNew() {
        return false;
    }

    /**
     * Creates a new Thing.
     */
    protected T createNew(String name) {
        throw new RuntimeException("Cannot create new " + noun + ": " + name);
    }

    /**
     * Adds a Thing.
     * If no slot prefix is defined, this doesn't return a slot.
     */
    public MVMSlot add(String name, T thing) {
        MVMSlot res = null;
        if (values.containsKey(name))
            throw new RuntimeException("Cannot define " + noun + ": " + name + " twice.");
        if (slotPrefix != null)
            res = env.defineSlot(new DatumSymbol(slotPrefix + name), thing, myType).help(null);
        values.put(name, thing);
        return res;
    }

    /**
     * If 'o' is a symbol or string, resolves it to the target.
     * Otherwise, returns whatever it is as-is and relies on the cast to fail.
     */
    @SuppressWarnings("unchecked")
    public T coerce(Object o) {
        if (o instanceof DatumSymbol)
            o = ((DatumSymbol) o).id;
        if (o instanceof String) {
            T res = values.get(o);
            if (res == null)
                throw new RuntimeException("Cannot resolve " + noun + ": " + o);
        }
        return (T) o;
    }
}

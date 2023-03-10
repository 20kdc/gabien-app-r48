/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import r48.App;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.data.RORIO;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * The registry of MagicalBinder objects that are used to replace major things.
 * Particular functions of this:
 * 1. Ensures a 1 IMagicalBinder per binder-format policy on major binders
 * 2. Ensures that for any given target object, there is one bound object per major binder
 * 2 is critically important for maintaining consistency, and 1 is important to maintain 2.
 * Created on February 11th, 2018
 */
public class MagicalBinders {
    // This never has to be cleared as these are application-wide singletons that don't have state.
    private static HashMap<String, IMagicalBinder> magicalBinderCache = new HashMap<String, IMagicalBinder>();

    public static IMagicalBinder getBinderByName(String name) {
        if (magicalBinderCache.containsKey(name))
            return magicalBinderCache.get(name);
        IMagicalBinder imb = getBinderByNameCore(name);
        if (imb != null)
            magicalBinderCache.put(name, imb);
        return imb;
    }

    private static IMagicalBinder getBinderByNameCore(String name) {
        // NOTE: You can't use the actual binder prefix here, too early in init
        return null;
    }

    public static IMagicalBinder getBinderFor(App app, RORIO rio) {
        if (rio.getType() == 'u')
            if (app.odb.binderPrefix != null)
                if (rio.getSymbol().startsWith(app.odb.binderPrefix)) {
                    String bp = rio.getSymbol().substring(app.odb.binderPrefix.length());
                    return MagicalBinders.getBinderByName(bp);
                }
        return null;
    }

    public static RubyIO toBoundWithCache(App app, IMagicalBinder binder, IRIO trueTarget) {
        HashMap<IMagicalBinder, WeakReference<RubyIO>> hm = app.magicalBindingCache.get(trueTarget);
        if (hm == null) {
            hm = new HashMap<IMagicalBinder, WeakReference<RubyIO>>();
            app.magicalBindingCache.put(trueTarget, hm);
        }
        WeakReference<RubyIO> b = hm.get(binder);
        RubyIO v = null;
        if (b != null) {
            v = b.get();
            if (v == null)
                b = null;
        }
        if (b == null) {
            v = binder.targetToBoundNCache(trueTarget);
            b = new WeakReference<RubyIO>(v);
            hm.put(binder, b);
        }
        return v;
    }
}

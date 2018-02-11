/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import r48.AppMain;
import r48.RubyIO;

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
    public static IMagicalBinder getBinderByName(String name) {
        if (AppMain.magicalBinderCache.containsKey(name))
            return AppMain.magicalBinderCache.get(name);
        IMagicalBinder imb = getBinderByNameCore(name);
        if (imb != null)
            AppMain.magicalBinderCache.put(name, imb);
        return imb;
    }

    private static IMagicalBinder getBinderByNameCore(String name) {
        if (name.equals("R2kAnimationFrames"))
            return LcfMagicalBinder.getAnimationFrames();
        if (name.equals("R2kTroopPages"))
            return LcfMagicalBinder.getTroopPages();
        return null;
    }

    public static IMagicalBinder getBinderFor(RubyIO rio) {
        if (rio.type == 'u')
            if (AppMain.objectDB.binderPrefix != null)
                if (rio.symVal.startsWith(AppMain.objectDB.binderPrefix)) {
                    String bp = rio.symVal.substring(AppMain.objectDB.binderPrefix.length());
                    return MagicalBinders.getBinderByName(bp);
                }
        return null;
    }

    public static RubyIO toBoundWithCache(IMagicalBinder binder, RubyIO trueTarget) {
        HashMap<IMagicalBinder, WeakReference<RubyIO>> hm = AppMain.magicalBindingCache.get(trueTarget);
        if (hm == null) {
            hm = new HashMap<IMagicalBinder, WeakReference<RubyIO>>();
            AppMain.magicalBindingCache.put(trueTarget, hm);
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

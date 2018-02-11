/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import r48.AppMain;
import r48.RubyIO; /**
 * The registry of MagicalBinder objects that are used to replace major things.
 * Used so that certain tools can still operate in the presence of a magical binder.
 * Created on February 11th, 2018
 */
public class MagicalBinders {
    public static IMagicalBinder getBinderByName(String name) {
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

}

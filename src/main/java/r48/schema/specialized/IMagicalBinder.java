/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import r48.RubyIO;

/**
 * Created on 29/07/17.
 */
public interface IMagicalBinder {
    RubyIO targetToBoundNCache(RubyIO target);

    // Returns true on change.
    boolean applyBoundToTarget(RubyIO bound, RubyIO target);

    // Returns true on change.
    boolean modifyVal(RubyIO trueTarget, boolean setDefault);
}

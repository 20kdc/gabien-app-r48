/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import r48.io.data.IRIO;

/**
 * Arrays are used everywhere, but they can be a bit inflexible
 * Created on 2/19/17.
 */
public class ArrayUtils {
    public static void insertRioElement(RubyIO target, RubyIO rio, int i) {
        IRIO[] old = target.arrVal;
        // If i >= old.length, add nulls (uhoh)
        if (i >= old.length) {
            IRIO[] n = new IRIO[i + 1];
            for (int j = 0; j < n.length; j++)
                n[j] = new RubyIO().setNull();
            System.arraycopy(old, 0, n, 0, old.length);
            n[i] = rio;
            target.arrVal = n;
            return;
        }
        IRIO[] newArr = new IRIO[old.length + 1];
        System.arraycopy(old, 0, newArr, 0, i);
        newArr[i] = rio;
        System.arraycopy(old, i, newArr, i + 1, old.length - i);
        target.arrVal = newArr;
    }
}

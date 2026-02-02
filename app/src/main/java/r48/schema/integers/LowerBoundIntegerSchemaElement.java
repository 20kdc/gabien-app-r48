/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.integers;

import r48.R48;

/**
 * Useful for hash keys.
 * Created on 02/06/17.
 */
public class LowerBoundIntegerSchemaElement extends IntegerSchemaElement {
    public int lowerBound;

    public LowerBoundIntegerSchemaElement(R48 app, int bound, int i) {
        super(app, i);
        lowerBound = bound;
    }

    @Override
    public long filter(long i) {
        if (i < lowerBound)
            i = lowerBound;
        return i;
    }
}

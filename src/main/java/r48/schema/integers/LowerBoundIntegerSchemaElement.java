/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.integers;

/**
 * Useful for hash keys.
 * Created on 02/06/17.
 */
public class LowerBoundIntegerSchemaElement extends IntegerSchemaElement {
    public int lowerBound;

    public LowerBoundIntegerSchemaElement(int bound, int i) {
        super(i);
        lowerBound = bound;
    }

    @Override
    public long filter(long i) {
        if (i < lowerBound)
            i = lowerBound;
        return i;
    }
}

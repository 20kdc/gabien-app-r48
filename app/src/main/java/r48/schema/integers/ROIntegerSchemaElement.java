/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.integers;

import r48.App;

/**
 * Created on 12/29/16.
 */
public class ROIntegerSchemaElement extends IntegerSchemaElement {
    public ROIntegerSchemaElement(App app, int i) {
        super(app, i);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}

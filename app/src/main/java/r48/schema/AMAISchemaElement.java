/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import r48.App;
import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;

/**
 * Always Matches Array Index
 * (@id on Events)
 * Created on 12/29/16.
 */
public class AMAISchemaElement extends OpaqueSchemaElement {
    public AMAISchemaElement(App app) {
        super(app);
    }

    @Override
    public String getMessage(IRIO v) {
        return T.s.parIdx + v;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // this only works properly given lastArrayIndex
        if (path.lastArrayIndex == null)
            return;
        if (checkType(target, 'i', null, setDefault || !IRIO.rubyEquals(target, path.lastArrayIndex))) {
            // always must be set to this
            target.setDeepClone(path.lastArrayIndex);
            path.changeOccurred(true);
        }
    }
}

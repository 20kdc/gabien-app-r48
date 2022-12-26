/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import r48.io.data.IRIO;

/**
 * 0 is false, anything else is true,
 * but some things assume only 1 is true, so let's be compatible.
 * Created on 12/30/16.
 */
public class IntBooleanSchemaElement extends BooleanSchemaElement {
    public IntBooleanSchemaElement(boolean defaultVal) {
        super(defaultVal);
    }

    @Override
    public boolean determineTruth(IRIO rubyIO) {
        return rubyIO.getFX() != 0;
    }

    @Override
    public boolean modifyValueTruth(IRIO target, boolean truth) {
        boolean modified = checkType(target, 'i', null, false);
        if (modified)
            target.setFX(0);
        long resVal = truth ? 1 : 0;
        if (target.getFX() != resVal) {
            target.setFX(resVal);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean truthInvalid(IRIO target) {
        return target.getType() != 'i';
    }
}

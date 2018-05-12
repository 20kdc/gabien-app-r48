/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import r48.RubyIO;

/**
 * 0 is false, anything else is true,
 * but some things assume only 1 is true, so let's be compatible.
 * Created on 12/30/16.
 */
public class IntBooleanSchemaElement extends BooleanSchemaElement {
    public IntBooleanSchemaElement(boolean defaultVal) {
        super(defaultVal);
    }

    public boolean determineTruth(RubyIO rubyIO) {
        return rubyIO.fixnumVal != 0;
    }

    public boolean modifyValueTruth(RubyIO target, boolean truth) {
        boolean modified = SchemaElement.ensureType(target, 'i', false);
        long fv = target.fixnumVal;
        target.fixnumVal = truth ? 1 : 0;
        return (fv != target.fixnumVal) || modified;
    }

    public boolean truthInvalid(RubyIO target) {
        return target.type != 'i';
    }
}

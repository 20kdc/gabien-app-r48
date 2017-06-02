/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.integers;

import r48.RubyIO;
import r48.schema.BooleanSchemaElement;
import r48.schema.integers.IntegerSchemaElement;

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
        boolean modified = IntegerSchemaElement.ensureType(target, 'i', false);
        long fv = target.fixnumVal;
        target.fixnumVal = truth ? 1 : 0;
        return (fv != target.fixnumVal) || modified;
    }

    public boolean truthInvalid(RubyIO target) {
        return target.type != 'i';
    }
}

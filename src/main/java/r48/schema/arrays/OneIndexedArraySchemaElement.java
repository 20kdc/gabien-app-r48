/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.arrays;

import r48.RubyIO;
import r48.schema.ISchemaElement;
import r48.schema.OpaqueSchemaElement;
import r48.schema.arrays.ArraySchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Because sometimes you get curve balls thrown at you like "one-indexed arrays that aren't".
 * Created on 2/16/17.
 */
public class OneIndexedArraySchemaElement extends ArraySchemaElement {
    public ISchemaElement subelems;

    public OneIndexedArraySchemaElement(ISchemaElement s, int fixedSize) {
        super(fixedSize, true);
        subelems = s;
    }

    @Override
    protected boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        return false;
    }

    @Override
    protected ISchemaElement getElementSchema(int j) {
        // Opaque will always *default* to NIL if the result would otherwise be completely invalid data.
        if (j == 0)
            return new OpaqueSchemaElement();
        return subelems;
    }

    @Override
    public int maxHoldingHeight() {
        if (sizeFixed != 0)
            return subelems.maxHoldingHeight() * sizeFixed;
        // *gulp* guess, and hope the guess is correct.
        return subelems.maxHoldingHeight() * 16;
    }

    @Override
    protected int elementPermissionsLevel(int i, RubyIO target) {
        if (i == 0)
            return 0;
        return super.elementPermissionsLevel(i, target);
    }
}

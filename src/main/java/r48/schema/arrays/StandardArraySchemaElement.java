/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.arrays;

import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Created on 2/16/17.
 */
public class StandardArraySchemaElement extends ArraySchemaElement {
    public SchemaElement subelems;

    public StandardArraySchemaElement(SchemaElement s, int fixedSize, boolean al1) {
        super(fixedSize, al1);
        subelems = s;
    }

    @Override
    protected boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        return false;
    }

    @Override
    protected SchemaElement getElementSchema(int j) {
        return subelems;
    }

    @Override
    public int maxHoldingHeight() {
        if (sizeFixed != 0)
            return subelems.maxHoldingHeight() * sizeFixed;
        // *gulp* guess, and hope the guess is correct.
        return subelems.maxHoldingHeight() * 16;
    }
}

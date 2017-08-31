/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.arrays;

import r48.RubyIO;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Because sometimes you get curve balls thrown at you like "one-indexed arrays that aren't".
 * Created on 2/16/17.
 */
public class ArbIndexedArraySchemaElement extends ArraySchemaElement {
    public SchemaElement subelems;
    public int indexOffset;

    public ArbIndexedArraySchemaElement(SchemaElement s, int io, int fixedSize) {
        super(fixedSize, false, 0);
        subelems = s;
        indexOffset = io;
    }

    @Override
    protected boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        return false;
    }

    @Override
    protected SchemaElement getElementSchema(int j) {
        // Opaque will always *default* to NIL if the result would otherwise be completely invalid data.
        if (j < indexOffset)
            return new OpaqueSchemaElement();
        return subelems;
    }

    @Override
    protected int elementPermissionsLevel(int i, RubyIO target) {
        if (i < indexOffset)
            return 0;
        return super.elementPermissionsLevel(i, target);
    }
}

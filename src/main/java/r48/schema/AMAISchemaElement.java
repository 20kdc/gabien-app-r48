/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Always Matches Array Index
 * (@id on Events)
 * Created on 12/29/16.
 */
public class AMAISchemaElement extends OpaqueSchemaElement {

    @Override
    public String getMessage() {
        return TXDB.get("Parent Index.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        boolean overrideIt = true;
        if (!IntegerSchemaElement.ensureType(target, 'i', setDefault))
            overrideIt = !RubyIO.rubyEquals(target, path.lastArrayIndex);
        if (overrideIt) {
            // always must be set to this
            target.setShallowClone(path.lastArrayIndex);
            path.changeOccurred(true);
        }
    }
}

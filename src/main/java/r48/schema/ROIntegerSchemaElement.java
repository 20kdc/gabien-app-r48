/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

/**
 * Created on 12/29/16.
 */
public class ROIntegerSchemaElement extends IntegerSchemaElement {
    public ROIntegerSchemaElement(int i) {
        super(i);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}

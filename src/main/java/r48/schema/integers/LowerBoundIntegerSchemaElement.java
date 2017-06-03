/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.integers;

/**
 * Useful for hash keys.
 * Created on 02/06/17.
 */
public class LowerBoundIntegerSchemaElement extends IntegerSchemaElement {
    public int lowerBound;

    public LowerBoundIntegerSchemaElement(int bound, int i) {
        super(i);
        lowerBound = bound;
    }

    @Override
    public int filter(int i) {
        if (i < lowerBound)
            i = lowerBound;
        return i;
    }
}

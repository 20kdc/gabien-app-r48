/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import r48.RubyIO;

/**
 * For anything that looks even vaguely like:
 * SOME NAME | SOME CONTENTS
 * Based off of the system used in IVarSchemaElement.
 * Created on 29/06/17.
 */
public interface IFieldSchemaElement {
    int getDefaultFieldWidth(RubyIO target);

    // NOTE: This is good for one buildHoldingEditor only.
    void setFieldWidthOverride(int w);
}

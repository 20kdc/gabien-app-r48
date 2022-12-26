/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Created on 2/16/17.
 */
public class StandardArraySchemaElement extends ArraySchemaElement {
    public SchemaElement subelems;

    public StandardArraySchemaElement(SchemaElement s, int fixedSize, boolean al1, int ido, IArrayInterface uiHelper) {
        super(fixedSize, al1 ? 1 : 0, ido, uiHelper);
        subelems = s;
    }
    public StandardArraySchemaElement(SchemaElement s, int fixedSize, boolean al1, int ido, IArrayInterface uiHelper, SchemaElement enumer) {
        super(fixedSize, al1 ? 1 : 0, ido, uiHelper, enumer);
        subelems = s;
    }

    // Integer AL variants
    public StandardArraySchemaElement(SchemaElement s, int fixedSize, int al1, int ido, IArrayInterface uiHelper) {
        super(fixedSize, al1, ido, uiHelper);
        subelems = s;
    }

    public StandardArraySchemaElement(SchemaElement s, int fixedSize, int al1, int ido, IArrayInterface uiHelper, SchemaElement enumer) {
        super(fixedSize, al1, ido, uiHelper, enumer);
        subelems = s;
    }

    @Override
    protected boolean autoCorrectArray(IRIO array, SchemaPath path) {
        return false;
    }

    @Override
    protected SchemaElement getElementSchema(int j) {
        return subelems;
    }
}

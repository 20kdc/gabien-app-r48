/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.toolsets.utils;

import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.SchemaElementIOP;
import r48.tr.TrPage.FF0;

/**
 * ID changer
 * Created 17th July, 2023.
 */
public final class IDChangerEntry {
    public final FF0 text;

    // Before deproxying. 1st is actual intended value.
    public final SchemaElement[] unresolvedElements;

    public final SchemaElement userFacing;

    public IDChangerEntry(FF0 text, SchemaElement[] ue) {
        this.text = text;
        unresolvedElements = ue;
        userFacing = ue[0];
    }

    public SchemaElement[] resolve() {
        SchemaElement[] res = new SchemaElement[unresolvedElements.length];
        for (int i = 0; i < unresolvedElements.length; i++)
            res[i] = AggregateSchemaElement.extractField(unresolvedElements[i], null);
        return res;
    }

    /**
     * This is probably the biggest gamble of them all...
     * That this function will match every variable reference...
     */
    public static boolean match(SchemaElementIOP a, RORIO rio, SchemaElement[] resolved) {
        SchemaElement b = AggregateSchemaElement.extractField(a, null);
        SchemaElement c = AggregateSchemaElement.extractField(a, rio);
        for (SchemaElement se : resolved)
            if (se == a || se == b || se == c)
                return true;
        return false;
    }

    public final EnumSchemaElement extractEnum() {
        return (EnumSchemaElement) AggregateSchemaElement.extractField(userFacing, null);
    }
}

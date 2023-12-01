/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.util;

import r48.io.data.IRIO;
import r48.schema.SchemaElement;

/**
 * Contains some data specific to a schema host instance about some of the UI.
 * Created 1st December, 2023.
 */
public final class EmbedDataSlot<T> {
    public final SchemaElement source;
    public final IRIO target;
    public final Object prop;
    public T value;
    public EmbedDataSlot(SchemaElement src, IRIO tgt, Object p) {
        source = src;
        target = tgt;
        prop = p;
    }
}
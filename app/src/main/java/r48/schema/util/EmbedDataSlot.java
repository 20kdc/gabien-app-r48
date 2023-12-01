/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.util;

import gabien.uslx.append.IGetSet;
import r48.io.data.IRIO;

/**
 * Contains some data specific to a schema host instance about some of the UI.
 * Created 1st December, 2023.
 */
public final class EmbedDataSlot<T> implements IGetSet<T> {
    public final IRIO target;
    public final EmbedDataKey<T> prop;
    public T value;

    public EmbedDataSlot(IRIO tgt, EmbedDataKey<T> p) {
        target = tgt;
        prop = p;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void accept(T arg0) {
        value = arg0;
    }
}
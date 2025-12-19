/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.util;

import java.util.WeakHashMap;

/**
 * A 'directory' of embed data.
 * This can be passed between schema elements to provide 'more uniqueness'.
 */
public class EmbedDataDir {
    /**
     * This is a scary implementation detail.
     */
    private static final WeakHashMap<Object, Object> MAGIC_HAT = new WeakHashMap<>();

    /*
     * Retrieves a sub-key.
     */
    @SuppressWarnings("unchecked")
    public <T> EmbedDataKey<T> key(EmbedDataKey<T> interiorKey) {
        Object res = MAGIC_HAT.get(interiorKey);
        if (res == null) {
            res = new EmbedDataKey<T>();
            MAGIC_HAT.put(interiorKey, res);
        }
        return (EmbedDataKey<T>) res;
    }
}

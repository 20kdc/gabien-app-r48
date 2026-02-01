/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.texture;

import gabien.GaBIEn;
import gabien.render.IImage;

import java.util.HashMap;

/**
 * The GabienImageLoader already has a cache (the internal cache)...
 * but XYZ doesn't, and in any case, nulls shouldn't be propagated.
 * Created on 31/05/17.
 */
public class CacheTexLoader implements ITexLoader {
    public final HashMap<String, IImage> loadedImages = new HashMap<String, IImage>();
    public final ITexLoader root;

    public CacheTexLoader(ITexLoader r) {
        root = r;
    }

    @Override
    public IImage getImage(String a, boolean t) {
        String ki = a.toLowerCase() + "_" + (t ? "pano" : "geni");
        if (loadedImages.containsKey(ki))
            return loadedImages.get(ki);
        IImage i = root.getImage(a, t);
        if (i == null)
            i = GaBIEn.getErrorImage();
        loadedImages.put(ki, i);
        return i;
    }

    @Override
    public void flushCache() {
        loadedImages.clear();
        root.flushCache();
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IImage;

import java.util.HashMap;

/**
 * The GabienImageLoader already has a cache (the internal cache)...
 * but XYZ doesn't, and in any case, nulls shouldn't be propagated.
 * Created on 31/05/17.
 */
public class CacheImageLoader implements IImageLoader {
    public final HashMap<String, IImage> loadedImages = new HashMap<String, IImage>();
    public final IImageLoader root;

    public CacheImageLoader(IImageLoader r) {
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

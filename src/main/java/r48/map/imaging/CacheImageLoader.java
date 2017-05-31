/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

import java.util.HashMap;

/**
 * The GabienImageLoader already has a cache (the internal cache)...
 * but XYZ doesn't!
 * Created on 31/05/17.
 */
public class CacheImageLoader implements IImageLoader {
    public final HashMap<String, IGrInDriver.IImage> loadedImages = new HashMap<String, IGrInDriver.IImage>();
    public final IImageLoader root;

    public CacheImageLoader(IImageLoader r) {
        root = r;
    }

    @Override
    public IGrInDriver.IImage getImage(String a, int tr, int tg, int tb) {
        String ki = a + "_" + tr + "_" + tg + "_" + tb;
        if (loadedImages.containsKey(ki))
            return loadedImages.get(ki);
        IGrInDriver.IImage i = root.getImage(a, tr, tg, tb);
        loadedImages.put(ki, i);
        return i;
    }

    @Override
    public void flushCache() {
        loadedImages.clear();
    }
}

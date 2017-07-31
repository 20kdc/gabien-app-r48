/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

/**
 * Used for multi-format scenarios such as:
 * XYZ
 * PNG  (8-bit)
 * PNG (32-bit)
 * Created on 30/07/17.
 */
public class ChainedImageLoader implements IImageLoader {
    private final IImageLoader[] subLoaders;

    public ChainedImageLoader(IImageLoader[] subs) {
        subLoaders = subs;
    }

    @Override
    public IGrInDriver.IImage getImage(String name, boolean panorama) {
        for (IImageLoader iil : subLoaders) {
            IGrInDriver.IImage im = iil.getImage(name, panorama);
            if (im != null)
                return im;
        }
        return null;
    }

    @Override
    public void flushCache() {
        for (IImageLoader iil : subLoaders)
            iil.flushCache();
    }
}

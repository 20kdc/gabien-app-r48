/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

/**
 * Replaces *all* instances of using GaBIEn image loading, due to XYZ support that may be required in future.
 * Or other things.
 * It is assumed the image loader has a cache.
 * Note that everything going here comes from the Map group.
 * Created on 29/05/17.
 */
public interface IImageLoader {
    // Similar to getImage in the old system.
    IGrInDriver.IImage getImage(String name, int cR, int cG, int cB);
    void flushCache();
}

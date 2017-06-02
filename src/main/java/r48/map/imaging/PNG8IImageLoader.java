/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

/**
 * You know what I mentioned previously about "Here goes nothing"?
 * Well, now here goes even more nothing.
 * Currently always fails.
 * Created on 02/06/17.
 */
public class PNG8IImageLoader implements IImageLoader {
    public PNG8IImageLoader(String rootPath) {
    }

    @Override
    public IGrInDriver.IImage getImage(String name, boolean panorama) {
        return null;
    }

    @Override
    public void flushCache() {

    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

/**
 * Might not handle transparency as well as it should on the PNGs if they don't do alpha properly.
 * (later) ...they don't.
 * Created on 01/06/17.
 */
public class XYZOrPNGImageLoader implements IImageLoader {
    public final String rootPath;
    public final IImageLoader xImageLoader;
    public final IImageLoader pImageLoader;
    public final IImageLoader gImageLoader;

    public XYZOrPNGImageLoader(String root) {
        rootPath = root;
        xImageLoader = new XYZImageLoader(rootPath);
        pImageLoader = new PNG8IImageLoader(rootPath);
        gImageLoader = new GabienImageLoader(rootPath, ".png");
    }

    @Override
    public IGrInDriver.IImage getImage(String name, boolean panorama) {
        //
        IGrInDriver.IImage d = xImageLoader.getImage(name, panorama);
        if (d == null)
            d = pImageLoader.getImage(name, panorama);
        if (d == null)
            return gImageLoader.getImage(name, panorama);
        return d;
    }

    @Override
    public void flushCache() {
        gImageLoader.flushCache();
        xImageLoader.flushCache();
    }
}

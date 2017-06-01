/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

/**
 * Might not handle transparency as well as it should on the PNGs if they don't do alpha properly.
 * Created on 01/06/17.
 */
public class XYZOrPNGImageLoader implements IImageLoader {
    public final String rootPath;
    public final IImageLoader gImageLoader;
    public final IImageLoader xImageLoader;

    public XYZOrPNGImageLoader(String root) {
        rootPath = root;
        xImageLoader = new XYZImageLoader(rootPath);
        gImageLoader = new GabienImageLoader(rootPath, ".png");
    }
    @Override
    public IGrInDriver.IImage getImage(String name, int cR, int cG, int cB) {
        IGrInDriver.IImage d = xImageLoader.getImage(name, cR, cG, cB);
        if (d == null)
            return gImageLoader.getImage(name, cR, cG, cB);
        return d;
    }

    @Override
    public void flushCache() {
        gImageLoader.flushCache();
        xImageLoader.flushCache();
    }
}

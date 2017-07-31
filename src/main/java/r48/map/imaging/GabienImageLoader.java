/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabienapp.Application;

/**
 * Does whatever the default can.
 * Basically, if it happens to not support something in a given case, *then* bring explicit support into Gabien,
 * unless it's an obscure format (XYZ), then in which case do something else.
 * Created on 29/05/17.
 */
public class GabienImageLoader implements IImageLoader {
    public final String prefix, postfix;
    public final boolean ck;
    public final int r, g, b;

    public GabienImageLoader(String pf, String pp, int cr, int cg, int cb) {
        prefix = pf;
        postfix = pp;
        ck = true;
        r = cr;
        g = cg;
        b = cb;
    }

    public GabienImageLoader(String pf, String pp) {
        prefix = pf;
        postfix = pp;
        ck = false;
        r = 0;
        g = 0;
        b = 0;
    }

    @Override
    public IGrInDriver.IImage getImage(String name, boolean panorama) {
        IGrInDriver.IImage error = GaBIEn.getErrorImage();
        if (ck) {
            IGrInDriver.IImage core = GaBIEn.getImageCK(Application.autoDetectWindows(prefix + name + postfix), r, g, b);
            if (core == error)
                return null;
            return core;
        } else {
            IGrInDriver.IImage core = GaBIEn.getImage(Application.autoDetectWindows(prefix + name + postfix));
            if (core == error)
                return null;
            return core;
        }
    }

    @Override
    public void flushCache() {
        GaBIEn.hintFlushAllTheCaches();
    }
}

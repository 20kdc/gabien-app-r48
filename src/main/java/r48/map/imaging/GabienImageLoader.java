/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.map.imaging.IImageLoader;

/**
 * Does whatever the default can.
 * Basically, if it happens to not support something in a given case, *then* bring explicit support into Gabien,
 *  unless it's an obscure format (XYZ), then in which case do something else.
 * Created on 29/05/17.
 */
public class GabienImageLoader implements IImageLoader {
    public final String prefix, postfix;
    public GabienImageLoader(String pf, String pp) {
        prefix = pf;
        postfix = pp;
    }
    @Override
    public IGrInDriver.IImage getImage(String name, int cR, int cG, int cB) {
        return GaBIEn.getImage(prefix + name + postfix, cR, cG, cB);
    }

    @Override
    public void flushCache() {
        GaBIEn.hintFlushAllTheCaches();
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.imaging;

import gabien.render.IImage;

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
    public IImage getImage(String name, boolean panorama) {
        for (IImageLoader iil : subLoaders) {
            IImage im = iil.getImage(name, panorama);
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

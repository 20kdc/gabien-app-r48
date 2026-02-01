/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.texture;

import gabien.render.IImage;
import gabien.uslx.vfs.FSBackend;
import r48.imageio.ImageIOFormat;

/**
 * Uses the 'imageio' stuff to load images in a given format.
 */
public class ImageIOTexLoader implements ITexLoader {

    public final FSBackend src;
    public final ImageIOFormat format;
    public final String postfix;
    public final boolean firstPalTransparency;

    public ImageIOTexLoader(FSBackend src, ImageIOFormat f, String pf, boolean t) {
        this.src = src;
        format = f;
        postfix = pf;
        firstPalTransparency = t;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        ImageIOFormat.TryToLoadResult im = ImageIOFormat.tryToLoad(src.intoPath(name + postfix), new ImageIOFormat[] {format});
        if (im != null) {
            if ((!panorama) && firstPalTransparency)
                if (im.iei.palette != null)
                    im.iei.palette.set(0, im.iei.palette.get(0) & 0xFFFFFF);
            return im.iei.rasterize();
        }
        return null;
    }

    @Override
    public void flushCache() {

    }
}

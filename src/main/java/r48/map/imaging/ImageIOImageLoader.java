/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.map.imaging;

import gabien.IImage;
import r48.imageio.ImageIOFormat;
import r48.imageio.ImageIOImage;

/**
 * Uses the 'imageio' stuff to load images in a given format.
 */
public class ImageIOImageLoader implements IImageLoader {

    public final ImageIOFormat format;
    public final String postfix;
    public final boolean firstPalTransparency;

    public ImageIOImageLoader(ImageIOFormat f, String pf, boolean t) {
        format = f;
        postfix = pf;
        firstPalTransparency = t;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        ImageIOImage im = ImageIOFormat.tryToLoad(name + postfix, new ImageIOFormat[] {format});
        if (im != null) {
            if ((!panorama) && firstPalTransparency)
                if (im.palette != null)
                    im.palette.set(0, im.palette.get(0) & 0xFFFFFF);
            return im.rasterize();
        }
        return null;
    }

    @Override
    public void flushCache() {

    }
}

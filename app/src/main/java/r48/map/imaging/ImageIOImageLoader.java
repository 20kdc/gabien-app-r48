/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.imaging;

import gabien.render.IImage;
import r48.app.AppCore;
import r48.imageio.ImageIOFormat;

/**
 * Uses the 'imageio' stuff to load images in a given format.
 */
public class ImageIOImageLoader extends AppCore.Csv implements IImageLoader {

    public final ImageIOFormat format;
    public final String postfix;
    public final boolean firstPalTransparency;

    public ImageIOImageLoader(AppCore app, ImageIOFormat f, String pf, boolean t) {
        super(app);
        format = f;
        postfix = pf;
        firstPalTransparency = t;
    }

    @Override
    public IImage getImage(String name, boolean panorama) {
        ImageIOFormat.TryToLoadResult im = ImageIOFormat.tryToLoad(app.gameResources.intoPath(name + postfix), new ImageIOFormat[] {format});
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

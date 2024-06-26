/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.imageio;

import r48.app.InterlaunchGlobals;

import java.io.IOException;

import gabien.render.WSIImage;

/**
 * ImageIOFormat for the gabien system PNG writer (but not the loader, since this API uses byte[])
 * Created on April 14th 2018.
 */
public class GabienImageIOFormat extends ImageIOFormat {
    public GabienImageIOFormat(InterlaunchGlobals app) {
        super(app, true);
    }

    @Override
    public String saveName(ImageIOImage img) {
        // Don't recommend saving in this format if indexed is possible.
        if (PNG8IImageIOFormat.supports(img))
            return null;
        return ilg.t.g.img_png32;
    }

    @Override
    public byte[] saveFile(ImageIOImage img) throws IOException {
        return img.rasterizeToWSI().createPNG();
    }

    @Override
    public ImageIOImage loadFile(byte[] s, WSIImage iImage) throws IOException {
        if (iImage == null)
            throw new IOException("system image loader didn't understand it");
        return new ImageIOImage(iImage.width, iImage.height, iImage.getPixels(), null);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imageio;

import gabien.GaBIEn;
import gabien.render.WSIImage;
import gabien.uslx.vfs.FSBackend;
import r48.app.AppCore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Describes an image format (save/load buttons)
 * Created on April 13th 2018
 */
public abstract class ImageIOFormat extends AppCore.Csv {
    public final boolean knowsColourKey;

    public ImageIOFormat(AppCore app, boolean ck) {
        super(app);
        knowsColourKey = ck;
    }

    // The 'save' button details (or null if no save system exists)
    public abstract String saveName(ImageIOImage img);

    // Tries to save an image, or throws an exception otherwise.
    public abstract byte[] saveFile(ImageIOImage img) throws IOException;

    // Tries to load an image, or throws an exception otherwise.
    // gInput is null (rather than the error image reference) or the system image loader output.
    public abstract ImageIOImage loadFile(byte[] s, WSIImage gInput) throws IOException;

    public static ImageIOFormat[] initializeFormats(AppCore app) {
        return new ImageIOFormat[] {
                new XYZImageIOFormat(app),
                new PNG8IImageIOFormat(app),
                new BMP8IImageIOFormat(app, 8),
                new BMP8IImageIOFormat(app, 4),
                new BMP8IImageIOFormat(app, 1),
                new GabienImageIOFormat(app),
        };
    }

    public static TryToLoadResult tryToLoad(FSBackend fs, ImageIOFormat[] formats) {
        ByteArrayOutputStream dataHolder = new ByteArrayOutputStream();
        InputStream inp = GaBIEn.getInFile(fs);
        if (inp != null) {
            try {
                byte[] buffer = new byte[2048];
                while (inp.available() > 0) {
                    int l = inp.read(buffer);
                    if (l <= 0)
                        break;
                    dataHolder.write(buffer, 0, l);
                }
                inp.close();
            } catch (IOException ioe) {
                try {
                    // ugh
                    inp.close();
                } catch (IOException ioe2) {

                }
            }
        } else {
            // No file data = This image clearly can't be loaded
            return null;
        }
        return tryToLoad(dataHolder.toByteArray(), formats);
    }

    public static TryToLoadResult tryToLoad(byte[] data, ImageIOFormat[] formats) {
        WSIImage im = GaBIEn.decodeWSIImage(new ByteArrayInputStream(data));

        for (ImageIOFormat ief : formats) {
            ImageIOImage iei = null;
            try {
                iei = ief.loadFile(data, im);
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
            if (iei != null)
                return new TryToLoadResult(ief, iei, ief.knowsColourKey);
        }
        return null;
    }

    public static class TryToLoadResult {
        public final ImageIOFormat format;
        public final ImageIOImage iei;
        public final boolean wouldKnowIfColourKey;

        public TryToLoadResult(ImageIOFormat format, ImageIOImage iei, boolean knowsColourKey) {
            this.format = format;
            this.iei = iei;
            this.wouldKnowIfColourKey = knowsColourKey;
        }
    }
}

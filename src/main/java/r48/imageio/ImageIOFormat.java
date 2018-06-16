/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.imageio;

import gabien.GaBIEn;
import gabien.IImage;
import r48.io.PathUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Describes an image format (save/load buttons)
 * Created on April 13th 2018
 */
public abstract class ImageIOFormat {
    // The 'save' button details (or null if no save system exists)
    public abstract String saveName(ImageIOImage img);

    // Tries to save an image, or throws an exception otherwise.
    public abstract byte[] saveFile(ImageIOImage img) throws IOException;

    // Tries to load an image, or throws an exception otherwise.
    // gInput is null (rather than the error image reference) or the system image loader output.
    public abstract ImageIOImage loadFile(byte[] s, IImage gInput) throws IOException;

    // The list of supported formats.
    public static ImageIOFormat[] supportedFormats = new ImageIOFormat[0];

    public static void initializeFormats() {
        supportedFormats = new ImageIOFormat[] {
                new XYZImageIOFormat(),
                new PNG8IImageIOFormat(),
                new BMP8IImageIOFormat(8),
                new BMP8IImageIOFormat(4),
                new BMP8IImageIOFormat(1),
                new GabienImageIOFormat(),
        };
    }

    public static ImageIOImage tryToLoad(String filename, ImageIOFormat[] formats) {
        filename = PathUtils.autoDetectWindows(filename);

        ByteArrayOutputStream dataHolder = new ByteArrayOutputStream();
        InputStream inp = GaBIEn.getInFile(filename);
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
        }
        byte[] data = dataHolder.toByteArray();

        IImage im = GaBIEn.getImageEx(filename, true, false);
        if (im == GaBIEn.getErrorImage())
            im = null;

        for (ImageIOFormat ief : formats) {
            ImageIOImage iei = null;
            try {
                iei = ief.loadFile(data, im);
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
            if (iei != null)
                return iei;
        }
        return null;
    }
}

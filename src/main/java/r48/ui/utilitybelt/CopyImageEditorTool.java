/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import r48.AppMain;
import r48.RubyIO;
import r48.io.BMPConnection;

import java.io.IOException;

/**
 * ...Yes, it uses BMPs. (Using the full ImageIO stack seemed like overcomplicating things a little.)
 * Created on December 15, 2018.
 */
public class CopyImageEditorTool extends RectangleImageEditorTool {
    @Override
    protected void performOperation(UIImageEditView view, int bW, int bH) {
        int palSize = view.image.paletteSize();
        byte[] data;
        if (view.image.usesPalette() && (palSize <= 256)) {
            data = BMPConnection.prepareBMP(bW, bH, 8, view.image.paletteSize(), true, false);
            try {
                BMPConnection bc = new BMPConnection(data, BMPConnection.CMode.Normal, 0, false);
                for (int i = 0; i < palSize; i++)
                    bc.putPalette(i, view.image.getPaletteRGB(i));
                for (int j = 0; j < bH; j++) {
                    for (int i = 0; i < bW; i++) {
                        FillAlgorithm.Point p = view.correctPoint(aX + i, aY + j);
                        if (p != null)
                            bc.putPixel(i, j, view.image.getRaw(p.x, p.y));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            data = BMPConnection.prepareBMP(bW, bH, 32, 0, true, false);
            try {
                BMPConnection bc = new BMPConnection(data, BMPConnection.CMode.Normal, 0, false);
                for (int j = 0; j < bH; j++) {
                    for (int i = 0; i < bW; i++) {
                        FillAlgorithm.Point p = view.correctPoint(aX + i, aY + j);
                        if (p != null)
                            bc.putPixel(i, j, view.image.getRGB(p.x, p.y));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        AppMain.theClipboard = new RubyIO().setUser("Image", data);
    }

}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import r48.App;
import r48.io.BMPConnection;
import r48.io.data.IRIOGeneric;

import java.io.IOException;

/**
 * ...Yes, it uses BMPs. (Using the full ImageIO stack seemed like overcomplicating things a little.)
 * Created on December 15, 2018.
 */
public class CopyImageEditorTool extends RectangleImageEditorTool {
    public CopyImageEditorTool(App app) {
        super(app);
    }

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
        view.app.theClipboard = new IRIOGeneric(app.ctxClipboardUTF8Encoding).setUser("Image", data);
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (stage2)
            return T.ie.tdFillE;
        return T.ie.tdCopy;
    }

}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.App;

/**
 * Created on 16th July 2018
 */
public class RectangleImageEditorTool extends ImageEditorTool {
    public boolean stage2;
    public int aX, aY;

    public RectangleImageEditorTool(App app) {
        super(app);
    }

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {
        if (major && (!dragging)) {
            if (!stage2) {
                aX = x;
                aY = y;
                stage2 = true;
            } else {
                int bW = (Math.max(aX, x) + 1) - Math.min(aX, x);
                int bH = (Math.max(aY, y) + 1) - Math.min(aY, y);
                aX = Math.min(aX, x);
                aY = Math.min(aY, y);
                performOperation(view, bW, bH);
                stage2 = false;
            }
        }
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    protected void performOperation(UIImageEditView view, int bW, int bH) {
        view.eds.startSection();
        for (int i = 0; i < bW; i++) {
            for (int j = 0; j < bH; j++) {
                FillAlgorithm.Point p = view.correctPoint(aX + i, aY + j);
                if (p == null)
                    continue;
                view.image.setPixel(p.x, p.y, view.selPaletteIndex);
            }
        }
        view.eds.endSection();
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, getClass());
    }

    @Override
    public Rect getSelection() {
        if (stage2)
            return new Rect(aX, aY, 0, 0);
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (stage2)
            return app.ts("Press another bounding point to finish.");
        return app.ts("Press bounding points to fill.");
    }

    @Override
    public ImageEditorTool getCamModeLT() {
        return null;
    }
}

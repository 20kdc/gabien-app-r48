/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.dbs.TXDB;

/**
 * Created on 16th July 2018
 */
public class RectangleImageEditorTool implements IImageEditorTool {
    public boolean stage2;
    public int aX, aY;

    @Override
    public void enter(UIImageEditView uiev) {

    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, UIImageEditView view, boolean major, boolean dragging) {
        if (major && (!dragging)) {
            if (!stage2) {
                aX = imp.x;
                aY = imp.y;
                stage2 = true;
            } else {
                int bW = (Math.max(aX, imp.x) + 1) - Math.min(aX, imp.x);
                int bH = (Math.max(aY, imp.y) + 1) - Math.min(aY, imp.y);
                aX = Math.min(aX, imp.x);
                aY = Math.min(aY, imp.y);
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
        UIImageEditView.ImPoint imp2 = new UIImageEditView.ImPoint(0, 0);
        for (int i = 0; i < bW; i++) {
            imp2.x = aX + i;
            for (int j = 0; j < bH; j++) {
                imp2.y = aY + j;
                imp2.updateCorrected(view);
                view.image.setPixel(imp2.correctedX, imp2.correctedY, view.selPaletteIndex);
            }
        }
        view.eds.endSection();
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, RectangleImageEditorTool.class);
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
            return TXDB.get("Press another bounding point to finish.");
        return TXDB.get("Press bounding points to fill.");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

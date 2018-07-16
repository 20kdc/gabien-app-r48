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
 * Created on 16th July 2018.
 */
public class TileImageEditorTool extends RectangleImageEditorTool {

    @Override
    public void enter(UIImageEditView uiev) {
        if (uiev.tiling != null) {
            uiev.tiling = null;
            uiev.currentTool = new RootImageEditorTool();
        }
    }

    @Override
    protected void performOperation(UIImageEditView view, int bW, int bH) {
        UIImageEditView.ImPoint l1 = new UIImageEditView.ImPoint(aX, aY);
        l1.updateCorrected(view);
        view.tiling = new Rect(l1.correctedX, l1.correctedY, bW, bH);
        view.currentTool = new RootImageEditorTool();
        view.newToolCallback.run();
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, TileImageEditorTool.class);
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (stage2)
            return TXDB.get("Click remaining point of area to restrict drawing to.");
        return TXDB.get("Click points of area to restrict drawing to.");
    }
}

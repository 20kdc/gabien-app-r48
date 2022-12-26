/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
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
    public void forceDifferentTool(UIImageEditView uiev) {
        if (uiev.tiling != null) {
            uiev.tiling = null;
            uiev.currentTool = new RootImageEditorTool();
        }
    }

    @Override
    protected void performOperation(UIImageEditView view, int bW, int bH) {
        FillAlgorithm.Point p = view.correctPoint(aX, aY);
        if (p == null)
            return;
        view.tiling = new Rect(p.x, p.y, bW, bH);
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

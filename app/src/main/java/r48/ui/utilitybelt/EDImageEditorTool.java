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
 * Created on 16th July 2018
 */
public class EDImageEditorTool implements IImageEditorTool {
    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {
        if (major && (!dragging)) {
            view.currentTool = new RootImageEditorTool();
            applyCore(view.correctPoint(x, y), view);
        }
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    // This one is called from UIImageEditView for shift purposes
    public void applyCore(FillAlgorithm.Point correctedPoint, UIImageEditView view) {
        view.eds.startSection();
        int rawValue = view.image.getRaw(correctedPoint.x, correctedPoint.y);
        view.selPaletteIndex = view.image.rawToPalette(rawValue);
        view.eds.endSection();
        // This did something with the palette. Use the new tool callback to force a state change.
        // This is also used to return to the root image editor tool if necessary.
        view.newToolCallback.run();
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, EDImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return TXDB.get("Tap on the point to select the colour of.");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

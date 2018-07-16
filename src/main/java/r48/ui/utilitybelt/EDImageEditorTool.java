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
public class EDImageEditorTool implements IImageEditorTool {
    @Override
    public void enter(UIImageEditView uiev) {

    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, UIImageEditView view, boolean major, boolean dragging) {
        if (major && (!dragging)) {
            view.currentTool = new RootImageEditorTool();
            applyCore(imp, view);
        }
    }

    public void applyCore(UIImageEditView.ImPoint imp, UIImageEditView uiImageEditView) {
        int rawValue = uiImageEditView.image.getRaw(imp.correctedX, imp.correctedY);
        uiImageEditView.selPaletteIndex = uiImageEditView.image.rawToPalette(rawValue);
        // This did something with the palette. Use the new tool callback to force a state change.
        // This is also used to return to the root image editor tool if necessary.
        uiImageEditView.newToolCallback.run();
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

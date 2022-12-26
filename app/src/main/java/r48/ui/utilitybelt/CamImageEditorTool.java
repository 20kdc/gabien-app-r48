/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPublicPanel;
import r48.dbs.TXDB;

/**
 * Created on 14th July 2018
 */
public class CamImageEditorTool implements IImageEditorTool {
    public final IImageEditorTool oldTool;

    public CamImageEditorTool(IImageEditorTool currentTool) {
        oldTool = currentTool;
    }

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {

    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return new UIPublicPanel(0, 0);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return TXDB.get("Drag: Move around, Camera: Return to old tool");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return oldTool;
    }

}

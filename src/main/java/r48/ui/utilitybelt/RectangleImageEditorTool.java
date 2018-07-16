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
    public int p1x, p1y;

    @Override
    public void enter(UIImageEditView uiev) {

    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, UIImageEditView view, boolean major, boolean dragging) {
        if (major && (!dragging)) {
            if (!stage2) {
                p1x = imp.x;
                p1y = imp.y;
            } else {

            }
        }
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, RectangleImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return new Rect(1, 1, 2, 2);
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return TXDB.get("Press bounding points to fill.");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

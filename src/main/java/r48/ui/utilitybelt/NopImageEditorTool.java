/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.Rect;
import gabien.ui.UIElement;

/**
 * Created on 14th July 2018
 */
public class NopImageEditorTool implements IImageEditorTool {
    @Override
    public void enter(UIImageEditView uiev) {

    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, UIImageEditView view, boolean major, boolean dragging) {

    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, NopImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return "";
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }

}

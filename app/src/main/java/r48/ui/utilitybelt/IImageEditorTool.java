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
 * Created on 13th July 2018.
 */
public interface IImageEditorTool {
    // Solely for forcing a switch to a different tool. Don't reset here!
    void forceDifferentTool(UIImageEditView uiev);

    // major means an actual specified point (otherwise a between point)
    // dragging means NOT the first point
    void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging);

    // Must always be called after an apply sequence, and must always go to the correct tool.
    void endApply(UIImageEditView view);

    UIElement createToolPalette(UIImageEditView uiev);

    // Null: No selection
    // Zero W/H: Target
    Rect getSelection();

    String getLocalizedText(boolean dedicatedDragControl);

    // If not null, we're in camera mode
    IImageEditorTool getCamModeLT();
}

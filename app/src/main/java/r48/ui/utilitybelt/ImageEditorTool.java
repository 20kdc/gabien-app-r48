/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import r48.App;

/**
 * Created on 13th July 2018. Abstract-class 9th March 2023.
 */
public abstract class ImageEditorTool extends App.Svc {
    public ImageEditorTool(App a) {
        super(a);
    }

    // Solely for forcing a switch to a different tool. Don't reset here!
    public abstract void forceDifferentTool(UIImageEditView uiev);

    // major means an actual specified point (otherwise a between point)
    // dragging means NOT the first point
    public abstract void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging);

    // Must always be called after an apply sequence, and must always go to the correct tool.
    public abstract void endApply(UIImageEditView view);

    public abstract UIElement createToolPalette(UIImageEditView uiev);

    // Null: No selection
    // Zero W/H: Target
    public abstract Rect getSelection();

    public abstract String getLocalizedText(boolean dedicatedDragControl);

    // If not null, we're in camera mode
    public abstract ImageEditorTool getCamModeLT();
}

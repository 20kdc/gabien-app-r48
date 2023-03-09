/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPublicPanel;

/**
 * Created on 14th July 2018
 */
public class CamImageEditorTool extends ImageEditorTool {
    public final ImageEditorTool oldTool;

    public CamImageEditorTool(@NonNull ImageEditorTool currentTool) {
        super(currentTool.app);
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
        return app.ts("Drag: Move around, Camera: Return to old tool");
    }

    @Override
    public ImageEditorTool getCamModeLT() {
        return oldTool;
    }

}

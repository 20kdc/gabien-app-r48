/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.uslx.append.*;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.App;

/**
 * Created on 16th July 2018
 */
public class AddColourFromImageEditorTool extends ImageEditorTool {
    public final IConsumer<Integer> result;

    public AddColourFromImageEditorTool(App a, IConsumer<Integer> finished) {
        super(a);
        result = finished;
    }

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {
        FillAlgorithm.Point p = view.correctPoint(x, y);
        if (p == null)
            return;
        result.accept(view.image.getRGB(p.x, p.y));
        view.currentTool = new RootImageEditorTool(app);
        view.newToolCallback.run();
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, AddColourFromImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return T.ie.tdEyePal;
    }

    @Override
    public ImageEditorTool getCamModeLT() {
        return null;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.IConsumer;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.dbs.TXDB;

/**
 * Created on 16th July 2018
 */
public class AddColourFromImageEditorTool implements IImageEditorTool {
    public final IConsumer<Integer> result;

    public AddColourFromImageEditorTool(IConsumer<Integer> finished) {
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
        view.currentTool = new RootImageEditorTool();
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
        return TXDB.get("Touch a point to add a new palette entry for it.");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

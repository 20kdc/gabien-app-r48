/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.IFunction;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.dbs.TXDB;

/**
 * Created on October 09, 2018.
 */
public class FillImageEditorTool implements IImageEditorTool {
    public boolean tiled, autoshade;

    @Override
    public void enter(UIImageEditView uiev) {

    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, final UIImageEditView view, boolean major, boolean dragging) {
        view.eds.startSection();
        final int spi = view.image.getRaw(imp.correctedX, imp.correctedY);
        FillAlgorithm fa = new FillAlgorithm(new IFunction<FillAlgorithm.Point, Boolean>() {
            @Override
            public Boolean apply(FillAlgorithm.Point point) {
                int spi2 = view.image.getRaw(point.x, point.y);
                if (spi == spi2) {
                    view.image.setPixel(point.x, point.y, view.selPaletteIndex);
                    return true;
                }
                return false;
            }
        }, view.image.width, view.image.height);
        fa.availablePointSet.add(new FillAlgorithm.Point(imp.correctedX, imp.correctedY));
        while (!fa.availablePointSet.isEmpty())
            fa.pass();
        view.eds.endSection();
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, FillImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return TXDB.get("Press to fill area.");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

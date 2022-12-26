/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import r48.dbs.TXDB;

/**
 * Created on October 09, 2018.
 */
public class LineImageEditorTool extends StagedImageEditorTool {

    public LineImageEditorTool() {
        super(2);
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, LineImageEditorTool.class);
    }

    @Override
    protected void performOperation(final UIImageEditView view) {
        view.eds.startSection();
        final LineAlgorithm la = new LineAlgorithm();
        la.ax = stageXs[0];
        la.ay = stageYs[0];
        la.run(stageXs[1], stageYs[1], new IFunction<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean aBoolean) {
                FillAlgorithm.Point p = view.correctPoint(la.ax, la.ay);
                if (p != null)
                    view.image.setPixel(p.x, p.y, view.selPaletteIndex);
                return true;
            }
        });
        view.eds.endSection();
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (stage == 0)
            return TXDB.get("Press to start line.");
        return TXDB.get("Press to end line.");
    }
}

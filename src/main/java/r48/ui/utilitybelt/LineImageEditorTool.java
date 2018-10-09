/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.IFunction;
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
        final UIImageEditView.ImPoint imp = new UIImageEditView.ImPoint(0, 0);
        la.run(stageXs[1], stageYs[1], new IFunction<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean aBoolean) {
                imp.x = la.ax;
                imp.y = la.ay;
                imp.updateCorrected(view);
                view.image.setPixel(imp.correctedX, imp.correctedY, view.selPaletteIndex);
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

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.Rect;
import r48.App;

/**
 * Created on October 09, 2018.
 */
public abstract class StagedImageEditorTool extends ImageEditorTool {
    protected int[] stageXs, stageYs;
    protected int stage;

    public StagedImageEditorTool(App app, int stageCount) {
        super(app);
        stageXs = new int[stageCount];
        stageYs = new int[stageCount];
    }

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {
        if (dragging)
            return;
        if (!major)
            return;
        stageXs[stage] = x;
        stageYs[stage] = y;
        stage++;
        if (stage >= stageXs.length) {
            performOperation(view);
            stage = 0;
        }
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    protected abstract void performOperation(UIImageEditView view);

    protected Rect getRectWithPoints() {
        int tx = Math.min(stageXs[0], stageXs[1]);
        int ty = Math.min(stageYs[0], stageYs[1]);
        int t2x = Math.max(stageXs[0], stageXs[1]);
        int t2y = Math.max(stageYs[0], stageYs[1]);
        return new Rect(tx, ty, (t2x - tx) + 1, (t2y - ty) + 1);
    }

    @Override
    public Rect getSelection() {
        if (stage == 1)
            return new Rect(stageXs[0], stageYs[0], 0, 0);
        if (stage >= 2)
            return getRectWithPoints();
        return null;
    }

    @Override
    public ImageEditorTool getCamModeLT() {
        return null;
    }
}

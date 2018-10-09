package r48.ui.utilitybelt;

import gabien.ui.Rect;

/**
 * Created on October 09, 2018.
 */
public abstract class StagedImageEditorTool implements IImageEditorTool {
    protected int[] stageXs, stageYs;
    protected int stage;

    public StagedImageEditorTool(int stageCount) {
        stageXs = new int[stageCount];
        stageYs = new int[stageCount];
    }

    @Override
    public void enter(UIImageEditView uiev) {
        stage = 0;
    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, UIImageEditView view, boolean major, boolean dragging) {
        if (dragging)
            return;
        if (!major)
            return;
        stageXs[stage] = imp.x;
        stageYs[stage] = imp.y;
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
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

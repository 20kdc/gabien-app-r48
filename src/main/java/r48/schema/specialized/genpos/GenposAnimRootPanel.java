/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.ui.Rect;
import gabien.ui.UIPanel;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;

/**
 * Animation Software For Serious Animation Purposes.
 * ...for stick figure animation. Ignore the 'RM'.
 * AAAA
 * --+-
 * BB|C
 * BB|C
 * A: timeframe
 * B: Frame Editor
 * C: Cell Editor
 * The 3-pane layout is controlled entirely from this class. Good luck.
 * Created on 2/17/17.
 */
public class GenposAnimRootPanel extends UIPanel {
    public IGenposAnim target;
    public GenposFramePanelController framePanelController;
    public UITimeframeControl timeframe;

    public GenposAnimRootPanel(IGenposAnim t, ISchemaHost launcher, int recommendedFramerate) {
        target = t;
        // Stop animation elements escaping the window
        useScissoring = true;

        framePanelController = new GenposFramePanelController(target.getFrameDisplay(), launcher);
        timeframe = new UITimeframeControl(this, recommendedFramerate);

        allElements.add(timeframe);
        allElements.add(framePanelController.rootLayout);

        frameChanged();
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int th = timeframe.getBounds().height;
        timeframe.setBounds(new Rect(0, 0, r.width, th));
        framePanelController.rootLayout.setBounds(new Rect(0, th, r.width, r.height - th));
    }

    @Override
    public String toString() {
        return TXDB.get("Animation Editor");
    }

    // This alerts everything to rebuild, but doesn't run the updateNotify.
    // Use alone for things like advancing through frames.
    public void frameChanged() {
        // This does bounds checks
        target.setFrameIdx(target.getFrameIdx());
        // Actually start alerting things
        framePanelController.frameChanged();
    }
}

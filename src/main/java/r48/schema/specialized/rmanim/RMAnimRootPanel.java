/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.ui.UINSVertLayout;

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
public class RMAnimRootPanel extends UIPanel {
    public RubyIO target;
    public Runnable updateNotify;
    public String framesetALoc, framesetBLoc;
    // 0-99: Set A 100-199: Set B
    public IGrInDriver.IImage framesetCacheA, framesetCacheB;
    public UINSVertLayout editingSidebar;
    public UICellSelectionPanel cellSelection;
    public UISingleFrameView editor = new UISingleFrameView(this);
    public UITimeframeControl timeframe = new UITimeframeControl(this);
    public int frameIdx = 0;
    public RMAnimRootPanel(RubyIO t, Runnable runnable, String a, String b) {
        target = t;
        updateNotify = runnable;
        framesetALoc = a;
        framesetBLoc = b;
        // Stop animation elements escaping the window
        useScissoring = true;
        allElements.add(editor);

        allElements.add(timeframe);

        cellSelection = new UICellSelectionPanel(this);
        // The UICellEditingPanel is informed about frame changes via UICellSelectionPanel
        editingSidebar = new UINSVertLayout(new UICellEditingPanel(cellSelection, this), cellSelection);
        // Set an absolute width for the editing sidebar
        editingSidebar.setBounds(new Rect(0, 0, 128, 32));

        allElements.add(editingSidebar);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int th = timeframe.getBounds().height;
        int esb = editingSidebar.getBounds().width;
        timeframe.setBounds(new Rect(0, 0, r.width, th));
        editor.setBounds(new Rect(0, th, r.width - esb, r.height - th));
        editingSidebar.setBounds(new Rect(r.width - esb, th, esb, r.height - th));
    }

    public RubyIO getFrame() {
        RubyIO[] frames = target.getInstVarBySymbol("@frames").arrVal;
        if (frameIdx < 0)
            frameIdx = frames.length - 1;
        if (frameIdx >= frames.length)
            frameIdx = 0;
        return frames[frameIdx];
    }

    @Override
    public String toString() {
        return "RMAnim Player";
    }

    public int getScaledImageIconSize(int scale) {
        return (int) (192 * (scale / 100.0d));
    }

    public void prepareFramesetCache() {
        String nameA = target.getInstVarBySymbol(framesetALoc).decString();
        String nameB = target.getInstVarBySymbol(framesetBLoc).decString();
        framesetCacheA = null;
        framesetCacheB = null;
        if (nameA.length() != 0)
            framesetCacheA = GaBIEn.getImage(AppMain.rootPath + "Graphics/Animations/" + nameA + ".png", 0, 0, 0);
        if (nameB.length() != 0)
            framesetCacheB = GaBIEn.getImage(AppMain.rootPath + "Graphics/Animations/" + nameB + ".png", 0, 0, 0);
    }

    public void insertFrameCopy() {
        AppMain.launchDialog("TODO");
    }

    public void deleteFrame() {
        AppMain.launchDialog("TODO");
    }

    // This alerts everything to rebuild, but doesn't run the updateNotify.
    // Use for things like advancing through frames.
    public void frameChanged() {
        // This does bounds checks
        getFrame();
        // Actually start alerting things
        cellSelection.frameChanged();
    }
}

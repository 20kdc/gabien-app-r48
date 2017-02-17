/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.RubyIO;

/**
 * Animation Software For Serious Animation Purposes.
 * ...for stick figure animation. Ignore the 'RM'.
 * Created on 2/17/17.
 */
public class RMAnimRootPanel extends UIPanel {
    public RubyIO target;
    public Runnable updateNotify;
    public String framesetALoc, framesetBLoc;
    public UISingleFrameEditor editor = new UISingleFrameEditor(this);
    public int frameIdx = 0;
    public double playTimer = 0;
    public RMAnimRootPanel(RubyIO t, Runnable runnable, String a, String b) {
        target = t;
        updateNotify = runnable;
        framesetALoc = a;
        framesetBLoc = b;
        useScissoring = true;
        allElements.add(editor);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        editor.setBounds(new Rect(0, 0, r.width, r.height));
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
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        playTimer += deltaTime;
        if (playTimer >= 0.1d) {
            playTimer -= 0.1;
            frameIdx++;
        }
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }

    @Override
    public String toString() {
        return "UNFINISHED FUNC";
    }
}

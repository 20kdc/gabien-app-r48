/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos.backend;

import r48.AppMain;
import r48.ArrayUtils;
import r48.RubyIO;
import r48.schema.specialized.genpos.IGenposAnim;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.util.SchemaPath;

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
 * <p/>
 * -- POST-GENPOS-REFACTOR --
 * Ok, what happens here is:
 * This class handles the @frames array (which must contain "RPG::Animation::Frame"-class objects).
 * (The reason it doesn't handle the parent object is because of magical bindings. Thanks to FT3.)
 * It delegates all the actual per-frame work to the frame handler you give it.
 * This lets this class manage both 2k3-era and RGSS-era animation objects,
 * while still letting the details in the frames (which are vastly different)
 * be handled elsewhere.
 * <p/>
 * Created on 2/17/17.
 */
public class RMGenposAnim implements IGenposAnim {
    // NOTE: This can be updated, and this is relied upon for cases where a magical binding is closely linked.
    public RubyIO target;
    public IGenposFrame perFrame;
    public Runnable updateNotify;
    public int frameIdx = 0;
    public boolean ix1 = false;

    public RMGenposAnim(RubyIO t, IGenposFrame frameHandler, Runnable runnable, boolean index) {
        perFrame = frameHandler;
        target = t;
        updateNotify = runnable;
        ix1 = index;

        setFrameIdx(getFrameIdx());
    }

    public RubyIO getFrame() {
        RubyIO[] frames = target.arrVal;
        int min = 0;
        if (ix1)
            min = 1;
        if (frames.length <= min) {
            // Add nulls if necessary
            while (frames.length < min) {
                ArrayUtils.insertRioElement(target, new RubyIO().setNull(), 0);
                frames = target.arrVal;
            }
            // Create a frame from scratch to avoid crashing
            RubyIO copy = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Animation::Frame"), null);
            frameIdx = min - 1;
            insertFrame(copy);
            return copy;
        }
        if (frameIdx < 0)
            frameIdx = frames.length - (1 + min);
        if (frameIdx >= (frames.length - min))
            frameIdx = 0;
        return frames[frameIdx + min];
    }

    @Override
    public void insertFrame(RubyIO source) {
        ArrayUtils.insertRioElement(target, source, frameIdx + 1);
        updateNotify.run();
        frameIdx++;
    }

    @Override
    public void deleteFrame() {
        ArrayUtils.removeRioElement(target, frameIdx);
        updateNotify.run();
        frameIdx--;
        getFrame();
    }

    @Override
    public IGenposFrame getFrameDisplay() {
        return perFrame;
    }

    @Override
    public boolean acceptableForPaste(RubyIO theClipboard) {
        if (AppMain.theClipboard.type == 'o')
            if (AppMain.theClipboard.symVal.equals("RPG::Animation::Frame"))
                return true;
        return false;
    }

    @Override
    public void modifiedFrame() {
        updateNotify.run();
    }

    @Override
    public void setFrameIdx(int i) {
        frameIdx = i;
        getFrame();
    }

    @Override
    public int getFrameIdx() {
        return frameIdx;
    }

    @Override
    public int getFrameCount() {
        int min = 0;
        if (ix1)
            min = 1;
        return target.arrVal.length - min;
    }
}

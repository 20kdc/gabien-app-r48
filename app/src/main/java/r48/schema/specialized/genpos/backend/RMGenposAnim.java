/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos.backend;

import r48.R48;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
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
public class RMGenposAnim extends R48.Svc implements IGenposAnim {
    // NOTE: This can be updated, and this is relied upon for cases where a magical binding is closely linked.
    public IRIO target;
    public IGenposFrame perFrame;
    public Runnable updateNotify;
    public int frameIdx;
    public boolean ix1;

    public RMGenposAnim(R48 app, IRIO t, IGenposFrame frameHandler, Runnable runnable, boolean index) {
        super(app);
        perFrame = frameHandler;
        target = t;
        updateNotify = runnable;
        ix1 = index;

        setFrameIdx(getFrameIdx());
    }

    @Override
    public boolean isStillValid() {
        return target.getType() == '[';
    }

    public IRIO getFrame() {
        int min = 0;
        if (ix1)
            min = 1;
        if (target.getALen() <= min) {
            // Add nulls if necessary
            while (target.getALen() < min)
                target.addAElem(0);
            // Create a frame from scratch to avoid crashing
            IRIO copy = new IRIOGeneric(app.ctxDisposableAppEncoding).setNull();
            SchemaPath.setDefaultValue(copy, app.sdb.getSDBEntry("RPG::Animation::Frame"), null);
            frameIdx = min - 1;
            insertFrame(copy);
            return copy;
        }
        if (frameIdx < 0)
            frameIdx = target.getALen() - (1 + min);
        if (frameIdx >= (target.getALen() - min))
            frameIdx = 0;
        return target.getAElem(frameIdx + min);
    }

    @Override
    public void insertFrame(RORIO source) {
        target.addAElem(frameIdx + 1).setDeepClone(source);
        updateNotify.run();
        frameIdx++;
    }

    @Override
    public void deleteFrame() {
        target.rmAElem(frameIdx);
        updateNotify.run();
        frameIdx--;
        getFrame();
    }

    @Override
    public IGenposFrame getFrameDisplay() {
        return perFrame;
    }

    @Override
    public boolean acceptableForPaste(RORIO theClipboard) {
        return !SchemaElement.checkType(theClipboard, 'o', "RPG::Animation::Frame", false);
    }

    @Override
    public void modifiedFrames() {
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
        if (target.getType() != '[')
            return 0;
        int min = 0;
        if (ix1)
            min = 1;
        return target.getALen() - min;
    }
}

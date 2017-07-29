/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos.backend;

import gabien.IGrInDriver;
import r48.AppMain;
import r48.ArrayUtils;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBindingSchemaElement;
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
 *
 * -- POST-GENPOS-REFACTOR --
 * Ok, what happens here is:
 * This class handles some generic object with an @frames array (which must contain "RPG::Animation::Frame"-class objects).
 * It delegates all the actual per-frame work to the frame handler you give it.
 * This lets this class manage both 2k3-era and RGSS-era animation objects,
 *  while still letting the details in the frames (which are vastly different)
 *  be handled elsewhere.
 *
 * Created on 2/17/17.
 */
public class RMGenposAnim implements IGenposAnim {
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
        RubyIO[] frames = target.getInstVarBySymbol("@frames").arrVal;
        int min = 0;
        if (ix1)
            min = 1;
        if (frames.length <= min) {
            // Add nulls if necessary
            while (frames.length < min) {
                ArrayUtils.insertRioElement(target.getInstVarBySymbol("@frames"), new RubyIO().setNull(), 0);
                frames = target.getInstVarBySymbol("@frames").arrVal;
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
        ArrayUtils.insertRioElement(target.getInstVarBySymbol("@frames"), source, frameIdx + 1);
        updateNotify.run();
        frameIdx++;
    }

    @Override
    public void deleteFrame() {
        ArrayUtils.removeRioElement(target.getInstVarBySymbol("@frames"), frameIdx);
        updateNotify.run();
        frameIdx--;
        getFrame();
    }

    @Override
    public IGenposFrame getFrameDisplay() {
        return perFrame;
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
        return target.getInstVarBySymbol("@frames").arrVal.length - min;
    }
}

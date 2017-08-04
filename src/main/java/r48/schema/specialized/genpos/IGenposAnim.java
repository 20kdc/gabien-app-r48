/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import r48.RubyIO;

/**
 * Takes over some of RMAnimRootPanel's duties.
 * Created on 29/07/17.
 */
public interface IGenposAnim {
    void setFrameIdx(int i);

    int getFrameIdx();

    int getFrameCount();

    // If the frame is modified directly, call modifiedFrame
    RubyIO getFrame();

    void modifiedFrame();

    // NOTE: You need to setFrameIdx(getFrameIdx()); after these.
    void insertFrame(RubyIO rio);

    void deleteFrame();

    // Only called once - the separation helps keep the code sane.
    IGenposFrame getFrameDisplay();
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

import r48.io.data.IRIO;
import r48.io.data.RORIO;

/**
 * Takes over some of RMAnimRootPanel's duties.
 * Created on 29/07/17.
 */
public interface IGenposAnim {
    /**
     * Confirms that the data still appears valid.
     * This has to catch undo shenanigans, but not that much else.
     */
    boolean isStillValid();

    void setFrameIdx(int i);

    int getFrameIdx();

    int getFrameCount();

    // If the frame is modified directly, call modifiedFrames.
    // Note that multiple frames can be modified before modifiedFrames is called.
    IRIO getFrame();

    void modifiedFrames();

    // NOTE: You need to setFrameIdx(getFrameIdx()); after these. modifiedFrames() is implicitly called.
    void insertFrame(RORIO rio);

    void deleteFrame();

    // Only called once - the separation helps keep the code sane.
    IGenposFrame getFrameDisplay();

    boolean acceptableForPaste(RORIO theClipboard);
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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

    boolean acceptableForPaste(RubyIO theClipboard);
}

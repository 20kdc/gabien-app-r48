/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

import java.util.function.Function;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.uslx.append.*;
import r48.schema.util.SchemaPath;

/**
 * Part of genpos.
 * The root panel is still controlled by the target-specific stuff.
 * Created on 28/07/17.
 */
public interface IGenposFrame {
    /**
     * Verifies the frame object is still valid.
     * This has to catch undo shenanigans, but not that much else.
     */
    boolean isStillValid();

    // Interleaved X/Y. Provides position markers.
    int[] getIndicators();

    boolean canAddRemoveCells();

    void addCell(int i2);

    void deleteCell(int i2);

    // Note: The target will be modified.
    // targetElement and the path itself should be used by the caller.
    // Use newWindow.
    SchemaPath getCellProp(int ct, int i);

    // Returns null for non-tweenable properties.
    // Semantics of access to this are the same as with getFrame() in GenposAnim.
    IGenposTweeningProp getCellPropTweening(int ct, int i);

    void moveCell(int ct, Function<Integer, Integer> x, Function<Integer, Integer> y);

    int getCellCount();

    String[] getCellProps();

    Rect getCellSelectionIndicator(int i);

    void drawCell(int i, int opx, int opy, IGrDriver igd);

    IImage getBackground();
}

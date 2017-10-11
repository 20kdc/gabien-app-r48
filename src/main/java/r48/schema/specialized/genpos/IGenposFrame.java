/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ui.IFunction;
import gabien.ui.Rect;
import r48.schema.util.SchemaPath;

/**
 * Part of genpos.
 * The root panel is still controlled by the target-specific stuff.
 * Created on 28/07/17.
 */
public interface IGenposFrame {

    // Interleaved X/Y. Provides position markers.
    int[] getIndicators();

    boolean canAddRemoveCells();

    void addCell(int i2);

    void deleteCell(int i2);

    // Note: The target will be modified.
    // targetElement and the path itself should be used by the caller.
    // Use newWindow.
    SchemaPath getCellProp(int ct, int i);

    void moveCell(int ct, IFunction<Integer, Integer> x, IFunction<Integer, Integer> y);

    int getCellCount();

    String[] getCellProps();

    Rect getCellSelectionIndicator(int i);

    void drawCell(int i, int opx, int opy, IGrInDriver igd);

    IImage getBackground();
}

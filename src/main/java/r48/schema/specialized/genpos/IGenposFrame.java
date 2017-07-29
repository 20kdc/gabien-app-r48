/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.ui.IFunction;
import gabien.ui.Rect;
import r48.RubyIO;
import r48.schema.SchemaElement;
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
    // Use newWindow with a null launcher. (This has to be corrected by caller.)
    SchemaPath getCellProp(int ct, int i);
    void moveCell(int ct, IFunction<Integer, Integer> x, IFunction<Integer, Integer> y);

    int getCellCount();

    String[] getCellProps();

    Rect getCellSelectionIndicator(int i);
    void drawCell(int i, int opx, int opy, IGrInDriver igd);

    IGrInDriver.IImage getBackground();

    // Use FramePanelController's frameChanged()
    // void setFrameChangeHandler(Runnable r);
}

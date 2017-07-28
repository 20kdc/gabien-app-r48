/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import r48.RubyIO;

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

    int getCellProp(int ct, int i);
    void setCellProp(int ct, int i, int number);

    int getCellCount();

    String[] getCellProps();

    void drawCell(int i, int opx, int opy, IGrInDriver igd);

    IGrInDriver.IImage getBackground();

    // Use FramePanelController's frameChanged()
    // void setFrameChangeHandler(Runnable r);
}

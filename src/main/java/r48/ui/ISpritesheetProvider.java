/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;

/**
 * Created on 29/07/17.
 */
public interface ISpritesheetProvider {
    int itemWidth();
    int itemHeight();
    int itemCount();
    void drawItem(int t, int x, int y, IGrInDriver igd);
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrDriver;
import gabien.IGrInDriver;

/**
 * Used for map-view-tools.
 * Created on 12/28/16.
 */
public interface IMapViewCallbacks {
    short shouldDrawAtCursor(short there, int layer, int currentLayer);

    int wantOverlay(boolean minimap);

    void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap);

    void confirmAt(int x, int y, int layer);

    boolean shouldIgnoreDrag();
}

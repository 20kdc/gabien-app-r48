/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrDriver;

/**
 * Used for map-view-tools.
 * Created on 12/28/16.
 */
public interface IMapViewCallbacks {
    short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer);

    int wantOverlay(boolean minimap);

    void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap);
    void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize);

    void confirmAt(int x, int y, int layer);

    boolean shouldIgnoreDrag();
}

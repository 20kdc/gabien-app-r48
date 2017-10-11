/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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

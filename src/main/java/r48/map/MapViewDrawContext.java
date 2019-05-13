/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map;

import gabien.IGrDriver;

/**
 * A structure that contains the subset of parameters needed for map view draw layers/etc.
 * Created on November 15, 2018.
 */
public class MapViewDrawContext {
    public int camX, camY, camTX, camTY, camTR, camTB, tileSize, currentLayer;
    public IMapViewCallbacks callbacks;
    public boolean debugToggle;
    public IGrDriver igd;
    // Null if the mouse doesn't exist.
    public MouseStatus mouseStatus;

    public static class MouseStatus {
        public final boolean pressed;
        // In tiles.
        public final int x, y;

        public MouseStatus(boolean pressed, int x, int y) {
            this.pressed = pressed;
            this.x = x;
            this.y = y;
        }
    }
}

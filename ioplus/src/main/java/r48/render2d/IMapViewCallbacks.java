/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.render2d;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Used for map-view-tools.
 * Created on 12/28/16.
 */
public interface IMapViewCallbacks {
    default int shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, int there, int layer, int currentLayer) {
        return there;
    }

    void performGlobalOverlay(MapViewDrawContext mvdc, boolean minimap);

    void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first);

    /**
     * This returns state information to control map re-rendering.
     */
    @NonNull String viewState(int mouseXT, int mouseYT);
}

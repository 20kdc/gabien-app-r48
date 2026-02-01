/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.map.events.IEventGraphicRenderer;
import r48.render2d.MapViewDrawContext;

public class EventZSortedObject implements ZSortingDrawLayer.IZSortedObject {
    private final IRIO evI;
    private final long z;
    private final ZSortingDrawLayer.SignalMapViewLayer signal;
    private final IEventGraphicRenderer renderer;

    public EventZSortedObject(IRIO event, long z, ZSortingDrawLayer.SignalMapViewLayer sig, IEventGraphicRenderer iegr) {
        this.z = z;
        evI = event;
        signal = sig;
        renderer = iegr;
    }

    @Override
    public long getZ() {
        return z;
    }

    @Override
    public ZSortingDrawLayer.SignalMapViewLayer getControlSignal() {
        return signal;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        int x = (int) evI.getIVar("@x").getFX();
        int y = (int) evI.getIVar("@y").getFX();
        // Events vary in size - to stop the most obvious glitching, add some margin.
        if (!mvdc.camTMargin.contains(x, y))
            return;
        int px = x * mvdc.tileSize;
        int py = y * mvdc.tileSize;
        RORIO g = renderer.extractEventGraphic(evI);
        if (g != null)
            renderer.drawEventGraphic(g, px, py, mvdc.igd, 1, evI);
    }

}

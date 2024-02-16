package r48.map.drawlayers;

import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.map.MapViewDrawContext;
import r48.map.events.IEventGraphicRenderer;

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
            renderer.drawEventGraphic(g, px, py, mvdc.igd, 1);
    }

}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

import gabien.IGrDriver;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.events.IEventGraphicRenderer;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created on 08/06/17.
 */
public class EventMapViewDrawLayer implements IMapViewDrawLayer {
    public RubyIO eventList;
    public int layer;
    public IEventGraphicRenderer iegr;
    public int tileSize;

    public EventMapViewDrawLayer(int layer2, RubyIO eventL, IEventGraphicRenderer e, int ts) {
        eventList = eventL;
        layer = layer2;
        iegr = e;
        tileSize = ts;
    }

    @Override
    public String getName() {
        return TXDB.get("Events");
    }

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        if (eTileSize != tileSize)
            return;
        // Event Enable
        // Having it here is more efficient than having it as a tool overlay,
        // and sometimes the user might want to see events when using other tools.
        LinkedList<RubyIO> ev = new LinkedList<RubyIO>(eventList.hashVal.values());
        ev.sort(new Comparator<RubyIO>() {
            @Override
            public int compare(RubyIO a, RubyIO b) {
                int yA = (int) a.getInstVarBySymbol("@y").fixnumVal;
                int yB = (int) b.getInstVarBySymbol("@y").fixnumVal;
                if (yA < yB)
                    return -1;
                if (yA > yB)
                    return 1;
                return 0;
            }
        });
        for (RubyIO evI : ev) {
            if (iegr.determineEventLayer(evI) != layer)
                continue;
            int x = (int) evI.getInstVarBySymbol("@x").fixnumVal;
            int y = (int) evI.getInstVarBySymbol("@y").fixnumVal;
            if (x < camTX)
                continue;
            if (y < camTY)
                continue;
            if (x >= camTR)
                continue;
            if (y >= camTB)
                continue;
            int px = (x * eTileSize) - camX;
            int py = (y * eTileSize) - camY;
            RubyIO g = iegr.extractEventGraphic(evI);
            if (g != null)
                iegr.drawEventGraphic(g, px, py, igd);
        }
    }
}

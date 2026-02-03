/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.ioplus.RenderArt;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map2d.MapViewDrawContext;
import r48.map2d.layers.MapViewDrawLayer;
import r48.tr.pages.TrRoot;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created on 08/06/17.
 */
public class EventMapViewDrawLayer extends MapViewDrawLayer {
    /**
     * MAY BE POKED BY EXTERNAL CODE
     * DO NOT MAKE FINAL OR RENAME
     */
    public IEventAccess eventList;
    public final int layer;
    /**
     * MAY BE POKED BY EXTERNAL CODE
     * DO NOT MAKE FINAL OR RENAME
     */
    public IEventGraphicRenderer iegr;
    public final RenderArt art;

    public EventMapViewDrawLayer(RenderArt art, TrRoot t, int layer, IEventAccess eventL, IEventGraphicRenderer e, String post) {
        super(layer == 0x7FFFFFFF ? t.m.l_evSel : (eventL.customEventsName() + post));
        this.art = art;
        eventList = eventL;
        this.layer = layer;
        iegr = e;
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        // Event Enable
        // Having it here is more efficient than having it as a tool overlay,
        // and sometimes the user might want to see events when using other tools.
        LinkedList<DMKey> ev = eventList.getEventKeys();
        Collections.sort(ev, new Comparator<DMKey>() {
            @Override
            public int compare(DMKey a, DMKey b) {
                int yA = (int) eventList.getEventY(a);
                int yB = (int) eventList.getEventY(b);
                if (yA < yB)
                    return -1;
                if (yA > yB)
                    return 1;
                return 0;
            }
        });
        for (DMKey evK : ev) {
            int x = (int) eventList.getEventX(evK);
            int y = (int) eventList.getEventY(evK);
            if (!mvdc.camTMargin.contains(x, y))
                continue;
            IRIO evI = eventList.getEvent(evK);
            if (evI == null)
                continue;
            int px = x * mvdc.tileSize;
            int py = y * mvdc.tileSize;
            if (layer == 0x7FFFFFFF) {
                if (mvdc.currentlyOpenInEditor(evI))
                    art.drawSelectionBox(px - 1, py - 1, mvdc.tileSize + 2, mvdc.tileSize + 2, 1, mvdc.igd);
            } else {
                if (iegr.determineEventLayer(evI) != layer)
                    continue;
                RORIO g = iegr.extractEventGraphic(evI);
                if (g != null)
                    iegr.drawEventGraphic(g, px, py, mvdc.igd, 1, evI);
            }
        }
    }
}

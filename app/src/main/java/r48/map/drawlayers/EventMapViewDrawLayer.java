/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.map.MapViewDrawContext;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.ui.Art;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created on 08/06/17.
 */
public class EventMapViewDrawLayer extends App.Svc implements IMapViewDrawLayer {
    public IEventAccess eventList;
    public int layer;
    public IEventGraphicRenderer iegr;
    public String postfix;

    public EventMapViewDrawLayer(App app, int layer2, IEventAccess eventL, IEventGraphicRenderer e, String post) {
        super(app);
        eventList = eventL;
        layer = layer2;
        iegr = e;
        postfix = post;
    }

    @Override
    public String getName() {
        if (layer == 0x7FFFFFFF)
            return T.m.l213;
        return eventList.customEventsName() + postfix;
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
                if (mvdc.app.ui.currentlyOpenInEditor(evI))
                    Art.drawSelectionBox(px - 1, py - 1, mvdc.tileSize + 2, mvdc.tileSize + 2, 1, mvdc.igd);
            } else {
                if (iegr.determineEventLayer(evI) != layer)
                    continue;
                IRIO g = iegr.extractEventGraphic(evI);
                if (g != null)
                    iegr.drawEventGraphic(g, px, py, mvdc.igd, 1);
            }
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import r48.AppMain;
import r48.RubyIO;
import r48.dbs.TXDB;
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
public class EventMapViewDrawLayer implements IMapViewDrawLayer {
    public IEventAccess eventList;
    public int layer;
    public IEventGraphicRenderer iegr;
    public String postfix;

    public EventMapViewDrawLayer(int layer2, IEventAccess eventL, IEventGraphicRenderer e, String post) {
        eventList = eventL;
        layer = layer2;
        iegr = e;
        postfix = post;
    }

    @Override
    public String getName() {
        if (layer == 0x7FFFFFFF)
            return TXDB.get("Event Selection");
        return eventList.customEventsName() + postfix;
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        // Event Enable
        // Having it here is more efficient than having it as a tool overlay,
        // and sometimes the user might want to see events when using other tools.
        LinkedList<RubyIO> ev = eventList.getEventKeys();
        Collections.sort(ev, new Comparator<RubyIO>() {
            @Override
            public int compare(RubyIO a, RubyIO b) {
                int yA = (int) eventList.getEventY(a);
                int yB = (int) eventList.getEventY(b);
                if (yA < yB)
                    return -1;
                if (yA > yB)
                    return 1;
                return 0;
            }
        });
        for (RubyIO evK : ev) {
            int x = (int) eventList.getEventX(evK);
            int y = (int) eventList.getEventY(evK);
            if (x < mvdc.camTX)
                continue;
            if (y < mvdc.camTY)
                continue;
            if (x >= mvdc.camTR)
                continue;
            if (y >= mvdc.camTB)
                continue;
            RubyIO evI = eventList.getEvent(evK);
            if (evI == null)
                continue;
            int px = (x * mvdc.tileSize) - mvdc.camX;
            int py = (y * mvdc.tileSize) - mvdc.camY;
            if (layer == 0x7FFFFFFF) {
                if (AppMain.currentlyOpenInEditor(evI))
                    Art.drawSelectionBox(px - 1, py - 1, mvdc.tileSize + 2, mvdc.tileSize + 2, 1, mvdc.igd);
            } else {
                if (iegr.determineEventLayer(evI) != layer)
                    continue;
                RubyIO g = iegr.extractEventGraphic(evI);
                if (g != null)
                    iegr.drawEventGraphic(g, px, py, mvdc.igd, 1);
            }
        }
    }
}

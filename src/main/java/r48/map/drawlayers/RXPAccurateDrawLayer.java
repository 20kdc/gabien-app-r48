/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import r48.RubyTable;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.map.MapViewDrawContext;
import r48.map.events.IEventAccess;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.tiles.XPTileRenderer;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * After looking closely at the system as part of the "reimagined-pancake" writeup,
 * I now understand RPG Maker XP's system, to some extent.
 * And now I am going to emulate it in R48 as perfectly as possible.
 * It's heavily reliant on the ability to lay everything out with precise Z control.
 * Keeping this in mind, I continue regardless - it is still impossible to produce the failure case for Z-sorting,
 * and so long as that is the case, I can still do this.
 * <p>
 * Note that this is heavily reliant on the draw layers being recreated on any change.
 * "I know you don't know - how could you? But I've seen these things before."
 * Created on 18th January 2018.
 */
public class RXPAccurateDrawLayer extends RMZAccurateDrawLayer {
    public final XPTileRenderer tiles;
    public final RMEventGraphicRenderer events;

    public final ZSortingDrawLayer.SignalMapViewLayer signalLayerEvA = new ZSortingDrawLayer.SignalMapViewLayer(TXDB.get("Event Layers (lower)"));
    public final ZSortingDrawLayer.SignalMapViewLayer signalLayerEvB = new ZSortingDrawLayer.SignalMapViewLayer(TXDB.get("Event Layers (upper)"));

    private static final int[] layerPreference = new int[] {0, 1, 2};

    public RXPAccurateDrawLayer(RubyTable tbl, IEventAccess eventList, XPTileRenderer tils, RMEventGraphicRenderer ev) {
        super(tbl, tbl.planeCount);
        tiles = tils;
        events = ev;
        signals.add(signalLayerEvA);
        signals.add(signalLayerEvB);
        // -1 is the "ground plane".
        for (int i = -1; i < tbl.height + 5; i++)
            zSorting.add(new RXPPriorityPlane(i));
        // Very specific choice of algorithm.
        for (IRIO r : eventList.getEventKeys()) {
            IRIO ed = eventList.getEvent(r);
            // Determine Z location.
            boolean isUpper = events.determineEventLayer(ed) != 0;
            // Since the "screen height" rules aren't *quite* being properly held to,
            //  use a suitable replacement for 999.
            long z = Long.MAX_VALUE;
            if (!isUpper) {
                // OS/"ground 1" is great at catching off-by-X errors here.
                // In particular, look at where the street lamps touch the fence,
                //  and how window lights interact with street lamps.
                // This routine needs to be well-calibrated.
                // Another thing to check is RQ/Level 5/Hall 1.
                // Note that while RQ should be a good stress test for this system,
                //  some maps, such as Map055, appear on TID examination to be incomplete.
                z = ((ed.getIVar("@y").getFX() + 1) * 2) + 1;
                IRIO edG = events.extractEventGraphic(ed);
                if (edG != null) {
                    long tid = edG.getIVar("@tile_id").getFX();
                    if (tid != 0) {
                        // Get priority...
                        z += getTIDPriority((short) tid);
                    } else {
                        /*
                        IImage i = events.imageLoader.getImage("Characters/" + edG.getInstVarBySymbol("@character_name").decString(), false);
                        int sprH = i.getHeight() / (4 * 32);
                        //z -= sprH; // causes more trouble than it's worth
                        if (sprH > 1)
                            z += 2;*/
                    }
                }
            }
            zSorting.add(new EventZSortedObject(ed, z, isUpper ? signalLayerEvB : signalLayerEvA, events));
        }
        completeSetup();
    }

    // MKXP bounds between 0 and 5, this is used for optimisation but is otherwise not relied upon by this code.
    private int getTIDPriority(short tid) {
        RubyTable rts = tiles.priorities;
        if (rts == null)
            return 0;
        if (tid < 0)
            return 0;
        if (tid >= rts.width)
            return 0;
        return rts.getTiletype(tid, 0, 0);
    }

    @Override
    public String getName() {
        return TXDB.get("XP Tile/Event Z-Emulation Layer");
    }

    private class RXPPriorityPlane extends TileMapViewDrawLayer implements IZSortedObject {
        // priority + tile Y
        public final int pIndex;

        public RXPPriorityPlane(int p) {
            super(mapTable, layerPreference, tiles, "INTERNAL - YOU SHOULD NOT SEE THIS");
            pIndex = p;
        }

        @Override
        public long getZ() {
            return (pIndex + 1) * 2;
        }

        @Override
        public SignalMapViewLayer getControlSignal() {
            return null;
        }

        @Override
        public boolean shouldDrawRow(int y, int layer) {
            // Optimisation to counteract the large amount of layers created by this.
            if (pIndex < y)
                return false;
            if (pIndex > (y + 5))
                return false;
            return true;
        }

        @Override
        public boolean shouldDraw(int x, int y, int layer, short value) {
            if (!tileSignalLayers[layer].active)
                return false;
            int pri = getTIDPriority(value);
            if (pIndex == -1)
                if (pri == 0)
                    return true;
            int targPIndex = y + pri;
            return targPIndex == pIndex;
        }
    }
}

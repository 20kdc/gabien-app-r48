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
import r48.map.tiles.VXATileRenderer;
import r48.map.tiles.XPTileRenderer;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Despite the name, right now this is stitched together to solve some bugs.
 * So it doesn't really represent full accuracy. (Maybe tomorrow.)
 * Copied from RXPAccurateDrawLayer on November 28th 2019.
 */
public class RVXAAccurateDrawLayer extends RMZAccurateDrawLayer {
    public final VXATileRenderer tiles;
    public final RMEventGraphicRenderer events;

    public final ZSortingDrawLayer.SignalMapViewLayer signalLayerEvA = new ZSortingDrawLayer.SignalMapViewLayer(TXDB.get("Event Layers"));

    private static final int[] layerPreference = new int[] {0, 1, 3, 2};

    public RVXAAccurateDrawLayer(RubyTable tbl, IEventAccess eventList, VXATileRenderer tils, RMEventGraphicRenderer ev) {
        super(tbl, 4);
        tiles = tils;
        events = ev;
        signals.add(signalLayerEvA);
        // -1 is the "ground plane".
        for (int i = -1; i < tbl.height + 5; i++)
            zSorting.add(new RVXAPriorityPlane(i));
        // Very specific choice of algorithm.
        for (IRIO r : eventList.getEventKeys()) {
            IRIO ed = eventList.getEvent(r);
            // Determine Z location.
            int eventLayer = events.determineEventLayer(ed);
            // Since the "screen height" rules aren't *quite* being properly held to,
            //  use a suitable replacement for 999.
            long z = Long.MAX_VALUE;
            if (eventLayer < 2)
                z = ((ed.getIVar("@y").getFX() + 1) * 2) + 1;
            if (eventLayer == 1)
                z++;
            zSorting.add(new EventZSortedObject(ed, z, signalLayerEvA, ev));
        }
        completeSetup();
    }

    @Override
    public String getName() {
        return TXDB.get("VXA Tile/Event Z-Emulation Layer");
    }

    private class RVXAPriorityPlane extends TileMapViewDrawLayer implements ZSortingDrawLayer.IZSortedObject {
        // priority + tile Y
        public final int pIndex;

        public RVXAPriorityPlane(int p) {
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
            if (pIndex > (y + 4))
                return false;
            return true;
        }

        @Override
        public boolean shouldDraw(int x, int y, int layer, short value) {
            if (!tileSignalLayers[layer].active)
                return false;
            int targPIndex = y;
            if (layer != 3) {
                // Normal layers
                int pri = 0;
                if (!tiles.flags.outOfBounds(value & 0xFFFF, 0)) {
                    int flags = tiles.flags.getTiletype(value & 0xFFFF, 0, 0);
                    if ((flags & 16) != 0)
                        pri += 4;
                } else {
                    System.err.println("WARNING: Corrupt/malsized VXA priority table (what are you playing at?)");
                }
                targPIndex += pri;
            } else {
                // no priority
            }
            return targPIndex == pIndex;
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.App;
import r48.RubyTable;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.map.events.IEventAccess;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.tiles.VXATileRenderer;

/**
 * Despite the name, right now this is stitched together to solve some bugs.
 * So it doesn't really represent full accuracy. (Maybe tomorrow.)
 * Copied from RXPAccurateDrawLayer on November 28th 2019.
 */
public class RVXAAccurateDrawLayer extends RMZAccurateDrawLayer {
    public final VXATileRenderer tiles;
    public final RMEventGraphicRenderer events;

    public final ZSortingDrawLayer.SignalMapViewLayer signalLayerEvA = new ZSortingDrawLayer.SignalMapViewLayer(T.m.l_ev);

    private static final int[] layerPreference = new int[] {0, 1, 3, 2};

    public RVXAAccurateDrawLayer(RubyTable tbl, IEventAccess eventList, VXATileRenderer tils, RMEventGraphicRenderer ev) {
        super(tils.app, tbl, 4);
        tiles = tils;
        events = ev;
        signals.add(signalLayerEvA);
        // -1 is the "ground plane".
        for (int i = -1; i < tbl.height + 5; i++)
            zSorting.add(new RVXAPriorityPlane(tils.app, i));
        // Very specific choice of algorithm.
        for (DMKey r : eventList.getEventKeys()) {
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
        return T.m.l_vxaZ;
    }

    private class RVXAPriorityPlane extends TileMapViewDrawLayer implements ZSortingDrawLayer.IZSortedObject {
        // priority + tile Y
        public final int pIndex;

        public RVXAPriorityPlane(App app, int p) {
            super(app, mapTable, layerPreference, tiles, "INTERNAL - YOU SHOULD NOT SEE THIS");
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

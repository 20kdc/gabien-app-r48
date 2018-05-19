/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.IGrDriver;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
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
public class RXPAccurateDrawLayer implements IMapViewDrawLayer {
    public final RubyTable mapTable;
    public final IEventAccess eventList;
    public final XPTileRenderer tiles;
    public final RMEventGraphicRenderer events;

    public boolean enableEventsA = false;
    public boolean enableEventsB = false;
    public boolean enableTilesA = false;
    public final IMapViewDrawLayer signalLayerEvA = new SignalMapViewLayer(TXDB.get("Event Layers (lower)"), new Runnable() {
        @Override
        public void run() {
            enableEventsA = true;
        }
    });
    public final IMapViewDrawLayer signalLayerEvB = new SignalMapViewLayer(TXDB.get("Event Layers (upper)"), new Runnable() {
        @Override
        public void run() {
            enableEventsB = true;
        }
    });

    public final IMapViewDrawLayer signalLayerTiA = new SignalMapViewLayer(TXDB.get("Tile Layers"), new Runnable() {
        @Override
        public void run() {
            enableTilesA = true;
        }
    });
    // Used to be a TreeSet, but objects keep disappearing and I want to eliminate causes.
    public final LinkedList<IZSortedObject> zSorting = new LinkedList<IZSortedObject>();

    public RXPAccurateDrawLayer(RubyTable tbl, IEventAccess evl, XPTileRenderer tils, RMEventGraphicRenderer ev) {
        mapTable = tbl;
        eventList = evl;
        tiles = tils;
        events = ev;
        // -1 is the "ground plane".
        for (int i = -1; i < tbl.height + 5; i++)
            zSorting.add(new RXPPriorityPlane(i));
        // Very specific choice of algorithm.
        for (RubyIO r : eventList.getEventKeys()) {
            RubyIO ed = eventList.getEvent(r);
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
                z = ((ed.getInstVarBySymbol("@y").fixnumVal + 1) * 2) + 1;
                RubyIO edG = events.extractEventGraphic(ed);
                if (edG != null) {
                    long tid = edG.getInstVarBySymbol("@tile_id").fixnumVal;
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
            zSorting.add(new RXPEventPlane(ed, z, isUpper));
        }
        Collections.sort(zSorting, new Comparator<IZSortedObject>() {
            @Override
            public int compare(IZSortedObject t0, IZSortedObject t1) {
                long zA = t0.getZ();
                long zB = t1.getZ();
                if (zA < zB)
                    return -1;
                if (zA > zB)
                    return 1;
                return 0;
            }
        });
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

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        for (IZSortedObject zso : zSorting) {
            if (zso instanceof RXPPriorityPlane)
                if (!enableTilesA)
                    continue;
            if (zso instanceof RXPEventPlane) {
                if (!((RXPEventPlane) zso).eventUpper) {
                    if (!enableEventsA)
                        continue;
                } else {
                    if (!enableEventsB)
                        continue;
                }
            }
            zso.draw(camX, camY, camTX, camTY, camTR, camTB, mouseXT, mouseYT, eTileSize, currentLayer, callbacks, debug, igd);
        }
        enableTilesA = false;
        enableEventsA = false;
        enableEventsB = false;
    }

    private class RXPEventPlane implements IZSortedObject {
        public final long z;
        public final RubyIO evI;
        public final boolean eventUpper;

        public RXPEventPlane(RubyIO event, long z, boolean isUpper) {
            this.z = z;
            evI = event;
            eventUpper = isUpper;
        }

        @Override
        public long getZ() {
            return z;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
            int x = (int) evI.getInstVarBySymbol("@x").fixnumVal;
            int y = (int) evI.getInstVarBySymbol("@y").fixnumVal;
            // Events vary in size - to stop the most obvious glitching, add some margin.
            camTX -= 2;
            camTY -= 2;
            camTR += 2;
            camTB += 2;
            if (x < camTX)
                return;
            if (y < camTY)
                return;
            if (x >= camTR)
                return;
            if (y >= camTB)
                return;
            int px = (x * eTileSize) - camX;
            int py = (y * eTileSize) - camY;
            RubyIO g = events.extractEventGraphic(evI);
            if (g != null)
                events.drawEventGraphic(g, px, py, igd, 1);
        }
    }

    private class RXPPriorityPlane extends TileMapViewDrawLayer implements IZSortedObject {
        // priority + tile Y
        public final int pIndex;

        public RXPPriorityPlane(int p) {
            super(mapTable, -1, tiles, "INTERNAL - YOU SHOULD NOT SEE THIS");
            pIndex = p;
        }

        @Override
        public long getZ() {
            return (pIndex + 1) * 2;
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
            int pri = getTIDPriority(value);
            if (pIndex == -1)
                if (pri == 0)
                    return true;
            int targPIndex = y + pri;
            return targPIndex == pIndex;
        }
    }

    private interface IZSortedObject extends IMapViewDrawLayer {
        long getZ();
    }

    private class SignalMapViewLayer implements IMapViewDrawLayer {
        public final String signalName;
        public final Runnable call;

        public SignalMapViewLayer(String sn, Runnable flagEnabler) {
            signalName = sn;
            call = flagEnabler;
        }

        @Override
        public String getName() {
            return signalName;
        }

        @Override
        public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
            call.run();
        }
    }
}

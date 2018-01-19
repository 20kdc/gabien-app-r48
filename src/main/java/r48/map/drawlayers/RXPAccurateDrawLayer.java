/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.IGrDriver;
import gabien.IImage;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;
import r48.map.events.IEventAccess;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.tiles.XPTileRenderer;

import java.util.Comparator;
import java.util.TreeSet;

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
    public boolean enableTilesB = false;
    public boolean enableTilesC = false;

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
    public final IMapViewDrawLayer signalLayerTiA = new SignalMapViewLayer(TXDB.get("Tiles L0"), new Runnable() {
        @Override
        public void run() {
            enableTilesA = true;
        }
    });

    public final IMapViewDrawLayer signalLayerTiB = new SignalMapViewLayer(TXDB.get("Tiles L1"), new Runnable() {
        @Override
        public void run() {
            enableTilesB = true;
        }
    });
    public final IMapViewDrawLayer signalLayerTiC = new SignalMapViewLayer(TXDB.get("Tiles L2"), new Runnable() {
        @Override
        public void run() {
            enableTilesC = true;
        }
    });

    // Used to quickly build the efficient render list to offset the cost of all this.
    public final TreeSet<IZSortedObject> zSorting = new TreeSet<IZSortedObject>(new Comparator<IZSortedObject>() {
        @Override
        public int compare(IZSortedObject t0, IZSortedObject t1) {
            long zA = t0.getZ();
            long zB = t1.getZ();
            if (zA < zB)
                return 1;
            if (zA > zB)
                return -1;
            return 0;
        }
    });

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
                z = (ed.getInstVarBySymbol("@y").fixnumVal * 32) + 32;
                RubyIO edG = events.extractEventGraphic(ed);
                if (edG != null) {
                    long tid = edG.getInstVarBySymbol("@tile_id").fixnumVal;
                    if (tid != 0) {
                        // Get priority...
                        z += getTIDPriority((short) tid) * 32;
                    } else {
                        // Firstly, correct for missing Y accounting
                        IImage i = events.imageLoader.getImage("Characters/" + edG.getInstVarBySymbol("@character_name").decString(), false);
                        int sprH = i.getHeight() / 4;
                        z -= sprH;
                        if (sprH > 32)
                            z += 31;
                    }
                }
            }
            zSorting.add(new RXPEventPlane(ed, z, isUpper));
        }
    }

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
            if (zso instanceof RXPPriorityPlane) {
                if (((RXPPriorityPlane) zso).tileLayer == 0)
                    if (!enableTilesA)
                        continue;
                if (((RXPPriorityPlane) zso).tileLayer == 1)
                    if (!enableTilesB)
                        continue;
                if (((RXPPriorityPlane) zso).tileLayer == 2)
                    if (!enableTilesC)
                        continue;
            }
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
        enableTilesB = false;
        enableTilesC = false;
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
            super(mapTable, -1, tiles);
            pIndex = p;
        }

        @Override
        public long getZ() {
            if (pIndex == -1)
                return 0;
            return (pIndex + 1) * 32;
        }

        @Override
        public boolean shouldDraw(int x, int y, int layer, short value) {
            int targPIndex = y + getTIDPriority(value);
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

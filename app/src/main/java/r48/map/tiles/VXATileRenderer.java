/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.tiles;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.Rect;
import r48.App;
import r48.RubyTable;
import r48.dbs.ATDB;
import r48.io.data.IRIO;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * This uses a totally different system from XP, based around 5 AT sheets and 4 primary sheets.
 * Created on 1/27/17.
 */
public class VXATileRenderer extends App.Svc implements ITileRenderer {

    public static final int tileSize = 32;
    public final IImage[] tilesetMaps = new IImage[9];
    private final IRIO tileset;
    // Generated one-pixel image to be blended for shadow
    public IImage shadowImage;
    public RubyTable flags;
    /**
     * See prepareATTF function for details
     */
    private final ExpandedATTF[] preparedATTF;

    public VXATileRenderer(App app, IImageLoader il, IRIO tileset) {
        super(app);
        this.tileset = tileset;
        int[] tinyTile = new int[] {0x80000000};
        shadowImage = GaBIEn.createImage(tinyTile, 1, 1);
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
        if (tileset != null) {
            flags = new RubyTable(tileset.getIVar("@flags").getBuffer());
            IRIO amNames = tileset.getIVar("@tileset_names");
            for (int i = 0; i < tilesetMaps.length; i++) {
                IRIO rio = amNames.getAElem(i);
                String expectedAT = rio.decString();
                if (expectedAT.length() != 0)
                    tilesetMaps[i] = il.getImage("Tilesets/" + expectedAT, false);
            }
        } else {
            flags = new RubyTable(2, 0, 0, 0, new int[0]);
        }
        preparedATTF = prepareATTF();
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, boolean editor) {
        if (tileset == null)
            return; // just don't bother.
        // [EPC] (use this to find other bits of documentation)
        // - See IRB_VXA_TILES for a full ID listing!
        // - Plane 0x08 is special - every other row is skipped.
        //   Why? Because animation!
        // - Some tiles (Crysalis :: Limenas Inn, counter) can warp reality.
        //   Yes. You read that right. THEY CAN WARP REALITY.
        //   Unsure if this even should be supported.
        // - What is the difference between Mode 0 and Mode 1?
        // - Shadow layer values.
        //   Apart from the known upper bits:
        //   They appear to be a set of 8 flags, of which 4 I currently understand.
        //   Flag 1 is TL, Flag 2 is TR, Flag 4 is LL, Flag 8 is LR.
        //   I'm unsure if there even is a use for the other 4.
        //   You could probably use the shadow layer to show a disco pattern,
        //    with access to tileIDs from RGSS...

        // Shadow layer.
        if (layer == 3) {
            int st = (tileSize / 2);
            drawShadowTileFlag(tidx, 1, px, py, igd, st);
            drawShadowTileFlag(tidx, 2, px + st, py, igd, st);
            drawShadowTileFlag(tidx, 4, px, py + st, igd, st);
            drawShadowTileFlag(tidx, 8, px + st, py + st, igd, st);
            return;
        }

        // Check all-tiles range.
        // Note 0x0000 is omitted.
        if ((tidx < 0x0001) || (tidx > 0x1FFF))
            return;

        int plane = tidx & 0xFF00;
        plane >>= 8;

        // First 8 planes are just handleMTLayer stuff on a specific sheet.
        // This covers 0x0001:0x07FF
        int[] tsi = new int[] {5, 6, 7, 8, -1, -1, 4, 4};
        if (plane >= 0x00 && plane < 0x08) {
            int ok = tsi[plane];
            if (ok != -1)
                if (handleMTLayer(tidx, tileSize, px, py, ok, igd))
                    return;
        } else if (plane >= 0x08 && plane < 0x20) {
            int frame = getFrame();
            ExpandedATTF attf = preparedATTF[(((int) tidx) - 0x0800) / 48];
            int fX = attf.fieldTileSpace.x, fY = attf.fieldTileSpace.y;
            frame %= attf.anim.length / 2;
            frame *= 2;
            fX += attf.anim[frame];
            fY += attf.anim[frame + 1];
            if (handleATLayer(tidx, attf.start, tileSize, px, py, attf.tilesetIdx, igd, attf.databaseId, fX, fY))
                return;
        }

        GaBIEn.engineFonts.f8.drawLAB(igd, px, py, Integer.toHexString(tidx), false);
    }

    /**
     * Prepares the expanded ATTFs.
     * These are used for drawing.
     * Most importantly, they are a continuous array covering all of 0x0800 and stopping before 0x2000.
     * This keeps lookups fast. 
     */
    private ExpandedATTF[] prepareATTF() {
        ExpandedATTF[] attf = new ExpandedATTF[128];
        int[] animNone = new int[] {0, 0};
        int[] animWater = new int[] {0, 0, 2, 0, 4, 0, 2, 0};
        int[] animWaterfall = new int[] {0, 0, 0, 1, 0, 2};
        for (int i = 0; i < attf.length; i++) {
            int base = 0x0800 + (i * 48);
            if (i == 1) {
                // A1 - override groundmatter 1
                attf[i] = ExpandedATTF.vxGeneral(base, 0, 0, 3, animWater);
            } else if ((i == 2) || (i == 3)) {
                // A1 - override groundmatter 2/3
                attf[i] = ExpandedATTF.vxGeneral(base, 0, 6, (i & 1) * 3, animNone);
            } else if (i >= 0 && i < 16) {
                // A1 - general
                // base for major quadrant (upper 2 bits)
                int baseX = (i & 4) << 1;
                int baseY = (i / 8) * 6;
                // apply row offset
                baseY += (i & 2) != 0 ? 3 : 0;
                // water/waterfall control
                if ((i & 1) == 0) {
                    // water
                    attf[i] = ExpandedATTF.vxGeneral(base, 0, baseX, baseY, animWater);
                } else {
                    // waterfall
                    attf[i] = ExpandedATTF.vxWaterfall(base, 0, baseX + 6, baseY, animWaterfall);
                }
            } else if (i >= 16 && i < 48) {
                // A2
                int rel = i - 16;
                int relX = rel % 8;
                int relY = rel / 8;
                attf[i] = ExpandedATTF.vxGeneral(base, 1, relX * 2, relY * 3, animNone);
            } else if (i >= 48 && i < 80) {
                // A3
                int rel = i - 48;
                int relX = rel % 8;
                int relY = rel / 8;
                attf[i] = ExpandedATTF.vxWall(base, 2, relX * 2, relY * 2, animNone);
            } else if (i >= 80 && i < 128) {
                // A4
                int rel = i - 80;
                int relX = rel % 8;
                int relY = rel / 8;
                // "major row" number
                int relYM = relY >> 1;
                // "major row" base tile Y
                int relYMT = relYM * 5;
                if ((relY & 1) == 0) {
                    // ceiling
                    attf[i] = ExpandedATTF.vxGeneral(base, 3, relX * 2, relYMT, animNone);
                } else {
                    // wall
                    attf[i] = ExpandedATTF.vxWall(base, 3, relX * 2, relYMT + 3, animNone);
                }
            }
        }
        return attf;
    }

    private void drawShadowTileFlag(short tidx, int i, int i1, int i2, IGrDriver igd, int st) {
        if ((tidx & i) != 0)
            igd.blitScaledImage(0, 0, 1, 1, i1, i2, st, st, shadowImage);
    }

    /**
     * Handles 2-column 256-tile sheets
     */
    private boolean handleMTLayer(short tidx, int ets, int px, int py, int tm, IGrDriver igd) {
        int t = tidx & 0xFF;
        IImage planeImage = tilesetMaps[tm];
        if (planeImage != null) {
            // Only makes sense with a baseline of 8 but that leads to 'corrupted'-looking data in parts hm.
            // 9b - 80 = 1B.
            // ...This explains it!
            int tgtX = t % 8;
            int tgtY = t / 8;
            if (tgtY >= 16) {
                tgtY -= 16;
                tgtX += 8;
            }
            igd.blitScaledImage(tgtX * tileSize, tgtY * tileSize, ets, ets, px, py, ets, ets, planeImage);
            return true;
        }
        return false;
    }

    private boolean handleATLayer(short tidx, int base, int ets, int px, int py, int tm, IGrDriver igd, int atF, int atOX, int atOY) {
        int tin = tidx - base;
        if (tin < 0)
            return false;
        int pox = tileSize * atOX;
        int poy = tileSize * atOY;
        IImage planeImg = tilesetMaps[tm];
        if (planeImg != null) {
            if ((ets == tileSize) && (app.autoTiles[atF] != null)) {
                ATDB.Autotile at = app.autoTiles[atF].entries[tin];
                if (at != null) {
                    int cSize = ets / 2;
                    int cSizeI = tileSize / 2;
                    for (int sA = 0; sA < 2; sA++)
                        for (int sB = 0; sB < 2; sB++) {
                            int ti = at.corners[sA + (sB * 2)];
                            int tx = (ti % 3) * tileSize;
                            int ty = (ti / 3) * tileSize;
                            if (ti == 2) {
                                tx = tileSize;
                            } else if (ti > 2) {
                                ty -= tileSize;
                                tx /= 2;
                                ty /= 2;
                                ty += tileSize;
                            }
                            int sX = (sA * cSizeI);
                            int sY = (sB * cSizeI);
                            int s2X = sA * cSize;
                            int s2Y = sB * cSize;
                            igd.blitScaledImage(tx + pox + sX, ty + poy + sY, cSizeI, cSizeI, px + s2X, py + s2Y, cSize, cSize, planeImg);
                        }
                    return true;
                }
            } else {
                igd.blitScaledImage(tileSize, 2 * tileSize, ets, ets, px, py, ets, ets, planeImg);
                return true;
            }
        }
        return false;
    }

    public TileEditingTab[] getEditConfig(int layer) {
        if (layer == 3) {
            return new TileEditingTab[0];
        } else {
            int[] allATs = new int[0x1800 / 48];
            for (int i = 0; i < allATs.length; i++)
                allATs[i] = 0x800 + (i * 48);
            return new TileEditingTab[] {
                    new TileEditingTab(app, "AT", false, allATs, indicateATs()),

                    new TileEditingTab("G1", false, TileEditingTab.range(0x000, 0x400)),
                    new TileEditingTab("G2", false, TileEditingTab.range(0x600, 0x100)),

                    new TileEditingTab("AT1-M", false, TileEditingTab.range(0x800, 0x300)),
                    new TileEditingTab("AT2-M", false, TileEditingTab.range(0xB00, 0x600)),
                    new TileEditingTab("AT3-M", false, TileEditingTab.range(0x1100, 0x600)),
                    new TileEditingTab("AT4-M", false, TileEditingTab.range(0x1700, 0x900))
            };
        }
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        // If you can't read the boring documentation I highlighted above,
        //  or just want AT information on the other banks (since they're basic but confusing),
        //  either go here or createATUIPlanes. Or both.
        AutoTileTypeField[] r = new AutoTileTypeField[preparedATTF.length];
        for (int i = 0; i < r.length; i++)
            r[i] = preparedATTF[i];
        return r;
    }

    @Override
    public int getFrame() {
        double gt = GaBIEn.getTime() * 2;
        if (gt < 0)
            gt = 0;
        // one three-frame cycle and one four-frame cycle
        // so multiply them and so they'll sync up
        gt %= 12;
        return (int) gt;
    }

    @Override
    public int getRecommendedWidth() {
        return 8;
    }

    public static class ExpandedATTF extends AutoTileTypeField {
        /**
         * Index of tileset sheet
         */
        public final int tilesetIdx;
        /**
         * Field coordinates, kind of
         */
        public final Rect fieldTileSpace;
        /**
         * Animation offsets in fields
         */
        public final int[] anim;

        public ExpandedATTF(int base, int databaseId, int represent, int tsi, Rect f, int[] a) {
            super(base, 48, databaseId, represent);
            tilesetIdx = tsi;
            fieldTileSpace = f;
            anim = a;
        }

        public static ExpandedATTF vxGeneral(int base, int tsi, int x, int y, int[] a) {
            return new ExpandedATTF(base, 0, 47, tsi, new Rect(x, y, 2, 3), a);
        }

        public static ExpandedATTF vxWall(int base, int tsi, int x, int y, int[] a) {
            return new ExpandedATTF(base, 1, 15, tsi, new Rect(x, y, 2, 2), a);
        }

        public static ExpandedATTF vxWaterfall(int base, int tsi, int x, int y, int[] a) {
            // fake it for now...
            return new ExpandedATTF(base, 2, 0, tsi, new Rect(x, y, 2, 1), a);
        }
    }
}

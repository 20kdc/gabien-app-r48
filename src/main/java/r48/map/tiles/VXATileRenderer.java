/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.tiles;

import gabien.FontManager;
import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import r48.AppMain;
import r48.RubyTable;
import r48.dbs.ATDB;
import r48.io.data.IRIO;
import r48.map.UIMapView;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

import java.util.LinkedList;

/**
 * This uses a totally different system from XP, based around 5 AT sheets and 4 primary sheets.
 * Created on 1/27/17.
 */
public class VXATileRenderer implements ITileRenderer {

    public static final int tileSize = 32;
    public final IImage[] tilesetMaps = new IImage[9];
    private final IRIO tileset;
    // Generated one-pixel image to be blended for shadow
    public IImage shadowImage;
    public RubyTable flags;

    public VXATileRenderer(IImageLoader il, IRIO tileset) {
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
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale, boolean editor) {
        if (tileset == null)
            return; // just don't bother.
        // MKXP repository links to http://www.tktkgame.com/tkool/memo/vx/tile_id.html
        // It's in Japanese but via translation at least explains that:
        // 1. Autotiles are different. Very different.
        //    (There *seem* to still be 48-tile planes, however.
        //     There are just additional less comprehensible planes.)
        // Gist of it is that autotiles are now in the higher sections (0x800+),
        //  0x0YY/0x1YY/0x2YY/0x3YY are the "upper layer" detail planes (not sure what's going on here)
        //  0x600-0x67F seems to be the closest thing I could find to "normal".

        // -------------IGNORE THE ABOVE------------------
        // [EPC] (use this to find other bits of documentation)
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
            int st = ((tileSize * spriteScale) / 2);
            drawShadowTileFlag(tidx, 1, px, py, igd, st);
            drawShadowTileFlag(tidx, 2, px + st, py, igd, st);
            drawShadowTileFlag(tidx, 4, px, py + st, igd, st);
            drawShadowTileFlag(tidx, 8, px + st, py + st, igd, st);
            return;
        }

        if (tidx == 0)
            return; // magical exception

        int plane = tidx & 0xFF00;
        plane /= 0x100;

        if (plane >= 0)
            if (plane < 4)
                if (handleMTLayer(tidx, tileSize, px, py, plane + 5, igd, spriteScale))
                    return;

        int mode = (int) tileset.getIVar("@mode").getFX();

        // AT Planes (Part 1 and 2). These use AT Type 0 "standard".

        // Notice only 3 planes are allocated - that's 2 rows of 3 ATs.
        if (plane >= 8)
            if (plane <= 0x0A)
                if (handleSATLayer(tidx, 0x0800, tileSize, px, py, 0, igd, mode, spriteScale))
                    return;

        if (plane >= 0x0B)
            if (plane <= 0x10)
                if (handleSATLayer(tidx, 0x0B00, tileSize, px, py, 1, igd, mode, spriteScale))
                    return;

        // Secondary 'Wall' AT Planes (Part 3 and 4). These use AT Type 1 "wall".

        if (plane >= 0x11)
            if (plane <= 0x16)
                if (handleSATLayer(tidx, 0x1100, tileSize, px, py, 2, igd, mode, spriteScale))
                    return;
        if (plane >= 0x17)
            if (plane <= 0x1F)
                if (handleSATLayer(tidx, 0x1700, tileSize, px, py, 3, igd, mode, spriteScale))
                    return;

        // Plane 6 (Part 5)

        if (plane == 6)
            if (handleMTLayer(tidx, tileSize, px, py, 4, igd, spriteScale))
                return;

        FontManager.drawString(igd, px, py, Integer.toHexString(tidx), false, false, UIMapView.mapDebugTextHeight);
    }

    private void drawShadowTileFlag(short tidx, int i, int i1, int i2, IGrDriver igd, int st) {
        if ((tidx & i) != 0)
            igd.blitScaledImage(0, 0, 1, 1, i1, i2, st, st, shadowImage);
    }

    private boolean handleMTLayer(short tidx, int ets, int px, int py, int tm, IGrDriver igd, int spriteScale) {
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
            RMEventGraphicRenderer.flexibleSpriteDraw(tgtX * tileSize, tgtY * tileSize, ets, ets, px, py, ets * spriteScale, ets * spriteScale, 0, planeImage, 0, igd);
            return true;
        }
        return false;
    }

    private boolean handleSATLayer(short tidx, int base, int ets, int px, int py, int tm, IGrDriver igd, int mode, int spriteScale) {
        int atField = 0;
        int atCW = 2;
        int atCH = 3;
        int atOX = 0;
        int atOY = 0;
        /**
         * THE EXTRA SUPER IMPORTANT NOTES ON Tilemap 3.
         * [EPC] (use this to find other bits of documentation)
         *
         * After much orderly and totally not insanity-causing investigation, I've come to the conclusion this is organized in "sheets" of 0x180 tiles.
         * The sheets are interleaved.
         * The naming convention I put in place for them is random, but:
         * A-type sheets are wall sheets, and have lots of invalid tiles.
         * B-type sheets are the ATs on top sheets.
         * The interleaving goes as thus: 0x1700, the first sheet, is a B-type sheet.
         * After that, at 0x1880, the second sheet starts: an A-type sheet.
         * Yes, I know, A/B is the wrong way around.
         *
         * NT II.
         *
         * Within the B-type sheets, it's as you'd expect: there isn't any spare room,
         * it's just all 8 48tile AT fields one after the other.
         * Within the A-type sheets, however...
         * It's 16 valid tiles, then 32 invalid tiles, to make up a 48tile AT field.
         * Yup, it's a 16-tile AT field expanded to 48. Because why not.
         *
         */

        if (tm == 3) {
            // WALL NOTES.
            // IDX of Col 4, Row 2. 1c12, 1c18.
            // IDX of Col 5, Row 2. 1c47, 1c4d.
            if (base == 0x1700) {
                int saBank = 0; // 1-indexed. Represents walls.
                int sbBank = 0; // also 1-indexed Represents ATs.
                if ((tidx >= 0x1700) && (tidx < 0x1880)) {
                    base = 0x1700;
                    sbBank = 1;
                }
                if ((tidx >= 0x1880) && (tidx < 0x1A00)) {
                    base = 0x1880;
                    saBank = 1;
                }
                if ((tidx >= 0x1A00) && (tidx < 0x1B80)) {
                    base = 0x1A00;
                    sbBank = 2;
                }
                if ((tidx >= 0x1B80) && (tidx < 0x1D00)) {
                    base = 0x1B80;
                    saBank = 2;
                }
                if ((tidx >= 0x1D00) && (tidx < 0x1E80)) {
                    base = 0x1D00;
                    sbBank = 3;
                }
                if ((tidx >= 0x1E80) && (tidx < 0x2000)) {
                    base = 0x1E80;
                    saBank = 3;
                }
                if (saBank != 0) {
                    atField = 1;
                    atCW = 2;
                    atCH = 5;
                    atOX = 0;
                    atOY = 3 + (5 * (saBank - 1));
                }
                if (sbBank != 0) {
                    atField = 0;
                    atCW = 2;
                    atCH = 5;
                    atOX = 0;
                    atOY = 5 * (sbBank - 1);
                }
            }
        } else if (tm == 2) {
            // [EPC] (use this to find other bits of documentation)
            // This is super simple and seems to do the job
            atField = 1;
            atCW = 2;
            atCH = 2;
            atOX = 0;
            atOY = 0;
        } else if (tm == 0) {
            // Every other row is skipped for animation purposes.
            atCH = 6;
        }
        return handleATLayer(tidx, base, ets, px, py, tm, igd, atField, atCW, atCH, atOX, atOY, 48, spriteScale);
    }

    private boolean handleATLayer(short tidx, int base, int ets, int px, int py, int tm, IGrDriver igd, int atF, int atCW, int atCH, int atOX, int atOY, int div, int spriteScale) {
        int tin = tidx - base;
        if (tin < 0)
            return false;
        int atid = tin % div;
        int apid = tin / div;
        int pox = apid % 8;
        int poy = apid / 8;
        pox *= tileSize * atCW;
        poy *= tileSize * atCH;
        pox += tileSize * atOX;
        poy += tileSize * atOY;
        IImage planeImg = tilesetMaps[tm];
        if (planeImg != null) {
            if ((ets == tileSize) && (AppMain.autoTiles[atF] != null)) {
                ATDB.Autotile at = AppMain.autoTiles[atF].entries[atid];
                if (at != null) {
                    int cSize = (ets / 2) * spriteScale;
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
                            RMEventGraphicRenderer.flexibleSpriteDraw(tx + pox + sX, ty + poy + sY, cSizeI, cSizeI, px + s2X, py + s2Y, cSize, cSize, 0, planeImg, 0, igd);
                        }
                    return true;
                }
            } else {
                RMEventGraphicRenderer.flexibleSpriteDraw(tileSize, 2 * tileSize, ets, ets, px, py, ets * spriteScale, ets * spriteScale, 0, planeImg, 0, igd);
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
                    new TileEditingTab("AT", false, allATs, indicateATs()),

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
        LinkedList<Integer> atFields = new LinkedList<Integer>();
        LinkedList<Integer> atWFields = new LinkedList<Integer>();
        for (int i = 0; i < 32; i++) {
            int resultingAddr = i * 48;

            if (i < 8) {
                // AT fields for T.M. 3!
                atFields.add(resultingAddr + 0x1700);
                atFields.add(resultingAddr + 0x1A00);
                atFields.add(resultingAddr + 0x1D00);
                atWFields.add(resultingAddr + 0x1880);
                atWFields.add(resultingAddr + 0x1B80);
                atWFields.add(resultingAddr + 0x1E80);
            }
            // T.M. 2 AT fields!
            atWFields.add(resultingAddr + 0x1100);
            // The rest
            int resultingEndAddr = resultingAddr + 47;
            // May need update in future.
            if (resultingEndAddr < 0x300)
                atFields.add(0x800 + resultingAddr);
            if (resultingEndAddr < 0x600)
                atFields.add(0xB00 + resultingAddr);
        }
        AutoTileTypeField[] r = new AutoTileTypeField[atFields.size() + atWFields.size()];
        for (int i = 0; i < atFields.size(); i++)
            r[i] = new AutoTileTypeField(atFields.get(i), 48, 0, 47);
        for (int i = 0; i < atWFields.size(); i++)
            r[i + atFields.size()] = new AutoTileTypeField(atWFields.get(i), 48, 1, 15);
        return r;
    }

    @Override
    public int getFrame() {
        return 0;
    }

    @Override
    public int getRecommendedWidth() {
        return 8;
    }
}

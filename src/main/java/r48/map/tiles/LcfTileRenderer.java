/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.ATDB;
import r48.map.UIMapView;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.IImageLoader;
import r48.ui.UITileGrid;

import java.util.LinkedList;

/**
 * I slept, finished MapUnit, and began writing this class.
 * Created on 31/05/17.
 */
public class LcfTileRenderer implements ITileRenderer {
    public final IImage chipset;

    public LcfTileRenderer(IImageLoader imageLoader, RubyIO tso) {
        if (tso != null) {
            chipset = imageLoader.getImage("ChipSet/" + tso.getInstVarBySymbol("@tileset_name").decString(), false);
        } else {
            chipset = null;
        }
    }

    @Override
    public int getTileSize() {
        return 16;
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int ets, int spriteScale) {
        if (chipset == null)
            return;
        // There are 288 "Common Tiles" (non-AT) divided into upper and lower layer tiles.
        // On the CS, they start at X 192.
        // Two pages of 144 each.
        // Everything here makes more sense in decimal.
        if ((tidx >= 5000) && (tidx < 6000))
            handleCommonPage(5000, 0, tidx, px, py, igd, chipset, ets, spriteScale);
        if ((tidx >= 10000) && (tidx < 11000))
            handleCommonPage(10000, 1, tidx, px, py, igd, chipset, ets, spriteScale);
        // This is a possible *50-wide AT Field!!!!!* Well, 12 of them.
        // Terrain ATs are laid out as follows on the image:
        // ??45
        // ??67
        // 0189
        // 23AB
        // '?' is animated and water
        if ((tidx >= 4000) && (tidx < 4600)) {
            // 4150 : 3, OS-Legacy
            // 50 * 12 = 600
            int field = ((tidx - 4000) / 50) + 4;
            int subfield = (tidx - 4000) % 50;

            int fx = ((field % 2) * 3) + ((field / 8) * 6);
            int fy = ((field / 2) % 4) * 4;
            handleATField(subfield, fx, fy, px, py, igd, chipset, ets, spriteScale);
            // igd.drawText(px, py, 255, 255, 255, 8, Integer.toString(field));
        }

        // Animated tiles are... frankly, weird.
        // They are *also* split into 50-entry fields, but all of the entries are the same.
        // 000-049 is field 1.
        // 050-099 is field 2.
        // 100-149 is field 3.
        // (Further fields presumably run off the right side or something)
        if ((tidx >= 3000) && (tidx < 4000)) {
            int field = (tidx - 3000) / 50; // this is all that matters
            // Unsure what the timing is on frames. Assuming 1/4 sec, will check later
            // It's 1/8th second.
            double s = GaBIEn.getTime() * 2;
            s -= Math.floor(s);
            s *= 4;
            s = Math.floor(s);
            int f = (int) s;
            f %= 4;
            RMEventGraphicRenderer.flexibleSpriteDraw(48 + (field * 16), 64 + (f * 16), ets, ets, px, py, ets * spriteScale, ets * spriteScale, 0, chipset, 0, igd);
        }

        // Water tiles are yet another 50-entry AT field, seemingly of a different type.
        // Or possibly with some serious corner remapping.
        // Notably, this leaves just enough room for 0000-0999, 1000-1999, and 2000-2999, with 3000-3999 being the domain of animated tiles.
        if ((tidx >= 0) && (tidx < 3000)) {
            // Actually, this isn't even an AT Field anymore. This is something... else.
            // The first 0x2F (47) tiles follow a pattern:
            // The first 16 tiles are a binary counter.
            //  then 4 tiles follow for a binary counter on the right side, left side locked to edge.
            //  then 4 tiles follow for a binary counter on the bottom side, top side locked to edge.
            //  then 4 tiles follow for a binary counter on the left side, right side locked to edge.
            //  then 4 tiles follow for a binary counter on the top side, bottom side locked to edge.
            //  then vertical tunnel, then horizontal tunnel,
            // then 4 corners follow, UL, UR, LR, LL, alternating between with inner dot and without inner dot,
            // then 4 directional stubs, here labelled by the side of the tile it leaves: DRUL,
            // and finally the blank dot.

            // This repeats at:
            // 0x32 (yes, really) where the top-left corner is overridden with a diamond (dark outside, no hl) corner piece,
            // 0x64 (yes, still really) where the top-right corner is overridden with a diamond (dark outside, no hl) corner piece,
            // 0x96 (...) where both top corners are overridden with diamond (dark outside, no hl) corner pieces,
            // and so on.

            // At 0x3E8, the pattern restarts, but the "base" is now the white-corner/edged.
            // At 0x7D0, the pattern restarts once more, but the "base" is now the green-corner/edged dark back (as opposed to the other 2 which are lightbacked)

            // Current implementation planning notes:
            // I'm using the AB Field Test as a visual reference to construct these tables.
            // It's a lot easier to understand and see.

            int tField = tidx / 1000;
            int tSubfield = tidx % 1000;

            // 1/3rd of a second
            double t = GaBIEn.getTime();
            t = Math.floor(t * 3);
            int f = ((int) t) % 4;
            int[] frames = new int[] {
                    0, 1, 2, 1
            };
            int aniX = frames[f] * 16;

            int ovlX = aniX;
            if (tField == 1)
                ovlX += 48; // skip 3 columns

            int baseY = 64;
            int diamondY = 80;
            if (tField == 2) {
                diamondY += 16;
                baseY += 48;
            }

            handleWATField(tSubfield, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, ets, spriteScale);
        }
    }

    private void handleWATField(int tSubfield, int px, int py, IGrDriver igd, IImage chipset, int aniX, int baseY, int diamondY, int ovlX, int ets, int spriteScale) {

        int innerSubfield = tSubfield % 50;
        int outerSubfield = tSubfield / 50;

        char[] charTbl = {' ', '+', 'O', '|', '-'};

        ATDB adb = AppMain.autoTiles[1];
        char ul = charTbl[adb.entries[innerSubfield].corners[0]];
        char ur = charTbl[adb.entries[innerSubfield].corners[1]];
        char ll = charTbl[adb.entries[innerSubfield].corners[2]];
        char lr = charTbl[adb.entries[innerSubfield].corners[3]];

        int etc = ets / 2;
        handleWATCorner(0, 0, ((outerSubfield & 1) != 0) ? 'D' : ul, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc, spriteScale);
        if ((etc * 2) == ets) {
            handleWATCorner(etc, 0, ((outerSubfield & 2) != 0) ? 'D' : ur, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc, spriteScale);
            handleWATCorner(0, etc, ((outerSubfield & 4) != 0) ? 'D' : ll, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc, spriteScale);
            handleWATCorner(etc, etc, ((outerSubfield & 8) != 0) ? 'D' : lr, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc, spriteScale);
        }
    }

    private void handleWATCorner(int cx, int cy, char c, int px, int py, IGrDriver igd, IImage chipset, int aniX, int baseY, int diamondY, int ovlX, int etc, int spriteScale) {
        int tox = 0;
        int toy = 0;
        switch (c) {
            case ' ':
                tox = aniX;
                toy = baseY;
                break;
            case 'D':
                tox = aniX;
                toy = diamondY;
                break;
            case 'O':
                tox = ovlX;
                toy = 0;
                break;
            case '|':
                tox = ovlX;
                toy = 16;
                break;
            case '-':
                tox = ovlX;
                toy = 32;
                break;
            case '+':
                tox = ovlX;
                toy = 48;
                break;
        }
        RMEventGraphicRenderer.flexibleSpriteDraw(tox + cx, toy + cy, etc, etc, px + (cx * spriteScale), py + (cy * spriteScale), etc * spriteScale, etc * spriteScale, 0, chipset, 0, igd);
    }

    private void handleCommonPage(int base, int ofsPage, short tidx, int px, int py, IGrDriver igd, IImage chipset, int ets, int spriteScale) {
        // Divided into 6-wide columns, 96 tiles per column.
        int ti = tidx - base;
        ti += ofsPage * 144;
        int tx = (ti % 6) + ((ti / 96) * 6);
        int ty = ((ti / 6) % 16);
        RMEventGraphicRenderer.flexibleSpriteDraw(192 + (tx * 16), ty * 16, ets, ets, px, py, ets * spriteScale, ets * spriteScale, 0, chipset,0, igd);
    }

    private void handleATField(int subfield, int fx, int fy, int px, int py, IGrDriver igd, IImage chipset, int ets, int spriteScale) {
        XPTileRenderer.generalOldRMATField(fx * 16, fy * 16, subfield, 0, 16, ets, px, py, igd, chipset, spriteScale);
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        int[] genLcfATs = new int[80];
        for (int i = 0; i < 60; i++)
            genLcfATs[i] = i * 50;
        for (int i = 0; i < 20; i++)
            genLcfATs[i + 60] = 4000 + (i * 50);
        // On L0, lower layer tiles take priority,
        // on L1, upper layer tiles take priority
        if (mv.currentLayer == 0) {
            return new UITileGrid[] {
                    new UITileGrid(mv, 0, 80, 50, genLcfATs),

                    new UITileGrid(mv, 5000, 144, 0, null),

                    new UITileGrid(mv, 0, 1000, 0, null),
                    new UITileGrid(mv, 1000, 1000, 0, null),
                    new UITileGrid(mv, 2000, 1000, 0, null),

                    new UITileGrid(mv, 3000, 3, 0, new int[] {0, 50, 100}),

                    new UITileGrid(mv, 4000, 600, 0, null),
                    new UITileGrid(mv, 10000, 144, 0, null),
            };
        } else {
            return new UITileGrid[] {
                    new UITileGrid(mv, 10000, 144, 0, null),

                    new UITileGrid(mv, 0, 80, 50, genLcfATs),

                    new UITileGrid(mv, 5000, 144, 0, null),

                    new UITileGrid(mv, 0, 1000, 0, null),
                    new UITileGrid(mv, 1000, 1000, 0, null),
                    new UITileGrid(mv, 2000, 1000, 0, null),

                    new UITileGrid(mv, 3000, 3, 0, new int[] {0, 50, 100}),

                    new UITileGrid(mv, 4000, 600, 0, null),
            };
        }
    }

    @Override
    public String[] getPlaneNames(int layer) {
        if (layer == 0) {
            return new String[] {
                    "ATF",
                    "LOWER",
                    "W1M",
                    "W2M",
                    "W3M",
                    "ANI",
                    "TEM",
                    "UPPER<DNU>",
            };
        } else {
            return new String[] {
                    "UPPER",
                    "ATF<DNU>",
                    "LOWER<DNU>",
                    "W1M<DNU>",
                    "W2M<DNU>",
                    "W3M<DNU>",
                    "ANI<DNU>",
                    "TEM<DNU>",
            };
        }
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        LinkedList<AutoTileTypeField> attf = new LinkedList<AutoTileTypeField>();
        int[] waterIndexes = new int[60];
        for (int i = 0; i < waterIndexes.length; i++) {
            waterIndexes[i] = i * 50;
            attf.add(new AutoTileTypeField(i * 50, 50, 1, waterIndexes));
        }
        for (int i = 4000; i < 4600; i += 50)
            attf.add(new AutoTileTypeField(i, 50, 0));
        return attf.toArray(new AutoTileTypeField[0]);
    }

    @Override
    public int getFrame() {
        // 1/3rd * 1/8th = 1/24th
        double t = GaBIEn.getTime();
        return ((int) Math.floor(t * 24)) % 24;
    }

    @Override
    public int getRecommendedWidth() {
        return 6;
    }
}

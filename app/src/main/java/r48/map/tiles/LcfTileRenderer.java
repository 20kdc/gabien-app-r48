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
import r48.App;
import r48.dbs.ATDB;
import r48.io.data.IRIO;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

import java.util.LinkedList;

/**
 * I slept, finished MapUnit, and began writing this class.
 * Created on 31/05/17.
 */
public class LcfTileRenderer extends App.Svc implements ITileRenderer {
    public final IImage chipset;
    public static final int tileSize = 16;

    public LcfTileRenderer(App app, IImageLoader imageLoader, IRIO tso) {
        super(app);
        if (tso != null) {
            chipset = imageLoader.getImage("ChipSet/" + tso.getIVar("@tileset_name").decString(), false);
        } else {
            chipset = null;
        }
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, boolean editor) {
        if (chipset == null)
            return;
        // There are 288 "Common Tiles" (non-AT) divided into upper and lower layer tiles.
        // On the CS, they start at X 192.
        // Two pages of 144 each.
        // Everything here makes more sense in decimal.
        if ((tidx >= 5000) && (tidx < 6000))
            handleCommonPage(5000, 0, tidx, px, py, igd, chipset);
        if ((tidx >= 10000) && (tidx < 11000))
            handleCommonPage(10000, 1, tidx, px, py, igd, chipset);
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
            XPTileRenderer.generalOldRMATField(app, fx * tileSize, fy * tileSize, subfield, app.autoTiles[0], tileSize, px, py, igd, chipset);
            //igd.drawText(px, py, 255, 255, 255, 8, Integer.toString(field));
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
            igd.blitScaledImage((tileSize * 3) + (field * tileSize), (tileSize * 4) + (f * tileSize), tileSize, tileSize, px, py, tileSize, tileSize, chipset);
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
            int aniX = frames[f] * tileSize;

            int ovlX = aniX;
            if (tField == 1)
                ovlX += tileSize * 3; // skip 3 columns

            int baseY = tileSize * 4;
            int diamondY = tileSize * 5;
            if (tField == 2) {
                diamondY += tileSize;
                baseY += tileSize * 3;
            }

            handleWATField(tSubfield, px, py, igd, chipset, aniX, baseY, diamondY, ovlX);
        }
    }

    private void handleWATField(int tSubfield, int px, int py, IGrDriver igd, IImage chipset, int aniX, int baseY, int diamondY, int ovlX) {

        int innerSubfield = tSubfield % 50;
        int outerSubfield = tSubfield / 50;

        char[] charTbl = {' ', '+', 'O', '|', '-'};

        ATDB adb = app.autoTiles[1];
        char ul = charTbl[adb.entries[innerSubfield].corners[0]];
        char ur = charTbl[adb.entries[innerSubfield].corners[1]];
        char ll = charTbl[adb.entries[innerSubfield].corners[2]];
        char lr = charTbl[adb.entries[innerSubfield].corners[3]];

        int etc = tileSize / 2;
        handleWATCorner(0, 0, ((outerSubfield & 1) != 0) ? 'D' : ul, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc);
        if ((etc * 2) == tileSize) {
            handleWATCorner(etc, 0, ((outerSubfield & 2) != 0) ? 'D' : ur, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc);
            handleWATCorner(0, etc, ((outerSubfield & 4) != 0) ? 'D' : ll, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc);
            handleWATCorner(etc, etc, ((outerSubfield & 8) != 0) ? 'D' : lr, px, py, igd, chipset, aniX, baseY, diamondY, ovlX, etc);
        }
    }

    private void handleWATCorner(int cx, int cy, char c, int px, int py, IGrDriver igd, IImage chipset, int aniX, int baseY, int diamondY, int ovlX, int etc) {
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
                toy = tileSize;
                break;
            case '-':
                tox = ovlX;
                toy = tileSize * 2;
                break;
            case '+':
                tox = ovlX;
                toy = tileSize * 3;
                break;
        }
        igd.blitScaledImage(tox + cx, toy + cy, etc, etc, px + cx, py + cy, etc, etc, chipset);
    }

    private void handleCommonPage(int base, int ofsPage, short tidx, int px, int py, IGrDriver igd, IImage chipset) {
        // Divided into 6-wide columns, 96 tiles per column.
        int ti = tidx - base;
        ti += ofsPage * 144;
        int tx = (ti % 6) + ((ti / 96) * 6);
        int ty = ((ti / 6) % 16);
        igd.blitScaledImage(((tx + 12) * tileSize), ty * tileSize, tileSize, tileSize, px, py, tileSize, tileSize, chipset);
    }

    @Override
    public TileEditingTab[] getEditConfig(int layerIdx) {
        // There are 12 redundant groups in the 60 water subgroups.
        // There are then 12 autotile groups.
        int[] genLcfATs = new int[60];
        int ia = 0;
        for (int i = 0; i < 60; i++)
            if ((i % 20) < 16)
                genLcfATs[ia++] = i * 50;
        for (int i = 0; i < 12; i++)
            genLcfATs[ia++] = 4000 + (i * 50);
        // On L0, lower layer tiles take priority,
        // on L1, upper layer tiles take priority
        TileEditingTab atf = new TileEditingTab(app, "ATF", layerIdx != 0, genLcfATs, indicateATs());
        TileEditingTab lwr = new TileEditingTab("LOWER", layerIdx != 0, TileEditingTab.range(5000, 144));
        TileEditingTab ani = new TileEditingTab("ANI", layerIdx != 0, new int[] {3000, 3050, 3100});
        TileEditingTab tem = new TileEditingTab("TEM", layerIdx != 0, TileEditingTab.range(4000, 600));
        TileEditingTab w1m = new TileEditingTab("W1M", layerIdx != 0, TileEditingTab.range(0, 1000));
        TileEditingTab w2m = new TileEditingTab("W2M", layerIdx != 0, TileEditingTab.range(1000, 1000));
        TileEditingTab w3m = new TileEditingTab("W3M", layerIdx != 0, TileEditingTab.range(2000, 1000));
        TileEditingTab upr = new TileEditingTab("UPPER", layerIdx != 1, TileEditingTab.range(10000, 144));
        if (layerIdx == 0) {
            return new TileEditingTab[] {
                    atf,
                    lwr,
                    ani,
                    tem,
                    w1m,
                    w2m,
                    w3m,
                    upr
            };
        } else {
            return new TileEditingTab[] {
                    upr,
                    atf,
                    lwr,
                    ani,
                    tem,
                    w1m,
                    w2m,
                    w3m
            };
        }
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        LinkedList<AutoTileTypeField> attf = new LinkedList<AutoTileTypeField>();
        int[] waterIndexes = new int[60];
        for (int i = 0; i < waterIndexes.length; i++) {
            waterIndexes[i] = i * 50;
            int subgroup = i % 20;
            // Water should default to hoverlike form
            int rep = 0;
            // Some water indexes are better represented with different tiles...
            if ((subgroup == 0) && (i != 40))
                rep = 46;
            attf.add(new AutoTileTypeField(i * 50, 50, 1, rep, waterIndexes));
        }
        for (int i = 4000; i < 4600; i += 50)
            attf.add(new AutoTileTypeField(i, 50, 0, 49));
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

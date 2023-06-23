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
import r48.RubyTable;
import r48.dbs.ATDB;
import r48.io.data.IRIO;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * Created on 1/27/17.
 */
public class XPTileRenderer extends App.Svc implements ITileRenderer {
    public final IImage[] tilesetMaps = new IImage[8];

    public static final int tileSize = 32;
    public final RubyTable priorities;

    @Override
    public int getTileSize() {
        return tileSize;
    }

    public XPTileRenderer(App app, IImageLoader imageLoader, IRIO tileset) {
        super(app);
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
        if (tileset != null) {
            priorities = new RubyTable(tileset.getIVar("@priorities").getBuffer());
            IRIO tn = tileset.getIVar("@tileset_name");
            if (tn != null) {
                // XP
                String expectedTS = tn.decString();
                if (expectedTS.length() != 0)
                    tilesetMaps[0] = imageLoader.getImage("Tilesets/" + expectedTS, false);
                IRIO amNames = tileset.getIVar("@autotile_names");
                for (int i = 0; i < 7; i++) {
                    IRIO rio = amNames.getAElem(i);
                    String expectedAT = rio.decString();
                    if (expectedAT.length() > 0)
                        tilesetMaps[i + 1] = imageLoader.getImage("Autotiles/" + expectedAT, false);
                }
            }
        } else {
            priorities = null;
        }
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale, boolean editor) {
        // The logic here is only documented in the mkxp repository, in tilemap.cpp.
        // I really hope it doesn't count as stealing here,
        //  if I would've had to have typed this code ANYWAY
        //  after an age trying to figure it out.
        if (tidx < (48 * 8)) {
            // Autotile
            int atMap = tidx / 48;
            if (atMap == 0)
                return;
            tidx %= 48;
            boolean didDraw = false;
            if (tilesetMaps[atMap] != null) {
                int animControl = 0;
                int animSets = tilesetMaps[atMap].getWidth() / 96;
                if (animSets > 0)
                    animControl = (96 * (getFrame() % animSets));
                didDraw = didDraw || generalOldRMATField(app, animControl, 0, tidx, app.autoTiles[0], tileSize, px, py, igd, tilesetMaps[atMap], spriteScale);
            } else {
                didDraw = true; // It's invisible, so it should just be considered drawn no matter what
            }
            if (!didDraw)
                GaBIEn.engineFonts.f8.drawLAB(igd, px, py, ":" + tidx, false);
            return;
        }
        tidx -= 48 * 8;
        int tsh = 8;
        int tx = tidx % tsh;
        int ty = tidx / tsh;
        if (tilesetMaps[0] != null)
            RMEventGraphicRenderer.flexibleSpriteDraw(app, tx * tileSize, ty * tileSize, tileSize, tileSize, px, py, tileSize * spriteScale, tileSize * spriteScale, 0, tilesetMaps[0], 0, igd);
    }

    // Used by 2k3 support too, since it follows the same AT design
    public static boolean generalOldRMATField(App app, int tox, int toy, int subfield, ATDB atFieldType, int fTileSize, int px, int py, IGrDriver igd, IImage img, int spriteScale) {
        if (atFieldType != null) {
            if (subfield >= atFieldType.entries.length)
                return false;
            ATDB.Autotile at = atFieldType.entries[subfield];
            if (at != null) {
                int cSize = fTileSize / 2;
                for (int sA = 0; sA < 2; sA++)
                    for (int sB = 0; sB < 2; sB++) {
                        int ti = at.corners[sA + (sB * 2)];
                        int tx = ti % 3;
                        int ty = ti / 3;
                        int sX = (sA * cSize);
                        int sY = (sB * cSize);
                        RMEventGraphicRenderer.flexibleSpriteDraw(app, (tx * fTileSize) + sX + tox, (ty * fTileSize) + sY + toy, cSize, cSize, px + (sX * spriteScale), py + (sY * spriteScale), cSize * spriteScale, cSize * spriteScale, 0, img, 0, igd);
                    }
                return true;
            }
        }
        return false;
    }

    @Override
    public TileEditingTab[] getEditConfig(int layerIdx) {
        IImage tm0 = tilesetMaps[0];
        int tileCount = 48;
        if (tm0 != null)
            tileCount = ((tm0.getHeight() / 32) * 8);
        return new TileEditingTab[] {
                new TileEditingTab(app, "AUTO", false, new int[] {
                        0,
                        48,
                        48 * 2,
                        48 * 3,
                        48 * 4,
                        48 * 5,
                        48 * 6,
                        48 * 7
                }, indicateATs()),
                new TileEditingTab("TMAP", false, TileEditingTab.range(48 * 8, tileCount)),
                new TileEditingTab("AT-M", false, TileEditingTab.range(48, 48 * 7)),
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[] {
                new AutoTileTypeField(0, 48, 0, 47),
                new AutoTileTypeField(48, 48, 0, 47),
                new AutoTileTypeField(48 * 2, 48, 0, 47),
                new AutoTileTypeField(48 * 3, 48, 0, 47),
                new AutoTileTypeField(48 * 4, 48, 0, 47),
                new AutoTileTypeField(48 * 5, 48, 0, 47),
                new AutoTileTypeField(48 * 6, 48, 0, 47),
                new AutoTileTypeField(48 * 7, 48, 0, 47),
        };
    }

    @Override
    public int getFrame() {
        // 16 / 40
        double m = 16.0d / 40.0d;
        return (int) (GaBIEn.getTime() / m);
    }

    @Override
    public int getRecommendedWidth() {
        return 8;
    }
}

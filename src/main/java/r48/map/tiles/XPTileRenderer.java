/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.tiles;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.ATDB;
import r48.map.UIMapView;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.IImageLoader;
import r48.ui.UITileGrid;

/**
 * Created on 1/27/17.
 */
public class XPTileRenderer implements ITileRenderer {
    public final IImage[] tilesetMaps = new IImage[8];

    public static final int tileSize = 32;
    public final RubyTable priorities;

    @Override
    public int getTileSize() {
        return tileSize;
    }

    public XPTileRenderer(IImageLoader imageLoader, RubyIO tileset) {
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
        if (tileset != null) {
            priorities = new RubyTable(tileset.getInstVarBySymbol("@priorities").userVal);
            RubyIO tn = tileset.getInstVarBySymbol("@tileset_name");
            if (tn != null) {
                // XP
                String expectedTS = tn.decString();
                if (expectedTS.length() != 0)
                    tilesetMaps[0] = imageLoader.getImage("Tilesets/" + expectedTS, false);
                RubyIO[] amNames = tileset.getInstVarBySymbol("@autotile_names").arrVal;
                for (int i = 0; i < 7; i++) {
                    RubyIO rio = amNames[i];
                    if (rio.strVal.length != 0) {
                        String expectedAT = rio.decString();
                        tilesetMaps[i + 1] = imageLoader.getImage("Autotiles/" + expectedAT, false);
                    }
                }
            }
        } else {
            priorities = null;
        }
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale) {
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
                didDraw = didDraw || generalOldRMATField(animControl, 0, tidx, 0, tileSize, px, py, igd, tilesetMaps[atMap], spriteScale);
            } else {
                didDraw = true; // It's invisible, so it should just be considered drawn no matter what
            }
            if (!didDraw)
                UILabel.drawString(igd, px, py, ":" + tidx, false, UIMapView.mapDebugTextHeight);
            return;
        }
        tidx -= 48 * 8;
        int tsh = 8;
        int tx = tidx % tsh;
        int ty = tidx / tsh;
        if (tilesetMaps[0] != null)
            RMEventGraphicRenderer.flexibleSpriteDraw(tx * tileSize, ty * tileSize, tileSize, tileSize, px, py, tileSize * spriteScale, tileSize * spriteScale, 0, tilesetMaps[0], 0, igd);
    }

    // Used by 2k3 support too, since it follows the same AT design
    public static boolean generalOldRMATField(int tox, int toy, int subfield, int atFieldType, int fTileSize, int px, int py, IGrDriver igd, IImage img, int spriteScale) {
        if (AppMain.autoTiles[atFieldType] != null) {
            if (subfield >= AppMain.autoTiles[atFieldType].entries.length)
                return false;
            ATDB.Autotile at = AppMain.autoTiles[atFieldType].entries[subfield];
            if (at != null) {
                int cSize = fTileSize / 2;
                for (int sA = 0; sA < 2; sA++)
                    for (int sB = 0; sB < 2; sB++) {
                        int ti = at.corners[sA + (sB * 2)];
                        int tx = ti % 3;
                        int ty = ti / 3;
                        int sX = (sA * cSize);
                        int sY = (sB * cSize);
                        RMEventGraphicRenderer.flexibleSpriteDraw((tx * fTileSize) + sX + tox, (ty * fTileSize) + sY + toy, cSize, cSize, px + (sX * spriteScale), py + (sY * spriteScale), cSize * spriteScale, cSize * spriteScale, 0, img, 0, igd);
                    }
                return true;
            }
        }
        return false;
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        IImage tm0 = tilesetMaps[0];
        int tileCount = 48;
        if (tm0 != null)
            tileCount = ((tm0.getHeight() / 32) * 8);
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 48, 0, null, "NULL"),
                new UITileGrid(mv, 0, 7, 48, new int[] {
                        48,
                        48 * 2,
                        48 * 3,
                        48 * 4,
                        48 * 5,
                        48 * 6,
                        48 * 7,
                }, "AUTO"),
                new UITileGrid(mv, 48, 48 * 7, 0, null, "AT-M"),
                new UITileGrid(mv, 48 * 8, tileCount, 0, null, "TMAP"),
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[] {
                new AutoTileTypeField(0, 48, 0),
                new AutoTileTypeField(48, 48, 0),
                new AutoTileTypeField(48 * 2, 48, 0),
                new AutoTileTypeField(48 * 3, 48, 0),
                new AutoTileTypeField(48 * 4, 48, 0),
                new AutoTileTypeField(48 * 5, 48, 0),
                new AutoTileTypeField(48 * 6, 48, 0),
                new AutoTileTypeField(48 * 7, 48, 0),
        };
    }

    @Override
    public int getFrame() {
        // Need to work out acceleration for this. Going w/ 4
        return (int) (GaBIEn.getTime() * 4);
    }

    @Override
    public int getRecommendedWidth() {
        return 8;
    }
}

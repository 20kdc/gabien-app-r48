/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.ATDB;
import r48.map.UIMapView;
import r48.map.imaging.IImageLoader;
import r48.ui.UITileGrid;

/**
 * Created on 1/27/17.
 */
public class XPTileRenderer implements ITileRenderer {
    private final RubyIO tileset;
    public final IGrInDriver.IImage[] tilesetMaps = new IGrInDriver.IImage[8];

    public static final int tileSize = 32;

    @Override
    public int getTileSize() {
        return tileSize;
    }

    public XPTileRenderer(IImageLoader imageLoader, RubyIO tileset) {
        this.tileset = tileset;
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
        if (tileset != null) {
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
        }
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int ets) {
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
                didDraw = didDraw || generalOldRMATField(0, 0, tidx, 0, tileSize, ets, px, py, igd, tilesetMaps[atMap]);
            } else {
                didDraw = true; // It's invisible, so it should just be considered drawn no matter what
            }
            if (!didDraw)
                UILabel.drawString(igd, px, py, ":" + tidx, false, FontSizes.mapDebugTextHeight);
            return;
        }
        tidx -= 48 * 8;
        int tsh = 8;
        int tx = tidx % tsh;
        int ty = tidx / tsh;
        if (tilesetMaps[0] != null)
            igd.blitImage(tx * tileSize, ty * tileSize, ets, ets, px, py, tilesetMaps[0]);
    }

    // Used by 2k3 support too, since it follows the same AT design
    public static boolean generalOldRMATField(int tox, int toy, int subfield, int atFieldType, int fTileSize, int ets, int px, int py, IGrDriver igd, IGrInDriver.IImage img) {
        if ((ets == fTileSize) && (AppMain.autoTiles[atFieldType] != null)) {
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
                        igd.blitImage((tx * fTileSize) + sX + tox, (ty * fTileSize) + sY + toy, cSize, cSize, px + sX, py + sY, img);
                    }
                return true;
            }
        } else {
            igd.blitImage(tox + fTileSize, toy + (2 * fTileSize), ets, ets, px, py, img);
            return true;
        }
        return false;
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        IGrInDriver.IImage tm0 = tilesetMaps[0];
        int tileCount = 48;
        if (tm0 != null)
            tileCount = ((tm0.getHeight() / 32) * 8);
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 7, 48, new int[] {
                        48,
                        48 * 2,
                        48 * 3,
                        48 * 4,
                        48 * 5,
                        48 * 6,
                        48 * 7,
                }),
                new UITileGrid(mv, 0, 48, 0, null),
                new UITileGrid(mv, 48, 48 * 7, 0, null),
                new UITileGrid(mv, 48 * 8, tileCount, 0, null),
        };
    }

    @Override
    public String[] getPlaneNames(int layer) {
        return new String[] {
                "AUTO",
                "NULL",
                "AT-M",
                "TMAP",
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
        return 0;
    }
}

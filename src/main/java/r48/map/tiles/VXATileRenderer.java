/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.ATDB;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * This uses a totally different system from XP, based around 5 AT sheets and 4 primary sheets.
 * Created on 1/27/17.
 */
public class VXATileRenderer implements ITileRenderer {
    public final IGrInDriver.IImage[] tilesetMaps = new IGrInDriver.IImage[9];
    private final RubyIO tileset;

    public String panoramaSetup = "";

    public VXATileRenderer(RubyIO tileset, String vxaPano) {
        this.tileset = tileset;
        panoramaSetup = vxaPano;
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
        if (tileset != null) {
            RubyIO[] amNames = tileset.getInstVarBySymbol("@tileset_names").arrVal;
            for (int i = 0; i < tilesetMaps.length; i++) {
                RubyIO rio = amNames[i];
                if (rio.strVal.length != 0) {
                    String expectedAT = rio.decString();
                    tilesetMaps[i] = GaBIEn.getImage(AppMain.rootPath + "Graphics/Tilesets/" + expectedAT + ".png", 0, 0, 0);
                }
            }
        }
    }


    @Override
    public void drawTile(short tidx, int px, int py, IGrInDriver igd, int ets) {
        // MKXP repository links to http://www.tktkgame.com/tkool/memo/vx/tile_id.html
        // It's in Japanese but via translation at least explains that:
        // 1. Autotiles are different. Very different.
        //    (There *seem* to still be 48-tile planes, however.
        //     There are just additional less comprehensible planes.)
        // Gist of it is that autotiles are now in the higher sections (0x800+),
        //  0x0YY/0x1YY/0x2YY/0x3YY are the "upper layer" detail planes (not sure what's going on here)
        //  0x600-0x67F seems to be the closest thing I could find to "normal".

        // 0xBE1 == 3041, horizontal line on L1 of Map047, RS2.
        if (tidx == 0)
            return; // magical exception

        int plane = tidx & 0xFF00;
        plane /= 0x100;

        if (plane >= 0)
            if (plane < 4) {
                int t = tidx & 0xFF;
                IGrInDriver.IImage planeImage = tilesetMaps[plane + 5];
                if (planeImage != null) {
                    int tgtX = t % 8;
                    int tgtY = t / 8;
                    igd.blitImage(tgtX * ITileRenderer.tileSize, tgtY * ITileRenderer.tileSize, ets, ets, px, py, planeImage);
                    return;
                }
            }

        // AT Planes

        if (plane >= 0x08)
            if (plane <= 0x0A)
                if (handleATLayer(tidx, 0x0800, ets, px, py, 0, igd))
                    return;

        if (plane >= 0x0B)
            if (plane <= 0x10)
                if (handleATLayer(tidx, 0x0B00, ets, px, py, 1, igd))
                    return;

        // Plane 6

        if (plane == 6) {
            int t = tidx & 0xFF;
            IGrInDriver.IImage planeImage = tilesetMaps[4];
            if (planeImage != null) {
                // Only makes sense with a baseline of 8 but that leads to 'corrupted'-looking data in parts hm.
                int tgtX = t % 8;
                int tgtY = t / 8;
                igd.blitImage(tgtX * ITileRenderer.tileSize, tgtY * ITileRenderer.tileSize, ets, ets, px, py, planeImage);
                return;
            }
        }

        igd.drawText(px, py, 255, 255, 255, 8, ":" + tidx);
    }

    private boolean handleATLayer(short tidx, int base, int ets, int px, int py, int tm, IGrInDriver igd) {
        int tin = tidx - base;
        int atid = tin % 48;
        int apid = tin / 48;
        int pox = apid % 8;
        int poy = apid / 8;
        pox *= (ITileRenderer.tileSize * 2);
        poy *= (ITileRenderer.tileSize * 3);
        IGrInDriver.IImage planeImg = tilesetMaps[tm];
        if (planeImg != null) {
            if (ets == tileSize) {
                ATDB.Autotile at = AppMain.autoTiles.entries[atid];
                if (at != null) {
                    int cSize = tileSize / 2;
                    for (int sA = 0; sA < 2; sA++)
                        for (int sB = 0; sB < 2; sB++) {
                            int ti = at.corners[sA + (sB * 2)];
                            int tx = (ti % 3) * ITileRenderer.tileSize;
                            int ty = (ti / 3) * ITileRenderer.tileSize;
                            if (ti == 2) {
                                tx = ITileRenderer.tileSize;
                            } else if (ti > 2) {
                                ty -= ITileRenderer.tileSize;
                                tx /= 2;
                                ty /= 2;
                                ty += ITileRenderer.tileSize;
                            }
                            int sX = (sA * cSize);
                            int sY = (sB * cSize);
                            igd.blitImage(tx + pox + sX, ty + poy + sY, cSize, cSize, px + sX, py + sY, planeImg);
                        }
                    return true;
                }
            } else {
                igd.blitImage(tileSize, 2 * tileSize, ets, ets, px, py, planeImg);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPanorama() {
        if (panoramaSetup.length() == 0)
            return "";
        return "Parallaxes/" + panoramaSetup;
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 0x1000, false),
        };
    }

    @Override
    public String[] getPlaneNames() {
        return new String[] {
                "TM"
        };
    }
}

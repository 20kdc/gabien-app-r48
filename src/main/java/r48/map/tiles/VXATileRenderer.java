/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.ATDB;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

import java.util.LinkedList;

/**
 * This uses a totally different system from XP, based around 5 AT sheets and 4 primary sheets.
 * Created on 1/27/17.
 */
public class VXATileRenderer implements ITileRenderer {

    public static final int tileSize = 32;

    @Override
    public int getTileSize() {
        return tileSize;
    }

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
    public void drawTile(int layer, short tidx, int px, int py, IGrInDriver igd, int ets) {
        // MKXP repository links to http://www.tktkgame.com/tkool/memo/vx/tile_id.html
        // It's in Japanese but via translation at least explains that:
        // 1. Autotiles are different. Very different.
        //    (There *seem* to still be 48-tile planes, however.
        //     There are just additional less comprehensible planes.)
        // Gist of it is that autotiles are now in the higher sections (0x800+),
        //  0x0YY/0x1YY/0x2YY/0x3YY are the "upper layer" detail planes (not sure what's going on here)
        //  0x600-0x67F seems to be the closest thing I could find to "normal".

        // 0xBE1 == 3041, horizontal line on L1 of Map047, RS2.
        if (layer == 3) {
            UILabel.drawString(igd, px, py, Integer.toHexString(tidx), false, 8);
            return;
        }

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
                    igd.blitImage(tgtX * tileSize, tgtY * tileSize, ets, ets, px, py, planeImage);
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
            if (t >= 0x7F)
                return;
            IGrInDriver.IImage planeImage = tilesetMaps[4];
            if (planeImage != null) {
                // Only makes sense with a baseline of 8 but that leads to 'corrupted'-looking data in parts hm.
                int tgtX = t % 8;
                int tgtY = t / 8;
                igd.blitImage(tgtX * tileSize, tgtY * tileSize, ets, ets, px, py, planeImage);
                return;
            }
        }

        UILabel.drawString(igd, px, py, Integer.toHexString(tidx), false, 8);
    }

    private boolean handleATLayer(short tidx, int base, int ets, int px, int py, int tm, IGrInDriver igd) {
        int tin = tidx - base;
        int atid = tin % 48;
        int apid = tin / 48;
        int pox = apid % 8;
        int poy = apid / 8;
        pox *= tileSize * 2;
        poy *= tileSize * 3;
        IGrInDriver.IImage planeImg = tilesetMaps[tm];
        if (planeImg != null) {
            if (ets == tileSize) {
                ATDB.Autotile at = AppMain.autoTiles.entries[atid];
                if (at != null) {
                    int cSize = tileSize / 2;
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
        return "Graphics/Parallaxes/" + panoramaSetup + ".png";
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0x67F, 1, false),
                new UITileGrid(mv, 0x000, 0x400, false),
                new UITileGrid(mv, 0x600, 0x7F, false),
                new UITileGrid(mv, 0x800, 0x300, true),
                new UITileGrid(mv, 0xB00, 0x600, true),
        };
    }

    @Override
    public String[] getPlaneNames() {
        return new String[] {
                "NIL",
                "pABCD",
                "6+",
                "8+",
                "B+",
        };
    }

    @Override
    public int[] indicateATs() {
        LinkedList<Integer> atFields = new LinkedList<Integer>();
        for (int i = 0; i < 32; i++) {
            int resultingAddr = i * 48;
            int resultingEndAddr = resultingAddr + 47;
            if (resultingEndAddr < 0x300)
                atFields.add(0x800 + resultingAddr);
            if (resultingEndAddr < 0x600)
                atFields.add(0xB00 + resultingAddr);
        }
        int[] r = new int[atFields.size()];
        for (int i = 0; i < r.length; i++)
            r[i] = atFields.get(i);
        return r;
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.RubyIO;
import r48.map.UIMapView;
import r48.map.imaging.IImageLoader;
import r48.ui.UITileGrid;

/**
 * I slept, finished MapUnit, and began writing this class.
 * Created on 31/05/17.
 */
public class LcfTileRenderer implements ITileRenderer {
    public final IGrInDriver.IImage chipset;
    public final String panorama;

    public LcfTileRenderer(IImageLoader imageLoader, RubyIO tso, String vxaPano) {
        if (vxaPano.equals("")) {
            panorama = "";
        } else {
            panorama = "Panorama/" + vxaPano;
        }
        if (tso != null) {
            chipset = imageLoader.getImage("ChipSet/" + tso.getInstVarBySymbol("@tileset_name").decString(), 0, 0, 0);
        } else {
            chipset = null;
        }
    }

    @Override
    public int getTileSize() {
        return 16;
    }

    @Override
    public int[] tileLayerDrawOrder() {
        return new int[] {
                0, 1
        };
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrInDriver igd, int ets) {
        if (chipset == null)
            return;
        // There are 288 "Common Tiles" (non-AT) divided into upper and lower layer tiles.
        // On the CS, they start at X 192.
        // Two pages of 144 each.
        // Everything here makes more sense in decimal.
        if ((tidx >= 5000) && (tidx < 5144))
            handleCommonPage(5000, 0, tidx, px, py, igd, chipset, ets);
        if ((tidx >= 10000) && (tidx < 10144))
            handleCommonPage(10000, 1, tidx, px, py, igd, chipset, ets);
        // This is a possible *50-wide AT Field!!!!!* Well, 12 of them.
        // Terrain ATs are laid out as follows:
        // ??45
        // ??67
        // 0189
        // 23AB
        if ((tidx >= 4000) && (tidx < 4600)) {
            int field = ((tidx - 4000) / 50) + 4;
            int subfield = (tidx - 4000) % 50;

            int fx = ((field % 2) * 3) + ((field / 8) * 6);
            int fy = (field / 2) % 4;
            handleATField(subfield, fx, fy, px, py, igd, chipset, ets);
            // igd.drawText(px, py, 255, 255, 255, 8, Integer.toString(field));
        }
    }

    private void handleCommonPage(int base, int ofsPage, short tidx, int px, int py, IGrInDriver igd, IGrInDriver.IImage chipset, int ets) {
        // Divided into 6-wide columns, 96 tiles per column.
        int ti = tidx - base;
        ti += ofsPage * 144;
        int tx = (ti % 6) + ((ti / 96) * 6);
        int ty = ((ti / 6) % 16);
        igd.blitImage(192 + (tx * 16), ty * 16, ets, ets, px, py, chipset);
    }

    private void handleATField(int subfield, int fx, int fy, int px, int py, IGrInDriver igd, IGrInDriver.IImage chipset, int ets) {
        XPTileRenderer.generalOldRMATField(fx * 16, fy * 16, subfield, 0, 16, ets, px, py, igd, chipset);
    }

    @Override
    public String getPanorama() {
        return panorama;
    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 16, false)
        };
    }

    @Override
    public String[] getPlaneNames(int layer) {
        return new String[] {
            "Primary Plane"
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[0];
    }
}

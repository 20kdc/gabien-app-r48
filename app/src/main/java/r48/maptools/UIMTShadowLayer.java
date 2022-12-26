/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.FontManager;
import gabien.ui.UILabel;
import gabien.ui.UINumberBox;
import gabien.ui.UISplitterLayout;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;

/**
 * UIMTAutotile isn't flexible enough for this.
 * Created on 2/19/17.
 */
public class UIMTShadowLayer extends UIMTBase implements IMapViewCallbacks {
    public final UIMapView map;
    private UINumberBox regionId;

    public UIMTShadowLayer(IMapToolContext mv) {
        super(mv);
        map = mv.getMapView();
        changeInner(new UISplitterLayout(new UILabel(TXDB.get("Region:"), FontSizes.tableElementTextHeight), regionId = new UINumberBox(0, FontSizes.tableElementTextHeight), false, 1, 2), true);
    }

    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        /*
        if (mouse)
            if (cx == tx)
                if (cy == ty)
                    if (layer == 3)
                        return (short) flags;*/
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        if (minimap)
            return 0;
        return 1;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, int layer, boolean minimap) {
        for (int tx = mvdc.camT.x; tx < mvdc.camT.x + mvdc.camT.width; tx++) {
            if (map.mapTable.outOfBounds(tx, 0))
                continue;
            for (int ty = mvdc.camT.y; ty < mvdc.camT.y + mvdc.camT.height; ty++) {
                if (map.mapTable.outOfBounds(tx, ty))
                    continue;
                int px = tx * mvdc.tileSize;
                int py = ty * mvdc.tileSize;
                int regionId = (map.mapTable.getTiletype(tx, ty, 3) & 0xFF00) >> 8;
                int l = FontManager.getLineLength("R" + regionId, UIMapView.mapDebugTextHeight) + 1;
                mvdc.igd.clearRect(0, 0, 0, px, py, l, UIMapView.mapDebugTextHeight);
                FontManager.drawString(mvdc.igd, px, py, "R" + regionId, true, false, UIMapView.mapDebugTextHeight);
            }
        }
    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        if (map.mapTable.outOfBounds(x, y))
            return;
        int shadowBasis = map.mapTable.getTiletype(x, y, 3) & 0x0F;
        int sz = map.tileSize / 2;
        int flagId = 0;
        if (pixx > sz)
            flagId++;
        if (pixy > sz)
            flagId += 2;
        shadowBasis ^= 1 << flagId;
        map.mapTable.setTiletype(x, y, 3, (short) (shadowBasis | (regionId.number << 8)));
        map.passModificationNotification();
    }
}

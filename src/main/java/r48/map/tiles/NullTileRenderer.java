/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.tiles;

import gabien.IGrDriver;
import r48.dbs.TXDB;
import r48.map.UIMapView;
import r48.ui.UITileGrid;

/**
 * Created on 1/27/17.
 */
public class NullTileRenderer implements ITileRenderer {
    @Override
    public int getTileSize() {
        return 32;
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale) {

    }

    @Override
    public UITileGrid[] createATUIPlanes(UIMapView mv, int sc) {
        return new UITileGrid[] {
                new UITileGrid(mv, 0, 1, 0, null, TXDB.get("Tiles Unsupported"), sc)
        };
    }


    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[0];
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

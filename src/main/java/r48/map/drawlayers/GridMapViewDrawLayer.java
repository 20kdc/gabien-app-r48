/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.IGrDriver;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;

public class GridMapViewDrawLayer implements IMapViewDrawLayer {
    @Override
    public String getName() {
        return TXDB.get("RM-Style Grid Overlay");
    }

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        for (int i = camTX; i < camTR; i++) {
            int a = ((i * eTileSize) + (eTileSize - 1)) - camX;
            igd.clearRect(0, 0, 0, a, 0, 1, igd.getHeight());
        }
        for (int i = camTY; i < camTB; i++) {
            int a = ((i * eTileSize) + (eTileSize - 1)) - camY;
            igd.clearRect(0, 0, 0, 0, a, igd.getWidth(), 1);
        }
    }
}

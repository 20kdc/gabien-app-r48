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
import r48.ui.Art;

/**
 * Created on 6/16/18.
 */
public class BorderMapViewDrawLayer implements IMapViewDrawLayer {
    public int width, height;
    public BorderMapViewDrawLayer(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public String getName() {
        return TXDB.get("Map Border");
    }

    @Override
    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        for (int i = Math.max(camTX, 0); i < Math.min(camTR, width); i++) {
            Art.drawSymbol(igd, Art.Symbol.Stripes, (i * eTileSize) - camX, -(camY + eTileSize), eTileSize, false, false);
            Art.drawSymbol(igd, Art.Symbol.Stripes, (i * eTileSize) - camX, (height * eTileSize) - camY, eTileSize, false, false);
        }
        for (int j = Math.max(camTY, -1); j < Math.min(camTB, height + 1); j++) {
            Art.drawSymbol(igd, Art.Symbol.Stripes, -(camX + eTileSize), (j * eTileSize) - camY, eTileSize, false, false);
            Art.drawSymbol(igd, Art.Symbol.Stripes, (width * eTileSize) - camX, (j * eTileSize) - camY, eTileSize, false, false);
        }
    }
}

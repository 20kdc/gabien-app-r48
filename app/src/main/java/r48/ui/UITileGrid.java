/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.render.IGrDriver;
import r48.R48;
import r48.map.StuffRenderer;

/**
 * Tile selection, now with more hax for the AutoTile button. (not-a-TM)
 * Created on 12/31/16.
 */
public class UITileGrid extends UIGrid {
    public final int layer;
    public final int spriteScale;
    public final StuffRenderer renderer;
    // Used to implement a feature Demetrius recommended: borders around AT field elements.
    public final int borderWidth;

    public final int[] viewMap, viewMapHover;

    private String toStringRes = "Nts/UITileGrid";

    public UITileGrid(R48 app, StuffRenderer sr, int layer, int[] remap, int sprScale) {
        this(app, sr, 0, layer, remap, remap, sprScale);
    }

    public UITileGrid(R48 app, StuffRenderer sr, int borderWidth, int layer, int[] mapN, int[] mapH, int sprScale) {
        super(app, 1337, 1337, mapN.length);
        if (mapN.length != mapH.length)
            throw new IndexOutOfBoundsException("hover remap table must match regular remap table in length");
        tileSizeW = tileSizeH = (sr.tileRenderer.tileSize * sprScale) + (borderWidth * 2);
        this.borderWidth = borderWidth;
        renderer = sr;
        this.layer = layer;
        bkgR = 128;
        bkgB = 128;
        viewMap = mapN;
        viewMapHover = mapH;
        spriteScale = sprScale;
    }

    public UITileGrid setName(String name) {
        toStringRes = name;
        return this;
    }

    @Override
    public String toString() {
        return toStringRes;
    }

    @Override
    protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
        t = hover ? viewMapHover[t] : viewMap[t];
        renderer.tileRenderer.drawTile(layer, (short) t, x + borderWidth, y + borderWidth, igd, spriteScale);
    }
}

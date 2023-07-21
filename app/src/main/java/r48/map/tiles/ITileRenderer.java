/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.tiles;

import org.eclipse.jdt.annotation.Nullable;

import gabien.atlas.AtlasSet;
import gabien.render.IGrDriver;
import r48.App;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * Used because this changes a LOT
 * Created on 1/27/17.
 */
public abstract class ITileRenderer extends App.Svc {
    /**
     * Tile size in pixels.
     */
    public final int tileSize;

    /**
     * Recommended width (in tiles) in tile selection pane.
     * Tilesets are built to this standard.
     */
    public final int recommendedWidth;

    public ITileRenderer(App app, int ts, int rw) {
        super(app);
        tileSize = ts;
        recommendedWidth = rw;
    }

    // NOTE: ETS should be how much of the tile to use at spriteScale 1.
    // When in minimap, it's at an appropriate value (< tileSize).
    // Otherwise, it's tileSize.
    // spriteScale meanwhile is how much to "zoom" the tile.
    // So the size to render is ETS * spriteScale.
    // spriteScale is used in most situations, but not UIMapView.
    // There, it is instead done "in bulk" on an offscreen buffer,
    //  since the offscreen buffer had to be used anyway for *other* efficiency reasons.
    // NOTE: The flag "editor" means "anything not a UIMapView"
    public abstract void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, boolean editor);
    public final void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale, boolean editor) {
        if (spriteScale != 1) {
            float rx = igd.trsTXS(px), ry = igd.trsTYS(py);
            float rsx = igd.trs[2], rsy = igd.trs[3];
            igd.trs[2] = rsx * spriteScale;
            igd.trs[3] = rsy * spriteScale;
            drawTile(layer, tidx, 0, 0, igd, editor);
            igd.trs[2] = rsx;
            igd.trs[3] = rsy;
            igd.trsTXYE(rx, ry);
        } else {
            drawTile(layer, tidx, px, py, igd, editor);
        }
    }

    // Returning a length of 0 makes the layer uneditable from UIMTAutotile.
    public abstract TileEditingTab[] getEditConfig(int layerIdx);

    public abstract AutoTileTypeField[] indicateATs();

    // Used to sync the map view and playing animations.
    public abstract int getFrame();

    /**
     * Debugging mechanism to get the atlas set of this renderer.
     */
    public @Nullable AtlasSet getAtlasSet() {
        return null;
    }
}

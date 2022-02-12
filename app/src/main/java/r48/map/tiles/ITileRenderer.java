/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.tiles;

import gabien.IGrDriver;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * Used because this changes a LOT
 * Created on 1/27/17.
 */
public interface ITileRenderer {
    int getTileSize();

    // NOTE: ETS should be how much of the tile to use at spriteScale 1.
    // When in minimap, it's at an appropriate value (< tileSize).
    // Otherwise, it's tileSize.
    // spriteScale meanwhile is how much to "zoom" the tile.
    // So the size to render is ETS * spriteScale.
    // spriteScale is used in most situations, but not UIMapView.
    // There, it is instead done "in bulk" on an offscreen buffer,
    //  since the offscreen buffer had to be used anyway for *other* efficiency reasons.
    // NOTE: The flag "editor" means "anything not a UIMapView"
    void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, int spriteScale, boolean editor);

    // Returning a length of 0 makes the layer uneditable from UIMTAutotile.
    TileEditingTab[] getEditConfig(int layerIdx);

    AutoTileTypeField[] indicateATs();

    // Used to sync the map view and playing animations.
    int getFrame();

    // The standardized tilemap width (8 for RXP, 6 for R2k)
    int getRecommendedWidth();
}

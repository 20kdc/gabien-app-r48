/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.tiles;

import org.eclipse.jdt.annotation.Nullable;

import r48.R48;
import r48.io.data.IRIO;
import r48.map2d.tiles.TileRenderer;

/**
 * Created 24th July, 2023, so RXPSystem and RVXASystem will stop fighting.
 */
public abstract class TSOAwareTileRenderer extends TileRenderer {
    public final R48 app;
    public TSOAwareTileRenderer(R48 app, int ts, int rw) {
        super(ts, rw);
        this.app = app;
    }

    /**
     * Sets up this tile renderer given a "TileSet Object" (whatever that means in the local system).
     * Note that this is a backend-specific object and may just plain not strictly be the tileset itself, but a reference to it.
     */
    public abstract void checkReloadTSO(@Nullable IRIO tso);
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.RubyTable;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.ui.UIAppendButton;

/**
 * NOTE @ 16th November 2018: As this really needs a preview, I've made two sets of behavior, one set dependent on mobile.
 * This is to patch over the usability issues that have occurred because of other UI edits.
 * Created on September 2, 2017
 */
public class UIMTPasteRectangle extends UIMTBase implements IMapViewCallbacks {

    private final RubyTable table;
    private UILabel innerLabel = new UILabel(app.ts("Click at the target, or close this window."), app.f.dialogWindowTextHeight);

    private int confirmX, confirmY;
    private UIElement confirmButton = new UIAppendButton(app.ts("Cancel"), new UITextButton(app.ts("Confirm"), app.f.dialogWindowTextHeight, new Runnable() {
        @Override
        public void run() {
            actualConfirm(confirmX, confirmY);
            changeInner(innerLabel, false);
            confirming = false;
        }
    }), new Runnable() {
        @Override
        public void run() {
            changeInner(innerLabel, false);
            confirming = false;
        }
    }, app.f.dialogWindowTextHeight);
    private boolean confirming = false;

    public UIMTPasteRectangle(IMapToolContext par, RubyTable clipboard) {
        super(par);
        changeInner(innerLabel, true);
        table = clipboard;
    }

    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        int cx;
        int cy;
        if (confirming) {
            cx = confirmX;
            cy = confirmY;
        } else {
            if (mouse == null)
                return there;
            cx = mouse.x;
            cy = mouse.y;
        }
        if (tx < cx)
            return there;
        if (ty < cy)
            return there;
        if (tx >= cx + table.width)
            return there;
        if (ty >= cy + table.height)
            return there;
        int px = tx - cx;
        int py = ty - cy;
        return table.getTiletype(px, py, layer);
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 0;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {

    }

    @Override
    public void confirmAt(final int x, final int y, int pixx, int pixy, final int layer, boolean first) {
        if (!first)
            return;
        if (app.ui.isMobile && !confirming) {
            // Need to absolutely confirm.
            confirmX = x;
            confirmY = y;
            changeInner(confirmButton, false);
            confirming = true;
        } else {
            actualConfirm(x, y);
        }
    }

    private void actualConfirm(int x, int y) {
        UIMapView map = mapToolContext.getMapView();
        for (int l = 0; l < table.planeCount; l++)
            for (int i = 0; i < table.width; i++)
                for (int j = 0; j < table.height; j++)
                    if (!map.mapTable.outOfBounds(i + x, j + y))
                        map.mapTable.setTiletype(i + x, j + y, l, table.getTiletype(i, j, l));
        map.passModificationNotification();
    }
}

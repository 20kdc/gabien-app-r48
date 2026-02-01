/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import org.eclipse.jdt.annotation.NonNull;

import gabien.GaBIEn;
import gabien.ui.elements.UILabel;
import r48.map.AppMapViewDrawContext;
import r48.render2d.IMapViewCallbacks;
import r48.render2d.MapViewDrawContext;
import r48.ui.UIAppendButton;

/**
 * Created on 10/06/17.
 */
public class UIMTAutotileRectangle extends UIMTBase implements IMapViewCallbacks {

    public final UIMTAutotile parent;
    public final int startX, startY;
    public final boolean autotile;

    public UIAppendButton innerLabel = new UIAppendButton(T.m.tCancel, new UILabel(T.m.tsFinishRect, app.f.dialogWindowTH), new Runnable() {
        @Override
        public void run() {
            parent.selfClose = false;
            parent.hasClosed = false;
            mapToolContext.accept(parent);
        }
    }, app.f.dialogWindowTH);

    public UIMTAutotileRectangle(UIMTAutotile par, int x, int y, boolean at) {
        super(par.mapToolContext);
        changeInner(innerLabel, true);
        parent = par;
        startX = x;
        startY = y;
        autotile = at;
    }

    @Override
    public int shouldDrawAt(AppMapViewDrawContext.MouseStatus mouse, int tx, int ty, int there, int layer, int currentLayer) {
        if (mouse != null) {
            int minX = Math.min(startX, mouse.x);
            int maxX = Math.max(startX, mouse.x);
            int minY = Math.min(startY, mouse.y);
            int maxY = Math.max(startY, mouse.y);
            if (tx >= minX)
                if (ty >= minY)
                    if (tx <= maxX)
                        if (ty <= maxY)
                            return parent.getTCSelected(tx - startX, ty - startY);
        }
        return there;
    }

    private int subFrame() {
        return ((int) (GaBIEn.getTime() * 4)) & 1;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, boolean minimap) {
        if (!minimap)
            if (subFrame() == 0)
                mvdc.drawIndicator(startX, startY, MapViewDrawContext.IndicatorStyle.SolidBlue);
    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        if (!parent.map.mapTable.outOfBounds(x, y)) {
            int minX = Math.min(startX, x);
            int maxX = Math.max(startX, x);
            int minY = Math.min(startY, y);
            int maxY = Math.max(startY, y);
            parent.performGeneralRectangle(layer, startX, startY, minX, minY, maxX, maxY);
            parent.selfClose = false;
            parent.hasClosed = false;
            mapToolContext.accept(parent);
        }
    }

    @Override
    @NonNull
    public String viewState(int mouseXT, int mouseYT) {
        return "ATR." + subFrame() + "." + mouseXT + "." + mouseYT;
    }
}

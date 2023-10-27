/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNull;

import gabien.GaBIEn;
import gabien.ui.elements.UILabel;
import r48.RubyTable;
import r48.io.data.IRIOGeneric;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.ui.UIAppendButton;

/**
 * Created on September 2, 2017
 */
public class UIMTCopyRectangle extends UIMTBase implements IMapViewCallbacks {

    public int startX, startY;
    public boolean stage;

    public UILabel innerLabel = new UILabel(T.m.tsStartRect, app.f.dialogWindowTH);
    public UIAppendButton inner = new UIAppendButton(T.m.tCancel, innerLabel, new Runnable() {
        @Override
        public void run() {
            mapToolContext.accept(null);
        }
    }, app.f.dialogWindowTH);

    public UIMTCopyRectangle(IMapToolContext par) {
        super(par);
        changeInner(inner, true);
    }

    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    private int subFrame() {
        return ((int) (GaBIEn.getTime() * 4)) & 1;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, boolean minimap) {
        if (!stage)
            return;
        if (minimap)
            return;
        UIMapView map = mapToolContext.getMapView();
        int px = startX * mvdc.tileSize;
        int py = startY * mvdc.tileSize;
        if (subFrame() == 0)
            mvdc.igd.clearRect(0, 0, 255, px, py, map.tileSize, map.tileSize);
        mvdc.drawMouseIndicator();
    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
        if (!first)
            return;
        if (stage) {
            UIMapView map = mapToolContext.getMapView();
            if (!map.mapTable.outOfBounds(x, y)) {
                int minX = Math.min(startX, x);
                int maxX = Math.max(startX, x);
                int minY = Math.min(startY, y);
                int maxY = Math.max(startY, y);
                RubyTable rt = new RubyTable(3, (maxX - minX) + 1, (maxY - minY) + 1, map.mapTable.planeCount, new int[map.mapTable.planeCount]);
                for (int l = 0; l < map.mapTable.planeCount; l++)
                    for (int i = minX; i <= maxX; i++)
                        for (int j = minY; j <= maxY; j++)
                            if (!map.mapTable.outOfBounds(i, j))
                                rt.setTiletype(i - minX, j - minY, l, map.mapTable.getTiletype(i, j, l));
                IRIOGeneric rb = new IRIOGeneric(StandardCharsets.UTF_8);
                rb.setUser("Table", rt.innerBytes);
                map.app.theClipboard = rb;
                mapToolContext.accept(null);
            }
        } else {
            startX = x;
            startY = y;
            innerLabel.text = T.m.tsFinishCopy;
            stage = true;
        }
    }

    @Override
    @NonNull
    public String viewState(int mouseXT, int mouseYT) {
        return "CopyRect." + subFrame() + "." + mouseXT + "." + mouseYT;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.dbs.PathSyntax;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 11/06/17.
 */
public class MapPositionHelperSchemaElement extends SchemaElement {
    public final PathSyntax pathA, pathB, pathC;

    public MapPositionHelperSchemaElement(PathSyntax a, PathSyntax b, PathSyntax c) {
        pathA = a;
        pathB = b;
        pathC = c;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        IRIO pathARIO = null;
        if (pathA != null)
            pathARIO = pathA.get(target);
        final IRIO[] abc = {
                pathARIO,
                pathB.get(target),
                pathC.get(target)
        };
        for (int i = (pathA == null ? 1 : 0); i < 3; i++)
            if (abc[i] == null)
                return new UILabel(TXDB.get("Position editor disabled."), FontSizes.schemaFieldTextHeight);
        String mapGUM = launcher.getContextGUM();
        if (abc[0] != null)
            mapGUM = launcher.getApp().system.mapReferentToGUM(abc[0]);
        if (mapGUM == null)
            return new UILabel(TXDB.get("Can't translate ID to map."), FontSizes.schemaFieldTextHeight);
        // The UIMapView constructor will automatically create missing maps. We don't want this.
        if (launcher.getApp().system.mapViewRequest(mapGUM, false) == null)
            return new UILabel(TXDB.get("No such map exists."), FontSizes.schemaFieldTextHeight);
        final long x = abc[1].getFX();
        final long y = abc[2].getFX();
        final UIMapView umv = new UIMapView(launcher.getApp(), mapGUM, 320, FontSizes.scaleGuess(192));
        umv.callbacks = new IMapViewCallbacks() {
            @Override
            public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
                return there;
            }

            @Override
            public int wantOverlay(boolean minimap) {
                return minimap ? 0 : 1;
            }

            @Override
            public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {
                mvdc.drawIndicator((int) x, (int) y, MapViewDrawContext.IndicatorStyle.Target);
            }

            @Override
            public void confirmAt(int x, int y, int pixx, int pixy, int layer, boolean first) {
                if (!first)
                    return;
                abc[1].setFX(x);
                abc[2].setFX(y);
                path.changeOccurred(false);
            }
        };
        umv.showTile((int) x, (int) y);
        return umv;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // Nothing can be done here.
    }
}

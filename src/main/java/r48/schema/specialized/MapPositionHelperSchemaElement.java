/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.IGrDriver;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.map.IMapViewCallbacks;
import r48.map.UIMapView;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

/**
 * Created on 11/06/17.
 */
public class MapPositionHelperSchemaElement extends SchemaElement {
    public final String pathA, pathB, pathC;

    public MapPositionHelperSchemaElement(String a, String b, String c) {
        pathA = a;
        pathB = b;
        pathC = c;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final RubyIO[] abc = {
                PathSyntax.parse(target, pathA),
                PathSyntax.parse(target, pathB),
                PathSyntax.parse(target, pathC)
        };
        for (int i = 0; i < 3; i++)
            if (abc[i] == null)
                return new UILabel("A component is missing.", FontSizes.schemaFieldTextHeight);
        String mapId = AppMain.system.mapReferentToId(abc[0]);
        if (mapId == null)
            return new UILabel("No such map exists.", FontSizes.schemaFieldTextHeight);
        final long x = abc[1].fixnumVal;
        final long y = abc[2].fixnumVal;
        final UIMapView umv = new UIMapView(mapId, 320, 192);
        umv.callbacks = new IMapViewCallbacks() {
            @Override
            public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
                return there;
            }

            @Override
            public int wantOverlay(boolean minimap) {
                return minimap ? 0 : 1;
            }

            @Override
            public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
                if (x == tx)
                    if (y == ty)
                        Art.drawTarget(px, py, umv.tileSize, igd);
            }

            @Override
            public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap) {

            }

            @Override
            public void confirmAt(int x, int y, int layer) {
                abc[1].fixnumVal = x;
                abc[2].fixnumVal = y;
                path.changeOccurred(false);
            }

            @Override
            public boolean shouldIgnoreDrag() {
                return true;
            }
        };
        umv.showTile((int) x, (int) y);
        return umv;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // Nothing can be done here.
    }
}

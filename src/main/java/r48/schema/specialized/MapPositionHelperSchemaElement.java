/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.IGrDriver;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.dbs.TXDB;
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
                return new UILabel(TXDB.get("A component is missing."), FontSizes.schemaFieldTextHeight);
        String mapGUM = AppMain.system.mapReferentToGUM(abc[0]);
        if (mapGUM == null)
            return new UILabel(TXDB.get("Can't translate ID to map."), FontSizes.schemaFieldTextHeight);
        // The UIMapView constructor will automatically create missing maps. We don't want this!
        if (AppMain.objectDB.getObject(mapGUM, null) == null)
            return new UILabel(TXDB.get("No such map exists."), FontSizes.schemaFieldTextHeight);
        final long x = abc[1].fixnumVal;
        final long y = abc[2].fixnumVal;
        final UIMapView umv = new UIMapView(mapGUM, 320, FontSizes.scaleGuess(192));
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
            public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

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

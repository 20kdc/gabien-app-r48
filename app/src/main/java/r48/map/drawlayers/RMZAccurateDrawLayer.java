/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.map.drawlayers;

import r48.App;
import r48.RubyTable;
import java.util.Collections;

/**
 * Copied from RXPAccurateDrawLayer on November 28th 2019.
 */
public class RMZAccurateDrawLayer extends ZSortingDrawLayer {
    public final RubyTable mapTable;
    public SignalMapViewLayer[] tileSignalLayers;

    public RMZAccurateDrawLayer(App app, RubyTable tbl, int layers) {
        super(app);
        mapTable = tbl;
        tileSignalLayers = new SignalMapViewLayer[layers];
        for (int i = 0; i < tileSignalLayers.length; i++) {
            tileSignalLayers[i] = new SignalMapViewLayer(T.z.l206.r(i));
            signals.add(tileSignalLayers[i]);
        }
        Collections.addAll(signals, tileSignalLayers);
    }
}

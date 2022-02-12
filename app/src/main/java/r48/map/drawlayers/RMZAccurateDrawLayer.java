/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.map.drawlayers;

import r48.RubyTable;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.io.data.IRIOFixnum;

import java.util.Collections;

/**
 * Copied from RXPAccurateDrawLayer on November 28th 2019.
 */
public class RMZAccurateDrawLayer extends ZSortingDrawLayer {
    public final RubyTable mapTable;
    public SignalMapViewLayer[] tileSignalLayers;

    public RMZAccurateDrawLayer(RubyTable tbl, int layers) {
        mapTable = tbl;
        tileSignalLayers = new SignalMapViewLayer[layers];
        for (int i = 0; i < tileSignalLayers.length; i++) {
            tileSignalLayers[i] = new SignalMapViewLayer(FormatSyntax.formatExtended(TXDB.get("Tile Layer #A"), new IRIOFixnum(i)));
            signals.add(tileSignalLayers[i]);
        }
        Collections.addAll(signals, tileSignalLayers);
    }
}

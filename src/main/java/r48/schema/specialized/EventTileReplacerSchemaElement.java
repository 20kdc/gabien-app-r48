/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.dbs.TSDB;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UITileGrid;

/**
 * Created on 12/06/17.
 */
public class EventTileReplacerSchemaElement extends SchemaElement {
    public final TSDB displayMap;
    public final int layer;
    public final String charName, charIdx;

    public EventTileReplacerSchemaElement(TSDB dmap, int l, String idx, String n) {
        displayMap = dmap;
        layer = l;
        charName = n;
        charIdx = idx;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final UITileGrid r = new UITileGrid(launcher.getContextRenderer(), layer, 0, displayMap.mapping.length, 0, displayMap.mapping, "This text can't be seen.");
        if (PathSyntax.parse(target, charName).strVal.length == 0)
            r.setSelected((int) PathSyntax.parse(target, charIdx).fixnumVal);
        r.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                PathSyntax.parse(target, charName).strVal = new byte[0];
                PathSyntax.parse(target, charIdx).fixnumVal = r.getSelected();
                path.changeOccurred(false);
            }
        };
        return r;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // Do nothing, shouldn't act as schema, just an embeddable editing component
    }
}

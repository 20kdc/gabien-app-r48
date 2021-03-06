/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.FontSizes;
import r48.dbs.PathSyntax;
import r48.dbs.TSDB;
import r48.io.data.IRIO;
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
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITileGrid r = new UITileGrid(launcher.getContextRenderer(), layer, displayMap.mapping, FontSizes.getSpriteScale());
        if (PathSyntax.parse(target, charName).decString().length() == 0)
            r.setSelected((int) PathSyntax.parse(target, charIdx).getFX());
        r.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                PathSyntax.parse(target, charName).setString("");
                PathSyntax.parse(target, charIdx).setFX(r.getSelected());
                path.changeOccurred(false);
                launcher.popObject();
            }
        };
        return r;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // Do nothing, shouldn't act as schema, just an embeddable editing component
    }
}

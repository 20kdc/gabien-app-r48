/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
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
public class EventTileReplacerSchemaElement extends SchemaElement.Leaf {
    public final TSDB displayMap;
    public final int layer;
    public final PathSyntax charName, charIdx;

    public EventTileReplacerSchemaElement(@NonNull TSDB dmap, int l, PathSyntax idx, PathSyntax n) {
        super(dmap.app);
        displayMap = dmap;
        layer = l;
        charName = n;
        charIdx = idx;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITileGrid r = new UITileGrid(app, launcher.getContextRenderer(), layer, displayMap.mapping, app.f.getSpriteScale());
        if (charName.get(target).decString().length() == 0)
            r.setSelected((int) charIdx.get(target).getFX());
        r.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                charName.get(target).setString("");
                charIdx.get(target).setFX(r.getSelected());
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

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.RubyIO;
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

    public EventTileReplacerSchemaElement(TSDB dmap, int l) {
        displayMap = dmap;
        layer = l;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final UITileGrid r = new UITileGrid(launcher.getContextRenderer(), layer, 0, displayMap.mapping.length, 0, displayMap.mapping);
        if (target.getInstVarBySymbol("@character_name").strVal.length == 0)
            r.setSelected((int) target.getInstVarBySymbol("@character_index").fixnumVal);
        r.onSelectionChange = new Runnable() {
            @Override
            public void run() {
                target.getInstVarBySymbol("@character_name").strVal = new byte[0];
                target.getInstVarBySymbol("@character_index").fixnumVal = r.getSelected();
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

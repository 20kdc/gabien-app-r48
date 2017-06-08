/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Installs a set of sensible defaults on command.
 * Created on 08/06/17.
 */
public class R2kSystemDefaultsInstallerSchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return new UIPanel();
    }

    @Override
    public int maxHoldingHeight() {
        return 0;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (setDefault) {
            // Target is RPG::Database.
            // Note that this relies on schema defaults for the most part,
            // it just puts some stuff that isn't so easily definable into place.
            // Tasks:
            // 1. Install a basic Actor
            RubyIO sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Actor"), new RubyIO().setFX(1));
            target.getInstVarBySymbol("@actors").hashVal.put(new RubyIO().setFX(1), sub);
            // 2. Install a tileset
            sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Tileset"), new RubyIO().setFX(1));
            target.getInstVarBySymbol("@tilesets").hashVal.put(new RubyIO().setFX(1), sub);
            // finally, signal
            path.changeOccurred(true);
        }
    }
}

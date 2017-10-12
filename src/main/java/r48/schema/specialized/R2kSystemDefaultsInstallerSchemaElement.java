/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.RubyTable;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Installs a set of sensible defaults on command.
 * Created on 08/06/17.
 */
public class R2kSystemDefaultsInstallerSchemaElement extends SchemaElement {
    public int mode = 0;

    public R2kSystemDefaultsInstallerSchemaElement(int i) {
        mode = i;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        UIPanel panel = new UIPanel();
        panel.setBounds(new Rect(0, 0, 0, 0));
        return panel;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (setDefault) {
            // Target is RPG::Database.
            // Note that this relies on schema defaults for the most part,
            // it just puts some stuff that isn't so easily definable into place.
            // Tasks:
            RubyIO sub = null;
            switch (mode) {
                case 0:
                    // 1. Install a basic Actor
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Actor"), new RubyIO().setFX(1));
                    target.getInstVarBySymbol("@actors").hashVal.put(new RubyIO().setFX(1), sub);
                    target.getInstVarBySymbol("@system").getInstVarBySymbol("@party").arrVal = new RubyIO[] {
                            new RubyIO().setFX(1)
                    };
                    // 2. Install a tileset
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Tileset"), new RubyIO().setFX(1));
                    target.getInstVarBySymbol("@tilesets").hashVal.put(new RubyIO().setFX(1), sub);
                    // 3. Setup Terrain
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Terrain"), new RubyIO().setFX(1));
                    target.getInstVarBySymbol("@terrains").hashVal.put(new RubyIO().setFX(1), sub);
                    // 4. Battle System initialization
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Animation"), new RubyIO().setFX(1));
                    sub.getInstVarBySymbol("@name").setString("Default Fallback Animation");
                    target.getInstVarBySymbol("@animations").hashVal.put(new RubyIO().setFX(1), sub);

                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::State"), new RubyIO().setFX(1));
                    // These are the minimum settings for death to work correctly.
                    sub.getInstVarBySymbol("@name").setString("Death");
                    sub.getInstVarBySymbol("@restriction").fixnumVal = 1;
                    target.getInstVarBySymbol("@states").hashVal.put(new RubyIO().setFX(1), sub);

                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::BattlerAnimationSet"), new RubyIO().setFX(1));
                    sub.getInstVarBySymbol("@name").setString("Default Fallback AnimSet");
                    target.getInstVarBySymbol("@battle_anim_sets_2k3").hashVal.put(new RubyIO().setFX(1), sub);
                    // Prepare.
                    AppMain.pendingRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            AppMain.r2kProjectCreationHelperFunction();
                        }
                    });
                    break;
                case 1:
                    // 1. Fix root
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::MapInfo"), new RubyIO().setFX(0));
                    sub.getInstVarBySymbol("@name").setString("Root");
                    sub.getInstVarBySymbol("@parent_id").fixnumVal = 0;
                    sub.getInstVarBySymbol("@type").fixnumVal = 0;
                    target.getInstVarBySymbol("@map_infos").hashVal.put(new RubyIO().setFX(0), sub);
                    // 2. Create basic map entry
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::MapInfo"), new RubyIO().setFX(1));
                    sub.getInstVarBySymbol("@name").setString("First Map");
                    sub.getInstVarBySymbol("@parent_id").fixnumVal = 0;
                    sub.getInstVarBySymbol("@type").fixnumVal = 1;
                    target.getInstVarBySymbol("@map_infos").hashVal.put(new RubyIO().setFX(1), sub);
                    // 3. Setup order
                    target.getInstVarBySymbol("@map_order").arrVal = new RubyIO[] {
                            new RubyIO().setFX(0),
                            new RubyIO().setFX(1),
                    };
                    // 4. Setup start
                    target.getInstVarBySymbol("@start").getInstVarBySymbol("@player_map").fixnumVal = 1;
                    break;
                case 2:
                    // Nobody expects tilesets to act the way they do on defaults, FIX IT.
                    // I was informed to set upper to false by default, and though I have done that for most tiles,
                    //  my having to do this is a natural consequence.
                    RubyTable rt = new RubyTable(target.getInstVarBySymbol("@highpass_data").userVal);
                    rt.setTiletype(0, 0, 0, (short) 0x1F);
                    break;
            }
            // finally, signal
            path.changeOccurred(true);
        }
    }
}

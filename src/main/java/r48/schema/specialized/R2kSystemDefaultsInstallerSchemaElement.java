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

import java.util.HashMap;

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
                    sub.getInstVarBySymbol("@name").setString("Default Fallback Animation", false);
                    target.getInstVarBySymbol("@animations").hashVal.put(new RubyIO().setFX(1), sub);

                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::State"), new RubyIO().setFX(1));
                    // These are the minimum settings for death to work correctly.
                    sub.getInstVarBySymbol("@name").setString("Death", false);
                    sub.getInstVarBySymbol("@restriction").fixnumVal = 1;
                    target.getInstVarBySymbol("@states").hashVal.put(new RubyIO().setFX(1), sub);

                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::BattlerAnimationSet"), new RubyIO().setFX(1));
                    sub.getInstVarBySymbol("@name").setString("Default Fallback AnimSet", false);
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
                    sub.getInstVarBySymbol("@name").setString("Root", false);
                    sub.getInstVarBySymbol("@parent_id").fixnumVal = 0;
                    sub.getInstVarBySymbol("@type").fixnumVal = 0;
                    target.getInstVarBySymbol("@map_infos").hashVal.put(new RubyIO().setFX(0), sub);
                    // 2. Create basic map entry
                    sub = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::MapInfo"), new RubyIO().setFX(1));
                    sub.getInstVarBySymbol("@name").setString("First Map", false);
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
                case 3:
                    // Savefile
                    saveFileSetup(target);
                    break;
                case 4:
                    // map_id saner default setter, but only for savefiles.
                    mapIdMagic(target, path.findRoot());
                    break;
            }
            // finally, signal
            path.changeOccurred(true);
        }
    }

    // sets target to the relevant map ID based on vague information
    private void mapIdMagic(RubyIO target, SchemaPath root) {
        String str = AppMain.objectDB.getIdByObject(root.targetElement);
        if (str == null)
            return;
        if (str.startsWith("Map"))
            if (str.endsWith(".lmu")) {
                try {
                    target.fixnumVal = Integer.parseInt(str.substring(3, str.length() - 4));
                } catch (Exception e) {
                    // nope
                }
            }
        if (str.startsWith("Save"))
            if (str.endsWith(".lsd"))
                target.setDeepClone(root.targetElement.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map"));
    }

    private void saveFileSetup(RubyIO target) {
        setupSaveCharacter(target.getInstVarBySymbol("@party_pos"), "@player_map", "@player_x", "@player_y");
        setupSaveCharacter(target.getInstVarBySymbol("@boat_pos"), "@boat_map", "@boat_x", "@boat_y");
        setupSaveCharacter(target.getInstVarBySymbol("@ship_pos"), "@ship_map", "@ship_x", "@ship_y");
        setupSaveCharacter(target.getInstVarBySymbol("@airship_pos"), "@airship_map", "@airship_x", "@airship_y");
        // copy over stuff
        RubyIO savSys = target.getInstVarBySymbol("@system");
        RubyIO ldb = AppMain.objectDB.getObject("RPG_RT.ldb");
        RubyIO ldbSys = ldb.getInstVarBySymbol("@system");

        target.getInstVarBySymbol("@party").getInstVarBySymbol("@party").setDeepClone(ldbSys.getInstVarBySymbol("@party"));
        // Unnecessary
        //savSys.getInstVarBySymbol("@system_name").setDeepClone(ldbSys.getInstVarBySymbol("@system_name"));
        savSys.getInstVarBySymbol("@system_box_tiling").setDeepClone(ldbSys.getInstVarBySymbol("@system_box_tiling"));
        savSys.getInstVarBySymbol("@font_id").setDeepClone(ldbSys.getInstVarBySymbol("@font_id"));

        initializeArrayWithClones(savSys.getInstVarBySymbol("@switches"), ldb.getInstVarBySymbol("@switches").hashVal, new RubyIO().setBool(false));
        initializeArrayWithClones(savSys.getInstVarBySymbol("@variables"), ldb.getInstVarBySymbol("@variables").hashVal, new RubyIO().setFX(0));

        for (String iv : savSys.iVarKeys)
            if (iv.endsWith("_se") || iv.endsWith("_music") || iv.endsWith("_fadein") || iv.endsWith("_fadeout"))
                savSys.getInstVarBySymbol(iv).setDeepClone(ldbSys.getInstVarBySymbol(iv));

        // table init!
        initTable(target.getInstVarBySymbol("@map_info").getInstVarBySymbol("@lower_tile_remap"));
        initTable(target.getInstVarBySymbol("@map_info").getInstVarBySymbol("@upper_tile_remap"));
    }

    private void initTable(RubyIO instVarBySymbol) {
        RubyTable rt = new RubyTable(instVarBySymbol.userVal);
        for (int i = 0; i < 0x90; i++)
            rt.setTiletype(i, 0, 0, (short) i);
    }

    private void initializeArrayWithClones(RubyIO instVarBySymbol, HashMap<RubyIO, RubyIO> length, RubyIO rubyIO) {
        int maxVal = 0;
        for (RubyIO rio : length.keySet())
            maxVal = Math.max((int) rio.fixnumVal, maxVal);
        instVarBySymbol.arrVal = new RubyIO[maxVal];
        for (int i = 0; i < instVarBySymbol.arrVal.length; i++)
            instVarBySymbol.arrVal[i] = new RubyIO().setDeepClone(rubyIO);
    }

    private void setupSaveCharacter(RubyIO chr, String s, String s1, String s2) {
        RubyIO lmt = AppMain.objectDB.getObject("RPG_RT.lmt").getInstVarBySymbol("@start");
        RubyIO a = lmt.getInstVarBySymbol(s);
        RubyIO b = lmt.getInstVarBySymbol(s1);
        RubyIO c = lmt.getInstVarBySymbol(s2);
        if (a != null)
            chr.getInstVarBySymbol("@map").setDeepClone(a);
        if (b != null)
            chr.getInstVarBySymbol("@x").setDeepClone(b);
        if (c != null)
            chr.getInstVarBySymbol("@y").setDeepClone(c);
    }
}

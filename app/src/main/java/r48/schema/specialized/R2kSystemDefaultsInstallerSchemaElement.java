/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.RubyTable;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.map.events.R2kSavefileEventAccess;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Installs a set of sensible defaults on command.
 * NOTE: As of IRIOs this does have a slight bit of weirdness ; it assumes setArray for a few elements leaves the array empty, which may not always be the case.
 * Created on 08/06/17.
 */
public class R2kSystemDefaultsInstallerSchemaElement extends SchemaElement.Leaf {
    public int mode = 0;

    public R2kSystemDefaultsInstallerSchemaElement(App app, int i) {
        super(app);
        mode = i;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
        if (mode == 3) {
            UITextButton utb1 = new UITextButton(T.s.svDoReset, app.f.schemaFieldTH, () -> {
                // Before doing anything stupid...
                long mapId = target.getIVar("@party_pos").getIVar("@map").getFX();
                String mapName = R2kRMLikeMapInfoBackend.sNameFromInt((int) mapId);
                IObjectBackend.ILoadedObject map = app.odb.getObject(mapName, null);
                if (map == null) {
                    app.ui.launchDialog(T.s.errInvalidMap);
                    return;
                }
                IRIO saveEvs = target.getIVar("@map_info").getIVar("@events");
                saveEvs.setHash();
                // Ghosts, become real!
                IRIO hmr = map.getObject().getIVar("@events");
                for (DMKey evs : hmr.getHashKeys())
                    R2kSavefileEventAccess.eventAsSaveEvent(app, saveEvs, mapId, evs, hmr.getHashVal(evs));
                // @system save_count is in-game save count, not actual System @save_count
                target.getIVar("@party_pos").getIVar("@map_save_count").setDeepClone(getSaveCount(map.getObject()));

                IRIO ldbSys = app.odb.getObject("RPG_RT.ldb").getObject().getIVar("@system");
                RORIO saveCount = getSaveCount(ldbSys);

                target.getIVar("@party_pos").getIVar("@db_save_count").setDeepClone(saveCount);
                initTable(target.getIVar("@map_info").getIVar("@lower_tile_remap"));
                initTable(target.getIVar("@map_info").getIVar("@upper_tile_remap"));

                path.changeOccurred(false);
                app.ui.launchDialog(T.s.svDidTheReset);
            });
            UITextButton utb2 = new UITextButton(T.s.svCauseReset, app.f.schemaFieldTH, () -> {
                IRIO saveEvs = target.getIVar("@map_info").getIVar("@events");
                saveEvs.setHash();
                target.getIVar("@party_pos").getIVar("@map_save_count").setFX(0);
                initTable(target.getIVar("@map_info").getIVar("@lower_tile_remap"));
                initTable(target.getIVar("@map_info").getIVar("@upper_tile_remap"));

                path.changeOccurred(false);
                app.ui.launchDialog(T.s.svCausedTheReset);
            });
            return new UISplitterLayout(utb1, utb2, true, 0.5d);
        } else {
            return new UIEmpty();
        }
    }

    public static RORIO getSaveCount(IRIO ldbSys) {
        RORIO saveCount = ldbSys.getIVar("@save_count_2k3en");
        if (saveCount == null)
            saveCount = ldbSys.getIVar("@save_count_other");
        if (saveCount == null)
            saveCount = DMKey.of(0);
        return saveCount;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (setDefault) {
            // Target is RPG::Database.
            // Note that this relies on schema defaults for the most part,
            // it just puts some stuff that isn't so easily definable into place.
            // Tasks:
            IRIO sub;
            switch (mode) {
                case 0:
                    // 1. Install a basic Actor
                    sub = target.getIVar("@actors").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::Actor"), DMKey.of(1));
                    sub.getIVar("@face_name").setString("faceset");
                    target.getIVar("@system").getIVar("@party").setArray().addAElem(0).setFX(1);
                    // 2. Install a tileset
                    SchemaPath.setDefaultValue(target.getIVar("@tilesets").addHashVal(DMKey.of(1)), app.sdb.getSDBEntry("RPG::Tileset"), DMKey.of(1));
                    // 3. Setup Terrain
                    SchemaPath.setDefaultValue(target.getIVar("@terrains").addHashVal(DMKey.of(1)), app.sdb.getSDBEntry("RPG::Terrain"), DMKey.of(1));
                    // 4. Battle System initialization
                    sub = target.getIVar("@animations").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::Animation"), DMKey.of(1));
                    sub.getIVar("@name").setString(T.s.r2kinit_anim);

                    sub = target.getIVar("@states").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::State"), DMKey.of(1));
                    // These are the minimum settings for death to work correctly.
                    sub.getIVar("@name").setString(T.s.r2kinit_death);
                    sub.getIVar("@restriction").setFX(1);

                    sub = target.getIVar("@battle_anim_sets_2k3").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::BattlerAnimationSet"), DMKey.of(1));
                    sub.getIVar("@name").setString(T.s.r2kinit_animSet);

                    // 5. Default enemy data
                    sub = target.getIVar("@enemies").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::Enemy"), DMKey.of(1));

                    sub = target.getIVar("@troops").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::Troop"), DMKey.of(1));
                    sub.getIVar("@name").setString(T.s.r2kinit_slime);

                    sub = sub.getIVar("@members");
                    sub.addAElem(0).setNull();
                    SchemaPath.setDefaultValue(sub.addAElem(1), app.sdb.getSDBEntry("RPG::Troop::Member"), DMKey.of(1));

                    // Prepare.
                    // This needs to be a bit indirect since app.np might not have inited yet
                    app.uiPendingRunnables.add(() -> app.np.r2kProjectCreationHelperFunction());
                    break;
                case 1:
                    // 1. Fix root
                    sub = target.getIVar("@map_infos").addHashVal(DMKey.of(0));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::MapInfo"), DMKey.of(0));
                    sub.getIVar("@name").setString("Root");
                    sub.getIVar("@parent_id").setFX(0);
                    sub.getIVar("@indent").setFX(0);
                    sub.getIVar("@type").setFX(0);

                    // 2. Create basic map entry
                    sub = target.getIVar("@map_infos").addHashVal(DMKey.of(1));
                    SchemaPath.setDefaultValue(sub, app.sdb.getSDBEntry("RPG::MapInfo"), DMKey.of(1));
                    sub.getIVar("@name").setString("First Map");
                    sub.getIVar("@parent_id").setFX(0);
                    sub.getIVar("@type").setFX(1);

                    // 3. Setup order
                    sub = target.getIVar("@map_order").setArray();
                    sub.addAElem(0).setFX(0);
                    sub.addAElem(1).setFX(1);
                    // 4. Setup start
                    target.getIVar("@start").getIVar("@player_map").setFX(1);
                    break;
                case 2:
                    // Nobody expects tilesets to act the way they do on defaults, FIX IT.
                    // I was informed to set upper to false by default, and though I have done that for most tiles,
                    //  my having to do this is a natural consequence.
                    RubyTable rt = new RubyTable(target.getIVar("@highpass_data").editUser());
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
    private void mapIdMagic(IRIO target, SchemaPath root) {
        String str = app.odb.getIdByObject(root.root);
        if (str == null)
            return;
        if (str.startsWith("Map"))
            if (str.endsWith(".lmu")) {
                try {
                    target.setFX(Integer.parseInt(str.substring(3, str.length() - 4)));
                } catch (Exception e) {
                    // nope
                }
            }
        if (str.startsWith("Save"))
            if (str.endsWith(".lsd"))
                target.setDeepClone(root.targetElement.getIVar("@party_pos").getIVar("@map"));
    }

    private void saveFileSetup(IRIO target) {
        setupSaveCharacter(target.getIVar("@party_pos"), "@player_map", "@player_x", "@player_y");
        setupSaveCharacter(target.getIVar("@boat_pos"), "@boat_map", "@boat_x", "@boat_y");
        setupSaveCharacter(target.getIVar("@ship_pos"), "@ship_map", "@ship_x", "@ship_y");
        setupSaveCharacter(target.getIVar("@airship_pos"), "@airship_map", "@airship_x", "@airship_y");
        // copy over stuff
        IRIO savSys = target.getIVar("@system");
        IRIO ldb = app.odb.getObject("RPG_RT.ldb").getObject();
        IRIO ldbSys = ldb.getIVar("@system");

        // Copy over stuff that isn't optional (hmm. Should it be optional?)
        target.getIVar("@party").getIVar("@party").setDeepClone(ldbSys.getIVar("@party"));
        savSys.getIVar("@font_id").setDeepClone(ldbSys.getIVar("@font_id"));

        initializeArrayWithClones(savSys.getIVar("@switches"), ldb.getIVar("@switches"), DMKey.FALSE);
        initializeArrayWithClones(savSys.getIVar("@variables"), ldb.getIVar("@variables"), DMKey.of(0));

        for (String iv : savSys.getIVars())
            if (iv.endsWith("_se") || iv.endsWith("_music") || iv.endsWith("_fadein") || iv.endsWith("_fadeout"))
                savSys.getIVar(iv).setDeepClone(ldbSys.getIVar(iv));

        // table init!
        initTable(target.getIVar("@map_info").getIVar("@lower_tile_remap"));
        initTable(target.getIVar("@map_info").getIVar("@upper_tile_remap"));
    }

    private void initTable(IRIO instVarBySymbol) {
        RubyTable rt = new RubyTable(instVarBySymbol.editUser());
        for (int i = 0; i < 0x90; i++)
            rt.setTiletype(i, 0, 0, (short) i);
    }

    private void initializeArrayWithClones(IRIO instVarBySymbol, IRIO length, RORIO rubyIO) {
        int maxVal = 0;
        for (DMKey rio : length.getHashKeys())
            maxVal = Math.max((int) rio.getFX(), maxVal);
        for (int i = 0; i < maxVal; i++)
            instVarBySymbol.addAElem(i).setDeepClone(rubyIO);
    }

    private void setupSaveCharacter(IRIO chr, String s, String s1, String s2) {
        IRIO lmt = app.odb.getObject("RPG_RT.lmt").getObject().getIVar("@start");
        IRIO a = lmt.getIVar(s);
        IRIO b = lmt.getIVar(s1);
        IRIO c = lmt.getIVar(s2);
        if (a != null)
            chr.getIVar("@map").setDeepClone(a);
        if (b != null)
            chr.getIVar("@x").setDeepClone(b);
        if (c != null)
            chr.getIVar("@y").setDeepClone(c);
    }

    public static void upgradeDatabase(IRIO root) {
        // WARNING! This attempts to upgrade a project to 2003 from 2000 while causing as little damage as possible,
        //  but do consider that it might not actually *work*.
        IRIO system = root.getIVar("@system");
        system.getIVar("@ldb_id").setFX(2003);
        if (system.getIVar("@save_count_2k3en") == null)
            system.addIVar("@save_count_2k3en").setFX(0);
        if (system.getIVar("@menu_commands_2k3") == null) {
            IRIO mc23 = system.addIVar("@menu_commands_2k3");
            mc23.setArray(5);
            mc23.getAElem(0).setFX(5);
            mc23.getAElem(1).setFX(1);
            mc23.getAElem(2).setFX(2);
            mc23.getAElem(3).setFX(3);
            mc23.getAElem(4).setFX(4);
        }
    }
}

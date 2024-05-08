/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.ShortR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.Music;
import r48.io.r2k.obj.Sound;

/**
 * COPY jun6-2017
 */
public class LdbSystem extends DM2R2kObject {
    @DMFXOBinding("@ldb_id") @DM2LcfBinding(0x0A) @DMCXInteger(0)
    public IntegerR2kStruct ldbId;
    @DMFXOBinding("@boat_name") @DM2LcfBinding(0x0B) @DMCXObject
    public StringR2kStruct boatName;
    @DMFXOBinding("@ship_name") @DM2LcfBinding(0x0C) @DMCXObject
    public StringR2kStruct shipName;
    @DMFXOBinding("@airship_name") @DM2LcfBinding(0x0D) @DMCXObject
    public StringR2kStruct airshipName;
    @DMFXOBinding("@boat_index") @DM2LcfBinding(0x0E) @DMCXInteger(0)
    public IntegerR2kStruct boatIndex;
    @DMFXOBinding("@ship_index") @DM2LcfBinding(0x0F) @DMCXInteger(0)
    public IntegerR2kStruct shipIndex;
    @DMFXOBinding("@airship_index") @DM2LcfBinding(0x10) @DMCXInteger(0)
    public IntegerR2kStruct airshipIndex;
    @DMFXOBinding("@title_name") @DM2LcfBinding(0x11) @DMCXObject
    public StringR2kStruct titleName;
    @DMFXOBinding("@gameover_name") @DM2LcfBinding(0x12) @DMCXObject
    public StringR2kStruct gameoverName;
    @DMFXOBinding("@system_name") @DM2LcfBinding(0x13) @DMCXObject
    public StringR2kStruct systemName;
    @DMFXOBinding("@system2_name_2k3") @DM2LcfBinding(0x14) @DMCXObject
    public StringR2kStruct system2Name;

    @DMFXOBinding("@party") @DM2LcfSizeBinding(0x15) @DM2LcfBinding(0x16)
    public DM2Array<ShortR2kStruct> party;

    // The menuCommandsSize -> menuCommands link is broken here, so it's fixed in the constructor.
    // Really not sure about this, it fixes v0.8 for release I think but I want to have a better look at it later
    @DMOptional @DMFXOBinding("@menu_commands_2k3") @DM2LcfSizeBinding(0x1A) @DM2LcfBinding(0x1B)
    public DM2Array<ShortR2kStruct> menuCommands;

    @DMFXOBinding("@title_music") @DM2LcfBinding(0x1F) @DMCXObject
    public Music titleMusic;
    @DMFXOBinding("@battle_music") @DM2LcfBinding(0x20) @DMCXObject
    public Music battleMusic;
    @DMFXOBinding("@battle_end_music") @DM2LcfBinding(0x21) @DMCXObject
    public Music battleEndMusic;
    @DMFXOBinding("@inn_music") @DM2LcfBinding(0x22) @DMCXObject
    public Music innMusic;
    @DMFXOBinding("@boat_music") @DM2LcfBinding(0x23) @DMCXObject
    public Music boatMusic;
    @DMFXOBinding("@ship_music") @DM2LcfBinding(0x24) @DMCXObject
    public Music shipMusic;
    @DMFXOBinding("@airship_music") @DM2LcfBinding(0x25) @DMCXObject
    public Music airshipMusic;
    @DMFXOBinding("@gameover_music") @DM2LcfBinding(0x26) @DMCXObject
    public Music gameoverMusic;

    @DMFXOBinding("@cursor_se") @DM2LcfBinding(0x29) @DMCXObject
    public Sound cursorSe;
    @DMFXOBinding("@decision_se") @DM2LcfBinding(0x2A) @DMCXObject
    public Sound decisionSe;
    @DMFXOBinding("@cancel_se") @DM2LcfBinding(0x2B) @DMCXObject
    public Sound cancelSe;
    @DMFXOBinding("@buzzer_se") @DM2LcfBinding(0x2C) @DMCXObject
    public Sound buzzerSe;
    @DMFXOBinding("@battle_se") @DM2LcfBinding(0x2D) @DMCXObject
    public Sound battleSe;
    @DMFXOBinding("@escape_se") @DM2LcfBinding(0x2E) @DMCXObject
    public Sound escapeSe;
    @DMFXOBinding("@enemy_attack_se") @DM2LcfBinding(0x2F) @DMCXObject
    public Sound enemyAttackSe;
    @DMFXOBinding("@enemy_hurt_se") @DM2LcfBinding(0x30) @DMCXObject
    public Sound enemyHurtSe;
    @DMFXOBinding("@actor_hurt_se") @DM2LcfBinding(0x31) @DMCXObject
    public Sound actorHurtSe;
    @DMFXOBinding("@dodge_se") @DM2LcfBinding(0x32) @DMCXObject
    public Sound dodgeSe;
    @DMFXOBinding("@enemy_death_se") @DM2LcfBinding(0x33) @DMCXObject
    public Sound enemyDeathSe;
    @DMFXOBinding("@item_se") @DM2LcfBinding(0x34) @DMCXObject
    public Sound itemSe;

    @DMFXOBinding("@transition_fadeout") @DM2LcfBinding(0x3D) @DMCXInteger(0)
    public IntegerR2kStruct transitionOut;
    @DMFXOBinding("@transition_fadein") @DM2LcfBinding(0x3E) @DMCXInteger(0)
    public IntegerR2kStruct transitionIn;
    @DMFXOBinding("@battle_start_fadeout") @DM2LcfBinding(0x3F) @DMCXInteger(0)
    public IntegerR2kStruct battleStartFadeout;
    @DMFXOBinding("@battle_start_fadein") @DM2LcfBinding(0x40) @DMCXInteger(0)
    public IntegerR2kStruct battleStartFadein;
    @DMFXOBinding("@battle_end_fadeout") @DM2LcfBinding(0x41) @DMCXInteger(0)
    public IntegerR2kStruct battleEndFadeout;
    @DMFXOBinding("@battle_end_fadein") @DM2LcfBinding(0x42) @DMCXInteger(0)
    public IntegerR2kStruct battleEndFadein;

    @DMFXOBinding("@system_box_tiling") @DM2LcfBinding(0x47) @DMCXInteger(0)
    public IntegerR2kStruct messageStretch;
    @DMFXOBinding("@font_id") @DM2LcfBinding(0x48) @DMCXInteger(0)
    public IntegerR2kStruct fontId;
    @DMFXOBinding("@test_condition") @DM2LcfBinding(0x51) @DMCXInteger(0)
    public IntegerR2kStruct testCondition;
    @DMFXOBinding("@test_actor") @DM2LcfBinding(0x52) @DMCXInteger(0)
    public IntegerR2kStruct testActor;
    @DMFXOBinding("@test_battle_background") @DM2LcfBinding(0x54) @DMCXObject
    public StringR2kStruct battletestBackground;
    @DMFXOBinding("@test_battle_data") @DM2LcfBinding(0x55) @DMCXSupplier(TestBattler.class)
    public DM2SparseArrayA<TestBattler> battletestData;
    @DMOptional @DMFXOBinding("@save_count_2k3en") @DM2LcfBinding(0x5A) @DMCXInteger(0)
    public IntegerR2kStruct saveCountEn;
    @DMOptional @DMFXOBinding("@save_count_other") @DM2LcfBinding(0x5B) @DMCXInteger(0)
    public IntegerR2kStruct saveCountJp;
    @DMFXOBinding("@test_battle_terrain") @DM2LcfBinding(0x5E) @DMCXInteger(0)
    public IntegerR2kStruct battletestTerrain;
    @DMFXOBinding("@test_battle_formation") @DM2LcfBinding(0x5F) @DMCXInteger(0)
    public IntegerR2kStruct battletestFormation;
    @DMFXOBinding("@test_battle_condition") @DM2LcfBinding(0x60) @DMCXInteger(0)
    public IntegerR2kStruct battletestCondition;
    @DMFXOBinding("@item_allow_classbased_2k3") @DM2LcfBinding(0x61) @DMCXInteger(0)
    public IntegerR2kStruct itemAllowClassbased;
    @DMFXOBinding("@frame_show_2k3") @DM2LcfBinding(0x63) @DMCXBoolean(false)
    public BooleanR2kStruct frameShow;
    @DMFXOBinding("@frame_name_2k3") @DM2LcfBinding(0x64) @DMCXObject
    public StringR2kStruct frameName;
    @DMFXOBinding("@invert_animations_2k3") @DM2LcfBinding(0x65) @DMCXBoolean(false)
    public BooleanR2kStruct invanim;
    @DMFXOBinding("@show_title_2k3") @DM2LcfBinding(0x6F) @DMCXBoolean(true)
    public BooleanR2kStruct showTitle;

    public LdbSystem(DMContext ctx) {
        super(ctx, "RPG::System");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@party"))
            return party = new DM2Array<ShortR2kStruct>(context, 0, true, true, 1) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(context, 1);
                }
            };
        if (sym.equals("@menu_commands_2k3"))
            return menuCommands = new DM2Array<ShortR2kStruct>(context, 0, true, true) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(context, 0);
                }
            };
        return super.dm2AddIVar(sym);
    }

    public static class TestBattler extends DM2R2kObject {
        @DMFXOBinding("@actor") @DM2LcfBinding(0x01) @DMCXInteger(1)
        public IntegerR2kStruct actor;
        @DMFXOBinding("@level") @DM2LcfBinding(0x02) @DMCXInteger(1)
        public IntegerR2kStruct level;
        @DMFXOBinding("@equip_weapon") @DM2LcfBinding(0x0B) @DMCXInteger(0)
        public IntegerR2kStruct weaponId;
        @DMFXOBinding("@equip_shield") @DM2LcfBinding(0x0C) @DMCXInteger(0)
        public IntegerR2kStruct shieldId;
        @DMFXOBinding("@equip_armour") @DM2LcfBinding(0x0D) @DMCXInteger(0)
        public IntegerR2kStruct armourId;
        @DMFXOBinding("@equip_helmet") @DM2LcfBinding(0x0E) @DMCXInteger(0)
        public IntegerR2kStruct helmetId;
        @DMFXOBinding("@equip_accessory") @DM2LcfBinding(0x0F) @DMCXInteger(0)
        public IntegerR2kStruct accessoryId;

        public TestBattler(DMContext ctx) {
            super(ctx, "RPG::System::TestBattler");
        }
    }
}

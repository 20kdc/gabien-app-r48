/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.IRIO;
import r48.io.data.obj.DM2CXSupplier;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
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
    @DM2FXOBinding("@ldb_id") @DM2LcfBinding(0x0A) @DMCXInteger(0)
    public IntegerR2kStruct ldbId;
    @DM2FXOBinding("@boat_name") @DM2LcfBinding(0x0B) @DMCXObject
    public StringR2kStruct boatName;
    @DM2FXOBinding("@ship_name") @DM2LcfBinding(0x0C) @DMCXObject
    public StringR2kStruct shipName;
    @DM2FXOBinding("@airship_name") @DM2LcfBinding(0x0D) @DMCXObject
    public StringR2kStruct airshipName;
    @DM2FXOBinding("@boat_index") @DM2LcfBinding(0x0E) @DMCXInteger(0)
    public IntegerR2kStruct boatIndex;
    @DM2FXOBinding("@ship_index") @DM2LcfBinding(0x0F) @DMCXInteger(0)
    public IntegerR2kStruct shipIndex;
    @DM2FXOBinding("@airship_index") @DM2LcfBinding(0x10) @DMCXInteger(0)
    public IntegerR2kStruct airshipIndex;
    @DM2FXOBinding("@title_name") @DM2LcfBinding(0x11) @DMCXObject
    public StringR2kStruct titleName;
    @DM2FXOBinding("@gameover_name") @DM2LcfBinding(0x12) @DMCXObject
    public StringR2kStruct gameoverName;
    @DM2FXOBinding("@system_name") @DM2LcfBinding(0x13) @DMCXObject
    public StringR2kStruct systemName;
    @DM2FXOBinding("@system2_name_2k3") @DM2LcfBinding(0x14) @DMCXObject
    public StringR2kStruct system2Name;

    @DM2FXOBinding("@party") @DM2LcfSizeBinding(0x15) @DM2LcfBinding(0x16)
    public DM2Array<ShortR2kStruct> party;

    // The menuCommandsSize -> menuCommands link is broken here, so it's fixed in the constructor.
    // Really not sure about this, it fixes v0.8 for release I think but I want to have a better look at it later
    @DM2Optional @DM2FXOBinding("@menu_commands_2k3") @DM2LcfSizeBinding(0x1A) @DM2LcfBinding(0x1B)
    public DM2Array<ShortR2kStruct> menuCommands;

    @DM2FXOBinding("@title_music") @DM2LcfBinding(0x1F) @DMCXObject
    public Music titleMusic;
    @DM2FXOBinding("@battle_music") @DM2LcfBinding(0x20) @DMCXObject
    public Music battleMusic;
    @DM2FXOBinding("@battle_end_music") @DM2LcfBinding(0x21) @DMCXObject
    public Music battleEndMusic;
    @DM2FXOBinding("@inn_music") @DM2LcfBinding(0x22) @DMCXObject
    public Music innMusic;
    @DM2FXOBinding("@boat_music") @DM2LcfBinding(0x23) @DMCXObject
    public Music boatMusic;
    @DM2FXOBinding("@ship_music") @DM2LcfBinding(0x24) @DMCXObject
    public Music shipMusic;
    @DM2FXOBinding("@airship_music") @DM2LcfBinding(0x25) @DMCXObject
    public Music airshipMusic;
    @DM2FXOBinding("@gameover_music") @DM2LcfBinding(0x26) @DMCXObject
    public Music gameoverMusic;

    @DM2FXOBinding("@cursor_se") @DM2LcfBinding(0x29) @DMCXObject
    public Sound cursorSe;
    @DM2FXOBinding("@decision_se") @DM2LcfBinding(0x2A) @DMCXObject
    public Sound decisionSe;
    @DM2FXOBinding("@cancel_se") @DM2LcfBinding(0x2B) @DMCXObject
    public Sound cancelSe;
    @DM2FXOBinding("@buzzer_se") @DM2LcfBinding(0x2C) @DMCXObject
    public Sound buzzerSe;
    @DM2FXOBinding("@battle_se") @DM2LcfBinding(0x2D) @DMCXObject
    public Sound battleSe;
    @DM2FXOBinding("@escape_se") @DM2LcfBinding(0x2E) @DMCXObject
    public Sound escapeSe;
    @DM2FXOBinding("@enemy_attack_se") @DM2LcfBinding(0x2F) @DMCXObject
    public Sound enemyAttackSe;
    @DM2FXOBinding("@enemy_hurt_se") @DM2LcfBinding(0x30) @DMCXObject
    public Sound enemyHurtSe;
    @DM2FXOBinding("@actor_hurt_se") @DM2LcfBinding(0x31) @DMCXObject
    public Sound actorHurtSe;
    @DM2FXOBinding("@dodge_se") @DM2LcfBinding(0x32) @DMCXObject
    public Sound dodgeSe;
    @DM2FXOBinding("@enemy_death_se") @DM2LcfBinding(0x33) @DMCXObject
    public Sound enemyDeathSe;
    @DM2FXOBinding("@item_se") @DM2LcfBinding(0x34) @DMCXObject
    public Sound itemSe;

    @DM2FXOBinding("@transition_fadeout") @DM2LcfBinding(0x3D) @DMCXInteger(0)
    public IntegerR2kStruct transitionOut;
    @DM2FXOBinding("@transition_fadein") @DM2LcfBinding(0x3E) @DMCXInteger(0)
    public IntegerR2kStruct transitionIn;
    @DM2FXOBinding("@battle_start_fadeout") @DM2LcfBinding(0x3F) @DMCXInteger(0)
    public IntegerR2kStruct battleStartFadeout;
    @DM2FXOBinding("@battle_start_fadein") @DM2LcfBinding(0x40) @DMCXInteger(0)
    public IntegerR2kStruct battleStartFadein;
    @DM2FXOBinding("@battle_end_fadeout") @DM2LcfBinding(0x41) @DMCXInteger(0)
    public IntegerR2kStruct battleEndFadeout;
    @DM2FXOBinding("@battle_end_fadein") @DM2LcfBinding(0x42) @DMCXInteger(0)
    public IntegerR2kStruct battleEndFadein;

    @DM2FXOBinding("@system_box_tiling") @DM2LcfBinding(0x47) @DMCXInteger(0)
    public IntegerR2kStruct messageStretch;
    @DM2FXOBinding("@font_id") @DM2LcfBinding(0x48) @DMCXInteger(0)
    public IntegerR2kStruct fontId;
    @DM2FXOBinding("@test_condition") @DM2LcfBinding(0x51) @DMCXInteger(0)
    public IntegerR2kStruct testCondition;
    @DM2FXOBinding("@test_actor") @DM2LcfBinding(0x52) @DMCXInteger(0)
    public IntegerR2kStruct testActor;
    @DM2FXOBinding("@test_battle_background") @DM2LcfBinding(0x54) @DMCXObject
    public StringR2kStruct battletestBackground;
    @DM2FXOBinding("@test_battle_data") @DM2LcfBinding(0x55) @DM2CXSupplier(TestBattler.class)
    public DM2SparseArrayA<TestBattler> battletestData;
    @DM2Optional @DM2FXOBinding("@save_count_2k3en") @DM2LcfBinding(0x5A) @DMCXInteger(0)
    public IntegerR2kStruct saveCountEn;
    @DM2Optional @DM2FXOBinding("@save_count_other") @DM2LcfBinding(0x5B) @DMCXInteger(0)
    public IntegerR2kStruct saveCountJp;
    @DM2FXOBinding("@test_battle_terrain") @DM2LcfBinding(0x5E) @DMCXInteger(0)
    public IntegerR2kStruct battletestTerrain;
    @DM2FXOBinding("@test_battle_formation") @DM2LcfBinding(0x5F) @DMCXInteger(0)
    public IntegerR2kStruct battletestFormation;
    @DM2FXOBinding("@test_battle_condition") @DM2LcfBinding(0x60) @DMCXInteger(0)
    public IntegerR2kStruct battletestCondition;
    @DM2FXOBinding("@item_allow_classbased_2k3") @DM2LcfBinding(0x61) @DMCXInteger(0)
    public IntegerR2kStruct itemAllowClassbased;
    @DM2FXOBinding("@frame_show_2k3") @DM2LcfBinding(0x63) @DMCXBoolean(false)
    public BooleanR2kStruct frameShow;
    @DM2FXOBinding("@frame_name_2k3") @DM2LcfBinding(0x64) @DMCXObject
    public StringR2kStruct frameName;
    @DM2FXOBinding("@invert_animations_2k3") @DM2LcfBinding(0x65) @DMCXBoolean(false)
    public BooleanR2kStruct invanim;
    @DM2FXOBinding("@show_title_2k3") @DM2LcfBinding(0x6F) @DMCXBoolean(true)
    public BooleanR2kStruct showTitle;

    public LdbSystem(DM2Context ctx) {
        super(ctx, "RPG::System");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@party"))
            return party = new DM2Array<ShortR2kStruct>(0, true, true, 1) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(1);
                }
            };
        if (sym.equals("@menu_commands_2k3"))
            return menuCommands = new DM2Array<ShortR2kStruct>(0, true, true) {
                @Override
                public ShortR2kStruct newValue() {
                    return new ShortR2kStruct(0);
                }
            };
        return super.dm2AddIVar(sym);
    }

    public static class TestBattler extends DM2R2kObject {
        @DM2FXOBinding("@actor") @DM2LcfBinding(0x01) @DMCXInteger(1)
        public IntegerR2kStruct actor;
        @DM2FXOBinding("@level") @DM2LcfBinding(0x02) @DMCXInteger(1)
        public IntegerR2kStruct level;
        @DM2FXOBinding("@equip_weapon") @DM2LcfBinding(0x0B) @DMCXInteger(0)
        public IntegerR2kStruct weaponId;
        @DM2FXOBinding("@equip_shield") @DM2LcfBinding(0x0C) @DMCXInteger(0)
        public IntegerR2kStruct shieldId;
        @DM2FXOBinding("@equip_armour") @DM2LcfBinding(0x0D) @DMCXInteger(0)
        public IntegerR2kStruct armourId;
        @DM2FXOBinding("@equip_helmet") @DM2LcfBinding(0x0E) @DMCXInteger(0)
        public IntegerR2kStruct helmetId;
        @DM2FXOBinding("@equip_accessory") @DM2LcfBinding(0x0F) @DMCXInteger(0)
        public IntegerR2kStruct accessoryId;

        public TestBattler(DM2Context ctx) {
            super(ctx, "RPG::System::TestBattler");
        }
    }
}

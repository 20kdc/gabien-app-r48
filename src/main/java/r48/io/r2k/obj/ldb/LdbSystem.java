/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.data.DM2Optional;
import r48.io.data.IRIO;
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
    @DM2FXOBinding("@ldb_id") @DM2LcfBinding(0x0A) @DM2LcfInteger(0)
    public IntegerR2kStruct ldbId;
    @DM2FXOBinding("@boat_name") @DM2LcfBinding(0x0B) @DM2LcfObject
    public StringR2kStruct boatName;
    @DM2FXOBinding("@ship_name") @DM2LcfBinding(0x0C) @DM2LcfObject
    public StringR2kStruct shipName;
    @DM2FXOBinding("@airship_name") @DM2LcfBinding(0x0D) @DM2LcfObject
    public StringR2kStruct airshipName;
    @DM2FXOBinding("@boat_index") @DM2LcfBinding(0x0E) @DM2LcfInteger(0)
    public IntegerR2kStruct boatIndex;
    @DM2FXOBinding("@ship_index") @DM2LcfBinding(0x0F) @DM2LcfInteger(0)
    public IntegerR2kStruct shipIndex;
    @DM2FXOBinding("@airship_index") @DM2LcfBinding(0x10) @DM2LcfInteger(0)
    public IntegerR2kStruct airshipIndex;
    @DM2FXOBinding("@title_name") @DM2LcfBinding(0x11) @DM2LcfObject
    public StringR2kStruct titleName;
    @DM2FXOBinding("@gameover_name") @DM2LcfBinding(0x12) @DM2LcfObject
    public StringR2kStruct gameoverName;
    @DM2FXOBinding("@system_name") @DM2LcfBinding(0x13) @DM2LcfObject
    public StringR2kStruct systemName;
    @DM2FXOBinding("@system2_name_2k3") @DM2LcfBinding(0x14) @DM2LcfObject
    public StringR2kStruct system2Name;

    @DM2FXOBinding("@party") @DM2LcfSizeBinding(0x15) @DM2LcfBinding(0x16)
    public DM2Array<ShortR2kStruct> party;

    // The menuCommandsSize -> menuCommands link is broken here, so it's fixed in the constructor.
    // Really not sure about this, it fixes v0.8 for release I think but I want to have a better look at it later
    @DM2Optional @DM2FXOBinding("@menu_commands_2k3") @DM2LcfSizeBinding(0x1A) @DM2LcfBinding(0x1B)
    public DM2Array<ShortR2kStruct> menuCommands;

    @DM2FXOBinding("@title_music") @DM2LcfBinding(0x1F) @DM2LcfObject
    public Music titleMusic;
    @DM2FXOBinding("@battle_music") @DM2LcfBinding(0x20) @DM2LcfObject
    public Music battleMusic;
    @DM2FXOBinding("@battle_end_music") @DM2LcfBinding(0x21) @DM2LcfObject
    public Music battleEndMusic;
    @DM2FXOBinding("@inn_music") @DM2LcfBinding(0x22) @DM2LcfObject
    public Music innMusic;
    @DM2FXOBinding("@boat_music") @DM2LcfBinding(0x23) @DM2LcfObject
    public Music boatMusic;
    @DM2FXOBinding("@ship_music") @DM2LcfBinding(0x24) @DM2LcfObject
    public Music shipMusic;
    @DM2FXOBinding("@airship_music") @DM2LcfBinding(0x25) @DM2LcfObject
    public Music airshipMusic;
    @DM2FXOBinding("@gameover_music") @DM2LcfBinding(0x26) @DM2LcfObject
    public Music gameoverMusic;

    @DM2FXOBinding("@cursor_se") @DM2LcfBinding(0x29) @DM2LcfObject
    public Sound cursorSe;
    @DM2FXOBinding("@decision_se") @DM2LcfBinding(0x2A) @DM2LcfObject
    public Sound decisionSe;
    @DM2FXOBinding("@cancel_se") @DM2LcfBinding(0x2B) @DM2LcfObject
    public Sound cancelSe;
    @DM2FXOBinding("@buzzer_se") @DM2LcfBinding(0x2C) @DM2LcfObject
    public Sound buzzerSe;
    @DM2FXOBinding("@battle_se") @DM2LcfBinding(0x2D) @DM2LcfObject
    public Sound battleSe;
    @DM2FXOBinding("@escape_se") @DM2LcfBinding(0x2E) @DM2LcfObject
    public Sound escapeSe;
    @DM2FXOBinding("@enemy_attack_se") @DM2LcfBinding(0x2F) @DM2LcfObject
    public Sound enemyAttackSe;
    @DM2FXOBinding("@enemy_hurt_se") @DM2LcfBinding(0x30) @DM2LcfObject
    public Sound enemyHurtSe;
    @DM2FXOBinding("@actor_hurt_se") @DM2LcfBinding(0x31) @DM2LcfObject
    public Sound actorHurtSe;
    @DM2FXOBinding("@dodge_se") @DM2LcfBinding(0x32) @DM2LcfObject
    public Sound dodgeSe;
    @DM2FXOBinding("@enemy_death_se") @DM2LcfBinding(0x33) @DM2LcfObject
    public Sound enemyDeathSe;
    @DM2FXOBinding("@item_se") @DM2LcfBinding(0x34) @DM2LcfObject
    public Sound itemSe;

    @DM2FXOBinding("@transition_fadeout") @DM2LcfBinding(0x3D) @DM2LcfInteger(0)
    public IntegerR2kStruct transitionOut;
    @DM2FXOBinding("@transition_fadein") @DM2LcfBinding(0x3E) @DM2LcfInteger(0)
    public IntegerR2kStruct transitionIn;
    @DM2FXOBinding("@battle_start_fadeout") @DM2LcfBinding(0x3F) @DM2LcfInteger(0)
    public IntegerR2kStruct battleStartFadeout;
    @DM2FXOBinding("@battle_start_fadein") @DM2LcfBinding(0x40) @DM2LcfInteger(0)
    public IntegerR2kStruct battleStartFadein;
    @DM2FXOBinding("@battle_end_fadeout") @DM2LcfBinding(0x41) @DM2LcfInteger(0)
    public IntegerR2kStruct battleEndFadeout;
    @DM2FXOBinding("@battle_end_fadein") @DM2LcfBinding(0x42) @DM2LcfInteger(0)
    public IntegerR2kStruct battleEndFadein;

    @DM2FXOBinding("@system_box_tiling") @DM2LcfBinding(0x47) @DM2LcfInteger(0)
    public IntegerR2kStruct messageStretch;
    @DM2FXOBinding("@font_id") @DM2LcfBinding(0x48) @DM2LcfInteger(0)
    public IntegerR2kStruct fontId;
    @DM2FXOBinding("@test_condition") @DM2LcfBinding(0x51) @DM2LcfInteger(0)
    public IntegerR2kStruct testCondition;
    @DM2FXOBinding("@test_actor") @DM2LcfBinding(0x52) @DM2LcfInteger(0)
    public IntegerR2kStruct testActor;
    @DM2FXOBinding("@test_battle_background") @DM2LcfBinding(0x54) @DM2LcfObject
    public StringR2kStruct battletestBackground;
    @DM2FXOBinding("@test_battle_data") @DM2LcfBinding(0x55) @DM2LcfSparseArrayA(TestBattler.class)
    public DM2SparseArrayA<TestBattler> battletestData;
    @DM2Optional @DM2FXOBinding("@save_count_2k3en") @DM2LcfBinding(0x5A) @DM2LcfInteger(0)
    public IntegerR2kStruct saveCountEn;
    @DM2Optional @DM2FXOBinding("@save_count_other") @DM2LcfBinding(0x5B) @DM2LcfInteger(0)
    public IntegerR2kStruct saveCountJp;
    @DM2FXOBinding("@test_battle_terrain") @DM2LcfBinding(0x5E) @DM2LcfInteger(0)
    public IntegerR2kStruct battletestTerrain;
    @DM2FXOBinding("@test_battle_formation") @DM2LcfBinding(0x5F) @DM2LcfInteger(0)
    public IntegerR2kStruct battletestFormation;
    @DM2FXOBinding("@test_battle_condition") @DM2LcfBinding(0x60) @DM2LcfInteger(0)
    public IntegerR2kStruct battletestCondition;
    @DM2FXOBinding("@item_allow_classbased_2k3") @DM2LcfBinding(0x61) @DM2LcfInteger(0)
    public IntegerR2kStruct itemAllowClassbased;
    @DM2FXOBinding("@frame_show_2k3") @DM2LcfBinding(0x63) @DM2LcfBoolean(false)
    public BooleanR2kStruct frameShow;
    @DM2FXOBinding("@frame_name_2k3") @DM2LcfBinding(0x64) @DM2LcfObject
    public StringR2kStruct frameName;
    @DM2FXOBinding("@invert_animations_2k3") @DM2LcfBinding(0x65) @DM2LcfBoolean(false)
    public BooleanR2kStruct invanim;
    @DM2FXOBinding("@show_title_2k3") @DM2LcfBinding(0x6F) @DM2LcfBoolean(true)
    public BooleanR2kStruct showTitle;

    public LdbSystem() {
        super("RPG::System");
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
        @DM2FXOBinding("@actor") @DM2LcfBinding(0x01) @DM2LcfInteger(1)
        public IntegerR2kStruct actor;
        @DM2FXOBinding("@level") @DM2LcfBinding(0x02) @DM2LcfInteger(1)
        public IntegerR2kStruct level;
        @DM2FXOBinding("@equip_weapon") @DM2LcfBinding(0x0B) @DM2LcfInteger(0)
        public IntegerR2kStruct weaponId;
        @DM2FXOBinding("@equip_shield") @DM2LcfBinding(0x0C) @DM2LcfInteger(0)
        public IntegerR2kStruct shieldId;
        @DM2FXOBinding("@equip_armour") @DM2LcfBinding(0x0D) @DM2LcfInteger(0)
        public IntegerR2kStruct armourId;
        @DM2FXOBinding("@equip_helmet") @DM2LcfBinding(0x0E) @DM2LcfInteger(0)
        public IntegerR2kStruct helmetId;
        @DM2FXOBinding("@equip_accessory") @DM2LcfBinding(0x0F) @DM2LcfInteger(0)
        public IntegerR2kStruct accessoryId;

        public TestBattler() {
            super("RPG::System::TestBattler");
        }
    }
}

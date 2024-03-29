/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.IRIO;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.Music;
import r48.io.r2k.obj.Sound;

public class SaveSystem extends DM2R2kObject {
    @DM2FXOBinding("@screen") @DM2LcfBinding(0x01) @DMCXInteger(0)
    public IntegerR2kStruct screen;
    @DM2FXOBinding("@frame_count") @DM2LcfBinding(0x0B) @DMCXInteger(0)
    public IntegerR2kStruct frameCount;
    @DM2FXOBinding("@system_name") @DM2LcfBinding(0x15) @DMCXObject
    public StringR2kStruct systemName;
    @DM2Optional @DM2FXOBinding("@system_box_tiling") @DM2LcfBinding(0x16) @DMCXInteger(0)
    public IntegerR2kStruct systemBoxStretch;
    @DM2FXOBinding("@font_id") @DM2LcfBinding(0x17) @DMCXInteger(0)
    public IntegerR2kStruct fontId;
    @DM2FXOBinding("@switches") @DM2LcfSizeBinding(0x1F) @DM2LcfBinding(0x20)
    public DM2Array<BooleanR2kStruct> switches;
    @DM2FXOBinding("@variables") @DM2LcfSizeBinding(0x21) @DM2LcfBinding(0x22)
    public DM2Array<Int32R2kStruct> variables;
    @DM2FXOBinding("@message_transparent") @DM2LcfBinding(0x29) @DMCXInteger(0)
    public IntegerR2kStruct messageTransparent;
    @DM2FXOBinding("@message_position") @DM2LcfBinding(0x2A) @DMCXInteger(2)
    public IntegerR2kStruct messagePosition;
    @DM2FXOBinding("@message_prevent_overlap") @DM2LcfBinding(0x2B) @DMCXInteger(1)
    public IntegerR2kStruct messagePreventOverlap;
    @DM2FXOBinding("@message_continue_events") @DM2LcfBinding(0x2C) @DMCXInteger(0)
    public IntegerR2kStruct messageContinueEvents;

    @DM2FXOBinding("@face_name") @DM2LcfBinding(0x33) @DMCXObject
    public StringR2kStruct faceName;
    @DM2FXOBinding("@face_index") @DM2LcfBinding(0x34) @DMCXInteger(0)
    public IntegerR2kStruct faceIdx;
    @DM2FXOBinding("@face_right") @DM2LcfBinding(0x35) @DMCXBoolean(false)
    public BooleanR2kStruct faceRight;
    @DM2FXOBinding("@face_flip") @DM2LcfBinding(0x36) @DMCXBoolean(false)
    public BooleanR2kStruct faceFlip;
    @DM2FXOBinding("@transparent") @DM2LcfBinding(0x37) @DMCXBoolean(false)
    public BooleanR2kStruct transparent;

    @DM2FXOBinding("@title_music") @DM2LcfBinding(0x47) @DMCXObject
    public Music titleMusic;
    @DM2FXOBinding("@battle_music") @DM2LcfBinding(0x48) @DMCXObject
    public Music battleMusic;
    @DM2FXOBinding("@battle_end_music") @DM2LcfBinding(0x49) @DMCXObject
    public Music battleEndMusic;
    @DM2FXOBinding("@inn_music") @DM2LcfBinding(0x4A) @DMCXObject
    public Music innMusic;
    @DM2FXOBinding("@rtmusic_current") @DM2LcfBinding(0x4B) @DMCXObject
    public Music currentMusic;
    @DM2FXOBinding("@rtmusic_vehicle_start") @DM2LcfBinding(0x4C) @DMCXObject
    public Music beforeVehicleMusic;
    @DM2FXOBinding("@rtmusic_battle_start") @DM2LcfBinding(0x4D) @DMCXObject
    public Music beforeBattleMusic;
    @DM2FXOBinding("@rtmusic_memorized") @DM2LcfBinding(0x4E) @DMCXObject
    public Music storedMusic;
    @DM2FXOBinding("@boat_music") @DM2LcfBinding(0x4F) @DMCXObject
    public Music boatMusic;
    @DM2FXOBinding("@ship_music") @DM2LcfBinding(0x50) @DMCXObject
    public Music shipMusic;
    @DM2FXOBinding("@airship_music") @DM2LcfBinding(0x51) @DMCXObject
    public Music airshipMusic;
    @DM2FXOBinding("@gameover_music") @DM2LcfBinding(0x52) @DMCXObject
    public Music gameOverMusic;

    @DM2FXOBinding("@cursor_se") @DM2LcfBinding(0x5B) @DMCXObject
    public Sound cursorSound;
    @DM2FXOBinding("@decision_se") @DM2LcfBinding(0x5C) @DMCXObject
    public Sound decisionSound;
    @DM2FXOBinding("@cancel_se") @DM2LcfBinding(0x5D) @DMCXObject
    public Sound cancelSound;
    @DM2FXOBinding("@buzzer_se") @DM2LcfBinding(0x5E) @DMCXObject
    public Sound buzzerSound;
    @DM2FXOBinding("@battle_se") @DM2LcfBinding(0x5F) @DMCXObject
    public Sound battleSound;
    @DM2FXOBinding("@escape_se") @DM2LcfBinding(0x60) @DMCXObject
    public Sound escapeSound;
    @DM2FXOBinding("@enemy_attack_se") @DM2LcfBinding(0x61) @DMCXObject
    public Sound enemyAttackSound;
    @DM2FXOBinding("@enemy_hurt_se") @DM2LcfBinding(0x62) @DMCXObject
    public Sound enemyDamagedSound;
    @DM2FXOBinding("@actor_hurt_se") @DM2LcfBinding(0x63) @DMCXObject
    public Sound actorDamagedSound;
    @DM2FXOBinding("@dodge_se") @DM2LcfBinding(0x64) @DMCXObject
    public Sound dodgeSound;
    @DM2FXOBinding("@enemy_death_se") @DM2LcfBinding(0x65) @DMCXObject
    public Sound enemyDeathSound;
    @DM2FXOBinding("@item_se") @DM2LcfBinding(0x66) @DMCXObject
    public Sound itemSound;

    // Believe it or not, these are in fact Uint8.
    // How, though...
    // Later note: I should have clarified this inconsistency,
    //  but now I'll never know what it means. My guess is:
    // 1. there's an enum value that depends on this being signed.
    // 2. EasyRPG had it marked as Uint8 but fixed it, so these notes don't make sense (a good thing)
    @DM2FXOBinding("@transition_fadeout") @DM2LcfBinding(0x6F)
    public ByteR2kStruct transitionOut;
    @DM2FXOBinding("@transition_fadein") @DM2LcfBinding(0x70)
    public ByteR2kStruct transitionIn;
    @DM2FXOBinding("@battle_start_fadeout") @DM2LcfBinding(0x71)
    public ByteR2kStruct battleStartFadeout;
    @DM2FXOBinding("@battle_start_fadein") @DM2LcfBinding(0x72)
    public ByteR2kStruct battleStartFadein;
    @DM2FXOBinding("@battle_end_fadeout") @DM2LcfBinding(0x73)
    public ByteR2kStruct battleEndFadeout;
    @DM2FXOBinding("@battle_end_fadein") @DM2LcfBinding(0x74)
    public ByteR2kStruct battleEndFadein;

    @DM2FXOBinding("@can_teleport") @DM2LcfBinding(0x79) @DMCXBoolean(true)
    public BooleanR2kStruct canTeleport;
    @DM2FXOBinding("@can_escape") @DM2LcfBinding(0x7A) @DMCXBoolean(true)
    public BooleanR2kStruct canEscape;
    @DM2FXOBinding("@can_save") @DM2LcfBinding(0x7B) @DMCXBoolean(true)
    public BooleanR2kStruct canSave;
    @DM2FXOBinding("@can_menu") @DM2LcfBinding(0x7C) @DMCXBoolean(true)
    public BooleanR2kStruct canMenu;

    @DM2FXOBinding("@battle_background") @DM2LcfBinding(0x7D) @DMCXObject
    public StringR2kStruct battleBackground;
    @DM2FXOBinding("@save_count") @DM2LcfBinding(0x83) @DMCXInteger(0)
    public IntegerR2kStruct saveCount;
    @DM2FXOBinding("@save_slot") @DM2LcfBinding(0x84) @DMCXInteger(1)
    public IntegerR2kStruct saveSlot;
    @DM2FXOBinding("@atb_wait_mode_2k3") @DM2LcfBinding(0x8C) @DMCXInteger(0)
    public IntegerR2kStruct atbMode2k3;

    public SaveSystem(DM2Context ctx) {
        super(ctx, "RPG::SaveSystem");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@switches"))
            return switches = new DM2Array<BooleanR2kStruct>() {
                @Override
                public BooleanR2kStruct newValue() {
                    return new BooleanR2kStruct(false);
                }
            };
        if (sym.equals("@variables"))
            return variables = new DM2Array<Int32R2kStruct>() {
                @Override
                public Int32R2kStruct newValue() {
                    return new Int32R2kStruct(0);
                }
            };
        if (sym.equals("@transition_fadeout"))
            return transitionOut = newSByte();
        if (sym.equals("@transition_fadein"))
            return transitionIn = newSByte();
        if (sym.equals("@battle_start_fadeout"))
            return battleStartFadeout = newSByte();
        if (sym.equals("@battle_start_fadein"))
            return battleStartFadein = newSByte();
        if (sym.equals("@battle_end_fadeout"))
            return battleEndFadeout = newSByte();
        if (sym.equals("@battle_end_fadein"))
            return battleEndFadein = newSByte();
        return super.dm2AddIVar(sym);
    }

    private ByteR2kStruct newSByte() {
        return new ByteR2kStruct(1).signed();
    }

    // 3D is an unknown integer. I'd play with it, but there's never the time!
    // If there's a way to get a save at a given moment in time with RPG_RT, that'd be SUPER useful.
    // new Index(0x3D, musicFadeout, "@music_fadeout"),
}

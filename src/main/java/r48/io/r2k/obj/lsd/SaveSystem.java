/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.obj.Music;
import r48.io.r2k.obj.Sound;

public class SaveSystem extends R2kObject {
    public IntegerR2kStruct screen = new IntegerR2kStruct(0);
    public IntegerR2kStruct frameCount = new IntegerR2kStruct(0);
    public StringR2kStruct systemName = new StringR2kStruct();
    // This gets it's default from the database.
    public OptionalR2kStruct<IntegerR2kStruct> systemBoxStretch = new OptionalR2kStruct<IntegerR2kStruct>(new ISupplier<IntegerR2kStruct>() {
        @Override
        public IntegerR2kStruct get() {
            return new IntegerR2kStruct(0);
        }
    });
    public IntegerR2kStruct fontId = new IntegerR2kStruct(0);
    public ArraySizeR2kInterpretable<BooleanR2kStruct> switchesSize = new ArraySizeR2kInterpretable<BooleanR2kStruct>(true);
    public ArrayR2kStruct<BooleanR2kStruct> switches = new ArrayR2kStruct<BooleanR2kStruct>(switchesSize, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(false);
        }
    });
    public ArraySizeR2kInterpretable<Int32R2kStruct> variablesSize = new ArraySizeR2kInterpretable<Int32R2kStruct>(true);
    public ArrayR2kStruct<Int32R2kStruct> variables = new ArrayR2kStruct<Int32R2kStruct>(variablesSize, new ISupplier<Int32R2kStruct>() {
        @Override
        public Int32R2kStruct get() {
            return new Int32R2kStruct(0);
        }
    });
    public IntegerR2kStruct messageTransparent = new IntegerR2kStruct(0);
    public IntegerR2kStruct messagePosition = new IntegerR2kStruct(2);
    public IntegerR2kStruct messagePreventOverlap = new IntegerR2kStruct(1);
    public IntegerR2kStruct messageContinueEvents = new IntegerR2kStruct(0);

    public StringR2kStruct faceName = new StringR2kStruct();
    public IntegerR2kStruct faceIdx = new IntegerR2kStruct(0);
    public BooleanR2kStruct faceRight = new BooleanR2kStruct(false);
    public BooleanR2kStruct faceFlip = new BooleanR2kStruct(false);

    public BooleanR2kStruct transparent = new BooleanR2kStruct(false);

    public Music titleMusic = new Music();
    public Music battleMusic = new Music();
    public Music battleEndMusic = new Music();
    public Music innMusic = new Music();
    public Music currentMusic = new Music();
    public Music beforeVehicleMusic = new Music();
    public Music beforeBattleMusic = new Music();
    public Music storedMusic = new Music();
    public Music boatMusic = new Music();
    public Music shipMusic = new Music();
    public Music airshipMusic = new Music();
    public Music gameOverMusic = new Music();

    public Sound cursorSound = new Sound();
    public Sound decisionSound = new Sound();
    public Sound cancelSound = new Sound();
    public Sound buzzerSound = new Sound();
    public Sound battleSound = new Sound();
    public Sound escapeSound = new Sound();
    public Sound enemyAttackSound = new Sound();
    public Sound enemyDamagedSound = new Sound();
    public Sound actorDamagedSound = new Sound();
    public Sound dodgeSound = new Sound();
    public Sound enemyDeathSound = new Sound();
    public Sound itemSound = new Sound();

    // Believe it or not, these are in fact Uint8.
    // How, though...
    // Later note: I should have clarified this inconsistency,
    //  but now I'll never know what it means. My guess is:
    // 1. there's an enum value that depends on this being signed.
    // 2. EasyRPG had it marked as Uint8 but fixed it, so these notes don't make sense (a good thing)
    public ByteR2kStruct transitionOut = new ByteR2kStruct(1).signed();
    public ByteR2kStruct transitionIn = new ByteR2kStruct(1).signed();
    public ByteR2kStruct battleStartFadeout = new ByteR2kStruct(1).signed();
    public ByteR2kStruct battleStartFadein = new ByteR2kStruct(1).signed();
    public ByteR2kStruct battleEndFadeout = new ByteR2kStruct(1).signed();
    public ByteR2kStruct battleEndFadein = new ByteR2kStruct(1).signed();

    public BooleanR2kStruct canTeleport = new BooleanR2kStruct(true);
    public BooleanR2kStruct canEscape = new BooleanR2kStruct(true);
    public BooleanR2kStruct canSave = new BooleanR2kStruct(true);
    public BooleanR2kStruct canMenu = new BooleanR2kStruct(true);

    public StringR2kStruct battleBackground = new StringR2kStruct();
    public IntegerR2kStruct saveCount = new IntegerR2kStruct(0);
    public IntegerR2kStruct saveSlot = new IntegerR2kStruct(1);
    public IntegerR2kStruct atbMode2k3 = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, screen, "@screen"),
                new Index(0x0B, frameCount, "@frame_count"),
                new Index(0x15, systemName, "@system_name"),
                new Index(0x16, systemBoxStretch, "@system_box_tiling"),
                new Index(0x17, fontId, "@font_id"),
                new Index(0x1F, switchesSize),
                new Index(0x20, switches, "@switches"),
                new Index(0x21, variablesSize),
                new Index(0x22, variables, "@variables"),
                new Index(0x29, messageTransparent, "@message_transparent"),
                new Index(0x2A, messagePosition, "@message_position"),
                new Index(0x2B, messagePreventOverlap, "@message_prevent_overlap"),
                new Index(0x2C, messageContinueEvents, "@message_continue_events"),
                new Index(0x33, faceName, "@face_name"),
                new Index(0x34, faceIdx, "@face_index"),
                new Index(0x35, faceRight, "@face_right"),
                new Index(0x36, faceFlip, "@face_flip"),
                new Index(0x37, transparent, "@transparent"),
                // 3D is an unknown integer. I'd play with it, but there's never the time!
                // If there's a way to get a save at a given moment in time with RPG_RT, that'd be SUPER useful.
                // new Index(0x3D, musicFadeout, "@music_fadeout"),
                new Index(0x47, titleMusic, "@title_music"),
                new Index(0x48, battleMusic, "@battle_music"),
                new Index(0x49, battleEndMusic, "@battle_end_music"),
                new Index(0x4A, innMusic, "@inn_music"),
                new Index(0x4B, currentMusic, "@rtmusic_current"),
                new Index(0x4C, beforeVehicleMusic, "@rtmusic_vehicle_start"),
                new Index(0x4D, beforeBattleMusic, "@rtmusic_battle_start"),
                new Index(0x4E, storedMusic, "@rtmusic_memorized"),
                new Index(0x4F, boatMusic, "@boat_music"),
                new Index(0x50, shipMusic, "@ship_music"),
                new Index(0x51, airshipMusic, "@airship_music"),
                new Index(0x52, gameOverMusic, "@gameover_music"),

                new Index(0x5B, cursorSound, "@cursor_se"),
                new Index(0x5C, decisionSound, "@decision_se"),
                new Index(0x5D, cancelSound, "@cancel_se"),
                new Index(0x5E, buzzerSound, "@buzzer_se"),
                new Index(0x5F, battleSound, "@battle_se"),
                new Index(0x60, escapeSound, "@escape_se"),

                new Index(0x61, enemyAttackSound, "@enemy_attack_se"),
                new Index(0x62, enemyDamagedSound, "@enemy_hurt_se"),
                new Index(0x63, actorDamagedSound, "@actor_hurt_se"),
                new Index(0x64, dodgeSound, "@dodge_se"),
                new Index(0x65, enemyDeathSound, "@enemy_death_se"),
                new Index(0x66, itemSound, "@item_se"),

                new Index(0x6F, transitionOut, "@transition_fadeout"),
                new Index(0x70, transitionIn, "@transition_fadein"),
                new Index(0x71, battleStartFadeout, "@battle_start_fadeout"),
                new Index(0x72, battleStartFadein, "@battle_start_fadein"),
                new Index(0x73, battleEndFadeout, "@battle_end_fadeout"),
                new Index(0x74, battleEndFadein, "@battle_end_fadein"),

                new Index(0x79, canTeleport, "@can_teleport"),
                new Index(0x7A, canEscape, "@can_escape"),
                new Index(0x7B, canSave, "@can_save"),
                new Index(0x7C, canMenu, "@can_menu"),

                new Index(0x7D, battleBackground, "@battle_background"),
                new Index(0x83, saveCount, "@save_count"),
                new Index(0x84, saveSlot, "@save_slot"),
                new Index(0x8C, atbMode2k3, "@atb_wait_mode_2k3"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveSystem", true);
        asRIOISF(rio);
        return rio;
    }
}

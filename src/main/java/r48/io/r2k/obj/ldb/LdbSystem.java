/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;
import r48.io.r2k.obj.Music;
import r48.io.r2k.obj.Sound;

/**
 * COPY jun6-2017
 */
public class LdbSystem extends R2kObject {
    public IntegerR2kStruct ldbId = new IntegerR2kStruct(0);
    public StringR2kStruct boatName = new StringR2kStruct();
    public StringR2kStruct shipName = new StringR2kStruct();
    public StringR2kStruct airshipName = new StringR2kStruct();
    public IntegerR2kStruct boatIndex = new IntegerR2kStruct(0);
    public IntegerR2kStruct shipIndex = new IntegerR2kStruct(0);
    public IntegerR2kStruct airshipIndex = new IntegerR2kStruct(0);
    public StringR2kStruct titleName = new StringR2kStruct();
    public StringR2kStruct gameoverName = new StringR2kStruct();
    public StringR2kStruct systemName = new StringR2kStruct();
    public StringR2kStruct system2Name = new StringR2kStruct();

    public ArraySizeR2kInterpretable<ShortR2kStruct> partySize = new ArraySizeR2kInterpretable<ShortR2kStruct>();
    public ArrayR2kStruct<ShortR2kStruct> party = new ArrayR2kStruct<ShortR2kStruct>(partySize, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    }, true);
    public ArraySizeR2kInterpretable<ShortR2kStruct> menuCommandsSize = new ArraySizeR2kInterpretable<ShortR2kStruct>();
    public ArrayR2kStruct<ShortR2kStruct> menuCommands = new ArrayR2kStruct<ShortR2kStruct>(menuCommandsSize, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    }, true);

    public Music titleMusic = new Music();
    public Music battleMusic = new Music();
    public Music battleEndMusic = new Music();
    public Music innMusic = new Music();
    public Music boatMusic = new Music();
    public Music shipMusic = new Music();
    public Music airshipMusic = new Music();
    public Music gameoverMusic = new Music();

    public Sound cursorSe = new Sound();
    public Sound decisionSe = new Sound();
    public Sound cancelSe = new Sound();
    public Sound buzzerSe = new Sound();
    public Sound battleSe = new Sound();
    public Sound escapeSe = new Sound();
    public Sound enemyAttackSe = new Sound();
    public Sound enemyHurtSe = new Sound();
    public Sound actorHurtSe = new Sound();
    public Sound dodgeSe = new Sound();
    public Sound enemyDeathSe = new Sound();
    public Sound itemSe = new Sound();
    public IntegerR2kStruct transitionOut = new IntegerR2kStruct(0);
    public IntegerR2kStruct transitionIn = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleStartFadeout = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleStartFadein = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleEndFadeout = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleEndFadein = new IntegerR2kStruct(0);
    public IntegerR2kStruct messageStretch = new IntegerR2kStruct(0);
    public IntegerR2kStruct fontId = new IntegerR2kStruct(0);
    public IntegerR2kStruct testCondition = new IntegerR2kStruct(0);
    public IntegerR2kStruct testActor = new IntegerR2kStruct(0);
    public StringR2kStruct battletestBackground = new StringR2kStruct();
    public SparseArrayAR2kStruct<BlobR2kStruct> battletestData = new SparseArrayAR2kStruct<BlobR2kStruct>(new ISupplier<BlobR2kStruct>() {
        @Override
        public BlobR2kStruct get() {
            return new BlobR2kStruct(R2kUtil.supplyBlank(1, (byte) 0));
        }
    });
    public IntegerR2kStruct magic = new IntegerR2kStruct(0);
    public IntegerR2kStruct battletestTerrain = new IntegerR2kStruct(0);
    public IntegerR2kStruct battletestFormation = new IntegerR2kStruct(0);
    public IntegerR2kStruct battletestCondition = new IntegerR2kStruct(0);
    public BooleanR2kStruct frameShow = new BooleanR2kStruct(false);
    public StringR2kStruct frameName = new StringR2kStruct();
    public BooleanR2kStruct invanim = new BooleanR2kStruct(false);
    public BooleanR2kStruct showTitle = new BooleanR2kStruct(true);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x0A, ldbId, "@ldb_id"),
                new Index(0x0B, boatName, "@boat_name"),
                new Index(0x0C, shipName, "@ship_name"),
                new Index(0x0D, airshipName, "@airship_name"),
                new Index(0x0E, boatIndex, "@boat_index"),
                new Index(0x0F, shipIndex, "@ship_index"),
                new Index(0x10, airshipIndex, "@airship_index"),
                new Index(0x11, titleName, "@title_name"),
                new Index(0x12, gameoverName, "@gameover_name"),
                new Index(0x13, systemName, "@system_name"),
                new Index(0x14, system2Name, "@system2_name_2k3"),
                new Index(0x15, partySize),
                new Index(0x16, party, "@party"),
                new Index(0x1A, menuCommandsSize),
                new Index(0x1B, menuCommands, "@menu_commands_2k3"),
                new Index(0x1F, titleMusic, "@title_music"),
                new Index(0x20, battleMusic, "@battle_music"),
                new Index(0x21, battleEndMusic, "@battle_end_music"),
                new Index(0x22, innMusic, "@inn_music"),
                new Index(0x23, boatMusic, "@boat_music"),
                new Index(0x24, shipMusic, "@ship_music"),
                new Index(0x25, airshipMusic, "@airship_music"),
                new Index(0x26, gameoverMusic, "@gameover_music"),
                new Index(0x29, cursorSe, "@cursor_se"),
                new Index(0x2A, decisionSe, "@decision_se"),
                new Index(0x2B, cancelSe, "@cancel_se"),
                new Index(0x2C, buzzerSe, "@buzzer_se"),
                new Index(0x2D, battleSe, "@battle_se"),
                new Index(0x2E, escapeSe, "@escape_se"),
                new Index(0x2F, enemyAttackSe, "@enemy_attack_se"),
                new Index(0x30, enemyHurtSe, "@enemy_hurt_se"),
                new Index(0x31, actorHurtSe, "@actor_hurt_se"),
                new Index(0x32, dodgeSe, "@dodge_se"),
                new Index(0x33, enemyDeathSe, "@enemy_death_se"),
                new Index(0x34, itemSe, "@item_se"),
                new Index(0x3D, transitionOut, "@transition_out"),
                new Index(0x3E, transitionIn, "@transition_in"),
                new Index(0x3F, battleStartFadeout, "@battle_start_fadeout"),
                new Index(0x40, battleStartFadein, "@battle_start_fadein"),
                new Index(0x41, battleEndFadeout, "@battle_end_fadeout"),
                new Index(0x42, battleEndFadein, "@battle_end_fadein"),
                new Index(0x47, messageStretch, "@message_stretch"),
                new Index(0x48, fontId, "@font_id"),
                new Index(0x51, testCondition, "@test_condition"),
                new Index(0x52, testActor, "@test_actor"),
                new Index(0x54, battletestBackground, "@test_battle_background"),
                new Index(0x55, battletestData, "@test_battle_data"),
                new Index(0x5B, magic, "@magic_number"),
                new Index(0x5E, battletestTerrain, "@test_battle_terrain"),
                new Index(0x5F, battletestFormation, "@test_battle_formation"),
                new Index(0x60, battletestCondition, "@test_battle_condition"),
                new Index(0x63, frameShow, "@frame_show_2k3"),
                new Index(0x64, frameName, "@frame_name_2k3"),
                new Index(0x65, invanim, "@invert_animations_2k3"),
                new Index(0x6F, showTitle, "@show_title_2k3"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::System", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

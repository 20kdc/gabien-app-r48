/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.struct.BPB;

/**
 * Created on 05/06/17.
 */
public class Actor extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct title = new StringR2kStruct();
    public StringR2kStruct charName = new StringR2kStruct();
    public IntegerR2kStruct charIdx = new IntegerR2kStruct(0);
    public BooleanR2kStruct transparent = new BooleanR2kStruct(false);
    public IntegerR2kStruct initLevel = new IntegerR2kStruct(1);
    public IntegerR2kStruct finalLevel = new IntegerR2kStruct(99);
    public BooleanR2kStruct canCrit = new BooleanR2kStruct(true);
    public IntegerR2kStruct critPercent = new IntegerR2kStruct(30);
    public StringR2kStruct faceName = new StringR2kStruct();
    public IntegerR2kStruct faceIdx = new IntegerR2kStruct(0);
    public BooleanR2kStruct dualWield = new BooleanR2kStruct(false);
    public BooleanR2kStruct lockEquipment = new BooleanR2kStruct(false);
    public BooleanR2kStruct autoBattle = new BooleanR2kStruct(false);
    public BooleanR2kStruct superGuard = new BooleanR2kStruct(false);
    public BPB parameters = new BPB();
    public IntegerR2kStruct initLevelExp = new IntegerR2kStruct(300);
    public IntegerR2kStruct eachLevelExpP = new IntegerR2kStruct(300);
    public IntegerR2kStruct eachLevelExpModC = new IntegerR2kStruct(0);
    public ArrayR2kStruct<ShortR2kStruct> equipment = new ArrayR2kStruct<ShortR2kStruct>(null, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    }, true);
    public IntegerR2kStruct noWeaponAttackAnim = new IntegerR2kStruct(1);
    public IntegerR2kStruct aClass = new IntegerR2kStruct(0);
    public IntegerR2kStruct batPosX = new IntegerR2kStruct(220);
    public IntegerR2kStruct batPosY = new IntegerR2kStruct(120);
    public IntegerR2kStruct battlerAnim = new IntegerR2kStruct(1);
    public SparseArrayHR2kStruct<Learning> learnSkills = new SparseArrayHR2kStruct<Learning>(new ISupplier<Learning>() {
        @Override
        public Learning get() {
            return new Learning();
        }
    });
    public BooleanR2kStruct canRename = new BooleanR2kStruct(false);
    public StringR2kStruct renameResult = new StringR2kStruct();
    public ArraySizeR2kInterpretable<ByteR2kStruct> stateRanksSz = new ArraySizeR2kInterpretable<ByteR2kStruct>();
    public ArraySetR2kStruct<ByteR2kStruct> stateRanks = new ArraySetR2kStruct<ByteR2kStruct>(stateRanksSz, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(2);
        }
    }, true);
    public ArraySizeR2kInterpretable<ByteR2kStruct> attrRanksSz = new ArraySizeR2kInterpretable<ByteR2kStruct>();
    public ArraySetR2kStruct<ByteR2kStruct> attrRanks = new ArraySetR2kStruct<ByteR2kStruct>(attrRanksSz, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(2);
        }
    }, true);
    public ArrayR2kStruct<Int32R2kStruct> battleCommands = new ArrayR2kStruct<Int32R2kStruct>(null, new ISupplier<Int32R2kStruct>() {
        @Override
        public Int32R2kStruct get() {
            return new Int32R2kStruct(0);
        }
    }, true);

    @Override
    public Index[] getIndices() {
        return new Index[] {

                new Index(0x01, name, "@name"),
                new Index(0x02, title, "@title"),
                new Index(0x03, charName, "@character_name"),
                new Index(0x04, charIdx, "@character_index"),
                new Index(0x05, transparent, "@character_blend_mode"),
                new Index(0x07, initLevel, "@init_level"),
                new Index(0x08, finalLevel, "@final_level"),
                new Index(0x09, canCrit, "@can_crit"),
                new Index(0x0A, critPercent, "@crit_percent"),
                new Index(0x0F, faceName, "@face_name"),
                new Index(0x10, faceIdx, "@face_index"),
                new Index(0x15, dualWield, "@dual_wield"),
                new Index(0x16, lockEquipment, "@lock_equipment"),
                new Index(0x17, autoBattle, "@battle_auto"),
                new Index(0x18, superGuard, "@battle_super_guard"),
                new Index(0x1F, parameters, "@battle_parameters"),
                new Index(0x29, initLevelExp, "@init_level_exp"),
                new Index(0x2A, eachLevelExpP, "@each_level_exp_mul"),
                new Index(0x2B, eachLevelExpModC, "@each_level_exp_add"),
                new Index(0x33, equipment, "@equipment"),
                new Index(0x38, noWeaponAttackAnim, "@no_weapon_attack_anim"),
                new Index(0x39, aClass, "@class_2k3"),
                new Index(0x3B, batPosX, "@battle_posx_2k3"),
                new Index(0x3C, batPosY, "@battle_posy_2k3"),
                new Index(0x3E, battlerAnim, "@battler_anim_2k3"),
                new Index(0x3F, learnSkills, "@learn_skills"),
                new Index(0x42, canRename, "@editor_use_skillspanel_name"),
                new Index(0x43, renameResult, "@battle_skillspanel_name"),
                new Index(0x47, stateRanksSz),
                new Index(0x48, stateRanks, "@state_ranks"),
                new Index(0x49, attrRanksSz),
                new Index(0x4A, attrRanks, "@element_ranks"),
                new Index(0x50, battleCommands, "@battle_commands_2k3")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Actor", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

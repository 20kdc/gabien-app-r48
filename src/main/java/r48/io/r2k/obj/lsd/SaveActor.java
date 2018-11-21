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

/**
 * They're pieces on the board that are waiting to be moved...
 */
public class SaveActor extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct title = new StringR2kStruct();
    public StringR2kStruct charName = new StringR2kStruct();
    public IntegerR2kStruct charIdx = new IntegerR2kStruct(0);
    public IntegerR2kStruct charFlags = new IntegerR2kStruct(0);
    public StringR2kStruct faceName = new StringR2kStruct();
    public IntegerR2kStruct faceIdx = new IntegerR2kStruct(0);
    public IntegerR2kStruct level = new IntegerR2kStruct(-1);
    public IntegerR2kStruct exp = new IntegerR2kStruct(-1);
    public IntegerR2kStruct hpMod = new IntegerR2kStruct(-1);
    public IntegerR2kStruct spMod = new IntegerR2kStruct(-1);
    public IntegerR2kStruct attackMod = new IntegerR2kStruct(0);
    public IntegerR2kStruct defenseMod = new IntegerR2kStruct(0);
    public IntegerR2kStruct spiritMod = new IntegerR2kStruct(0);
    public IntegerR2kStruct agilityMod = new IntegerR2kStruct(0);
    public ArraySizeR2kInterpretable<ShortR2kStruct> skillsSize = new ArraySizeR2kInterpretable<ShortR2kStruct>(true);
    public ArrayR2kStruct<ShortR2kStruct> skills = new ArrayR2kStruct<ShortR2kStruct>(skillsSize, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    });
    public ArrayR2kStruct<ShortR2kStruct> equipment = new ArrayR2kStruct<ShortR2kStruct>(null, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    }, 5);
    public IntegerR2kStruct currentHp = new IntegerR2kStruct(-1);
    public IntegerR2kStruct currentSp = new IntegerR2kStruct(-1);
    public ArrayR2kStruct<Int32R2kStruct> battleCommands = new ArrayR2kStruct<Int32R2kStruct>(null, new ISupplier<Int32R2kStruct>() {
        @Override
        public Int32R2kStruct get() {
            return new Int32R2kStruct(-1);
        }
    }, false, 7);
    public ArraySizeR2kInterpretable<ShortR2kStruct> statesSize = new ArraySizeR2kInterpretable<ShortR2kStruct>(true);
    public ArrayR2kStruct<ShortR2kStruct> states = new ArrayR2kStruct<ShortR2kStruct>(statesSize, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    });
    public BooleanR2kStruct changedBattleCommands = new BooleanR2kStruct(false);
    public IntegerR2kStruct classId = new IntegerR2kStruct(-1);
    public IntegerR2kStruct row = new IntegerR2kStruct(0);
    public BooleanR2kStruct twoWeapon = new BooleanR2kStruct(false);
    public BooleanR2kStruct lockEquipment = new BooleanR2kStruct(false);
    public BooleanR2kStruct autoBattle = new BooleanR2kStruct(false);
    public BooleanR2kStruct superGuard = new BooleanR2kStruct(false);
    public IntegerR2kStruct battlerAnimation = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, title, "@title"),
                new Index(0x0B, charName, "@character_name"),
                new Index(0x0C, charIdx, "@character_index"),
                new Index(0x0D, charFlags, "@character_flags"),
                new Index(0x15, faceName, "@face_name"),
                new Index(0x16, faceIdx, "@face_index"),
                new Index(0x1F, level, "@level"),
                new Index(0x20, exp, "@exp"),
                new Index(0x21, hpMod, "@hp_mod"),
                new Index(0x22, spMod, "@sp_mod"),
                new Index(0x29, attackMod, "@atk_mod"),
                new Index(0x2A, defenseMod, "@def_mod"),
                new Index(0x2B, spiritMod, "@spi_mod"),
                new Index(0x2C, agilityMod, "@agi_mod"),
                new Index(0x33, skillsSize),
                new Index(0x34, skills, "@skills"),
                new Index(0x3D, equipment, "@equipment"),
                new Index(0x47, currentHp, "@current_hp"),
                new Index(0x48, currentSp, "@current_sp"),
                // check for an 0x4F / 79
                new Index(0x50, battleCommands, "@battle_commands_2k3"),
                new Index(0x51, statesSize),
                new Index(0x52, states, "@states"),

                new Index(0x53, changedBattleCommands, "@changed_battle_commands_2k3"),
                new Index(0x5A, classId, "@class_id_2k3"),
                new Index(0x5B, row, "@row"),
                new Index(0x5C, twoWeapon, "@two_weapon"),
                new Index(0x5D, lockEquipment, "@lock_equipment"),
                new Index(0x5E, autoBattle, "@auto_battle"),
                new Index(0x5F, superGuard, "@super_guard"),
                new Index(0x60, battlerAnimation, "@battler_animation_2k3"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO root = new RubyIO().setSymlike("RPG::SaveActor", true);
        asRIOISF(root);
        return root;
    }
}

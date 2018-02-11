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
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;
import r48.io.r2k.obj.Sound;

/**
 * Created on 05/06/17.
 */
public class Skill extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct description = new StringR2kStruct();
    public StringR2kStruct um1 = new StringR2kStruct();
    public StringR2kStruct um2 = new StringR2kStruct();
    public IntegerR2kStruct fm = new IntegerR2kStruct(0);
    public IntegerR2kStruct type = new IntegerR2kStruct(0);
    public BooleanR2kStruct sp = new BooleanR2kStruct(false);
    public IntegerR2kStruct spPercent = new IntegerR2kStruct(1);
    public IntegerR2kStruct spCost = new IntegerR2kStruct(0);
    public IntegerR2kStruct scope = new IntegerR2kStruct(0);
    public IntegerR2kStruct switchId = new IntegerR2kStruct(1);
    public IntegerR2kStruct animationId = new IntegerR2kStruct(0);
    public Sound soundEffect = new Sound();
    public BooleanR2kStruct useOutBat = new BooleanR2kStruct(true);
    public BooleanR2kStruct useInBat = new BooleanR2kStruct(false);
    public BooleanR2kStruct stateAdd = new BooleanR2kStruct(false);
    public IntegerR2kStruct physRate = new IntegerR2kStruct(0);
    public IntegerR2kStruct magiRate = new IntegerR2kStruct(3);
    public IntegerR2kStruct variRate = new IntegerR2kStruct(4);
    public IntegerR2kStruct power = new IntegerR2kStruct(0);
    public IntegerR2kStruct hit = new IntegerR2kStruct(100);
    public BooleanR2kStruct affectHp = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectSp = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectAtk = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectDef = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectSpi = new BooleanR2kStruct(false);
    public BooleanR2kStruct affectAgi = new BooleanR2kStruct(false);
    public BooleanR2kStruct absDam = new BooleanR2kStruct(false);
    public BooleanR2kStruct igDef = new BooleanR2kStruct(false);

    // Uhoh it's Array Sizes. But at least it's near the end
    public ArraySizeR2kInterpretable<BooleanR2kStruct> basSE = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> sEfx = new ArraySetR2kStruct<BooleanR2kStruct>(basSE, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(false);
        }
    }, true);
    public ArraySizeR2kInterpretable<BooleanR2kStruct> basAE = new ArraySizeR2kInterpretable<BooleanR2kStruct>();
    public ArraySetR2kStruct<BooleanR2kStruct> aEfx = new ArraySetR2kStruct<BooleanR2kStruct>(basAE, new ISupplier<BooleanR2kStruct>() {
        @Override
        public BooleanR2kStruct get() {
            return new BooleanR2kStruct(false);
        }
    }, true);

    public BooleanR2kStruct afAtDef = new BooleanR2kStruct(false);
    public IntegerR2kStruct defBattlerAnim = new IntegerR2kStruct(1);

    public BlobR2kStruct battlerAnimMap = new BlobR2kStruct(R2kUtil.userspaceBinder + "R2kBattlerAnimationMap", R2kUtil.supplyBlank(1, (byte) 0));

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, description, "@description"),
                new Index(0x03, um1, "@use_text_1_2KO"),
                new Index(0x04, um2, "@use_text_2_2KO"),
                new Index(0x07, fm, "@failure_message_2KO"),
                new Index(0x08, type, "@type"),
                new Index(0x09, sp, "@sp_cost_percent_2k3"),
                new Index(0x0A, spPercent, "@sp_cost_val_percent_2k3"),
                new Index(0x0B, spCost, "@sp_cost_val_normal"),
                new Index(0x0C, scope, "@scope_n_healing"),
                new Index(0x0D, switchId, "@switch_control_target"),
                new Index(0x0E, animationId, "@animation"),
                new Index(0x10, soundEffect, "@sound"),
                new Index(0x12, useOutBat, "@usable_outside_battle"),
                new Index(0x13, useInBat, "@usable_in_battle"),
                new Index(0x14, stateAdd, "@add_states_2k3"),
                new Index(0x15, physRate, "@phys_dmg_frac20"),
                new Index(0x16, magiRate, "@mag_dmg_frac20"),
                new Index(0x17, variRate, "@variance"),
                new Index(0x18, power, "@base_dmg"),
                new Index(0x19, hit, "@hit_chance"),
                new Index(0x1F, affectHp, "@mod_hp"),
                new Index(0x20, affectSp, "@mod_sp"),
                new Index(0x21, affectAtk, "@mod_atk"),
                new Index(0x22, affectDef, "@mod_def"),
                new Index(0x23, affectSpi, "@mod_spi"),
                new Index(0x24, affectAgi, "@mod_agi"),
                new Index(0x25, absDam, "@steal_enemy_hp"),
                new Index(0x26, igDef, "@ignore_def"),
                new Index(0x29, basSE),
                new Index(0x2A, sEfx, "@mod_states"),
                new Index(0x2B, basAE),
                new Index(0x2C, aEfx, "@mod_by_attributes"),
                new Index(0x2D, afAtDef, "@affect_target_attr_defence"),
                new Index(0x31, defBattlerAnim, "@OFED_battler_anim_display_actor"),
                new Index(0x32, battlerAnimMap, "@battler_anim_data"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Skill", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

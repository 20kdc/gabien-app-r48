/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Created on 06/06/17.
 */
public class EnemyAction extends R2kObject {
    public IntegerR2kStruct kind = new IntegerR2kStruct(0);
    public IntegerR2kStruct basic = new IntegerR2kStruct(1);
    public IntegerR2kStruct skillId = new IntegerR2kStruct(1);
    public IntegerR2kStruct enemyId = new IntegerR2kStruct(1);
    public IntegerR2kStruct conditionType = new IntegerR2kStruct(0);
    public IntegerR2kStruct conditionP1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct conditionP2 = new IntegerR2kStruct(0);
    public IntegerR2kStruct switchId = new IntegerR2kStruct(1);
    public BooleanR2kStruct switchOn = new BooleanR2kStruct(false);
    public IntegerR2kStruct switchOnId = new IntegerR2kStruct(1);
    public BooleanR2kStruct switchOff = new BooleanR2kStruct(false);
    public IntegerR2kStruct switchOffId = new IntegerR2kStruct(1);
    public IntegerR2kStruct rating = new IntegerR2kStruct(50);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, kind, "@act_kind"),
                new Index(0x02, basic, "@act_basic"),
                new Index(0x03, skillId, "@act_skill"),
                new Index(0x04, enemyId, "@act_transform_enemy"),
                new Index(0x05, conditionType, "@condition_type"),
                new Index(0x06, conditionP1, "@condition_range_low"),
                new Index(0x07, conditionP2, "@condition_range_high"),
                new Index(0x08, switchId, "@condition_opt_switch_id"),
                new Index(0x09, switchOn, "@act_set_switch"),
                new Index(0x0A, switchOnId, "@act_set_switch_id"),
                new Index(0x0B, switchOff, "@act_reset_switch"),
                new Index(0x0C, switchOffId, "@act_reset_switch_id"),
                new Index(0x0D, rating, "@rating"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::EnemyAction", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

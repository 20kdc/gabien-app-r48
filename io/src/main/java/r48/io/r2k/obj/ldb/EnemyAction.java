/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2LcfBinding;
import r48.io.r2k.dm2chk.DM2LcfBoolean;
import r48.io.r2k.dm2chk.DM2LcfInteger;
import r48.io.r2k.dm2chk.DM2R2kObject;

/**
 * Created on 06/06/17.
 */
public class EnemyAction extends DM2R2kObject {
    @DM2FXOBinding("@act_kind") @DM2LcfBinding(0x01) @DM2LcfInteger(0)
    public IntegerR2kStruct kind;
    @DM2FXOBinding("@act_basic") @DM2LcfBinding(0x02) @DM2LcfInteger(1)
    public IntegerR2kStruct basic;
    @DM2FXOBinding("@act_skill") @DM2LcfBinding(0x03) @DM2LcfInteger(1)
    public IntegerR2kStruct skillId;
    @DM2FXOBinding("@act_transform_enemy") @DM2LcfBinding(0x04) @DM2LcfInteger(1)
    public IntegerR2kStruct enemyId;
    @DM2FXOBinding("@condition_type") @DM2LcfBinding(0x05) @DM2LcfInteger(0)
    public IntegerR2kStruct conditionType;
    @DM2FXOBinding("@condition_range_low") @DM2LcfBinding(0x06) @DM2LcfInteger(0)
    public IntegerR2kStruct conditionP1;
    @DM2FXOBinding("@condition_range_high") @DM2LcfBinding(0x07) @DM2LcfInteger(0)
    public IntegerR2kStruct conditionP2;
    @DM2FXOBinding("@condition_opt_switch_id") @DM2LcfBinding(0x08) @DM2LcfInteger(1)
    public IntegerR2kStruct switchId;
    @DM2FXOBinding("@act_set_switch") @DM2LcfBinding(0x09) @DM2LcfBoolean(false)
    public BooleanR2kStruct switchOn;
    @DM2FXOBinding("@act_set_switch_id") @DM2LcfBinding(0x0A) @DM2LcfInteger(1)
    public IntegerR2kStruct switchOnId;
    @DM2FXOBinding("@act_reset_switch") @DM2LcfBinding(0x0B) @DM2LcfBoolean(false)
    public BooleanR2kStruct switchOff;
    @DM2FXOBinding("@act_reset_switch_id") @DM2LcfBinding(0x0C) @DM2LcfInteger(1)
    public IntegerR2kStruct switchOffId;
    @DM2FXOBinding("@rating") @DM2LcfBinding(0x0D) @DM2LcfInteger(50)
    public IntegerR2kStruct rating;

    public EnemyAction() {
        super("RPG::EnemyAction");
    }
}

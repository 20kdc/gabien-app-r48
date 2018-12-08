/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DM2FXOBinding;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.DoubleR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.obj.MoveRoute;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public abstract class SaveCharacter extends DM2R2kObject {
    @DM2FXOBinding("@active") @DM2LcfBinding(1) @DM2LcfBoolean(true)
    public BooleanR2kStruct active;
    @DM2FXOBinding("@map") @DM2LcfBinding(11) @DM2LcfInteger(-1)
    public IntegerR2kStruct mapId;
    @DM2FXOBinding("@x") @DM2LcfBinding(12) @DM2LcfInteger(-1)
    public IntegerR2kStruct x;
    @DM2FXOBinding("@y") @DM2LcfBinding(13) @DM2LcfInteger(-1)
    public IntegerR2kStruct y;
    @DM2FXOBinding("@dir") @DM2LcfBinding(21) @DM2LcfInteger(2)
    public IntegerR2kStruct dir;
    @DM2FXOBinding("@character_direction") @DM2LcfBinding(22) @DM2LcfInteger(2)
    public IntegerR2kStruct spriteDir;
    @DM2FXOBinding("@character_pattern") @DM2LcfBinding(23) @DM2LcfInteger(1)
    public IntegerR2kStruct spritePattern;
    @DM2FXOBinding("@transparency") @DM2LcfBinding(24) @DM2LcfInteger(0)
    public IntegerR2kStruct transparency;
    @DM2FXOBinding("@remaining_step") @DM2LcfBinding(31) @DM2LcfInteger(0)
    public IntegerR2kStruct remainingStep;
    @DM2FXOBinding("@move_freq") @DM2LcfBinding(32) @DM2LcfInteger(2)
    public IntegerR2kStruct moveFrequency;
    @DM2FXOBinding("@layer") @DM2LcfBinding(33) @DM2LcfInteger(1)
    public IntegerR2kStruct layer;
    @DM2FXOBinding("@block_other_events") @DM2LcfBinding(34) @DM2LcfBoolean(false)
    public BooleanR2kStruct overlapForbidden;
    @DM2FXOBinding("@anim_type") @DM2LcfBinding(35) @DM2LcfInteger(1)
    public IntegerR2kStruct animType;
    @DM2FXOBinding("@lock_dir") @DM2LcfBinding(36) @DM2LcfBoolean(false)
    public BooleanR2kStruct lockFacing;
    @DM2FXOBinding("@move_speed") @DM2LcfBinding(37) @DM2LcfInteger(3)
    public IntegerR2kStruct moveSpeed;

    @DM2FXOBinding("@move_route") @DM2LcfBinding(41) @DM2LcfObject
    public MoveRoute moveRoute;
    @DM2FXOBinding("@move_route_overwrite") @DM2LcfBinding(42) @DM2LcfBoolean(false)
    public BooleanR2kStruct moveRouteInUse;
    @DM2FXOBinding("@move_route_position") @DM2LcfBinding(43) @DM2LcfInteger(0)
    public IntegerR2kStruct moveRouteIndex;
    @DM2FXOBinding("@move_route_has_looped") @DM2LcfBinding(44) @DM2LcfBoolean(false)
    public BooleanR2kStruct moveRouteRepeated;

    // need more info on this!
    @DM2FXOBinding("@anim_paused") @DM2LcfBinding(48) @DM2LcfInteger(0)
    public IntegerR2kStruct animPaused;

    @DM2FXOBinding("@through") @DM2LcfBinding(51) @DM2LcfBoolean(false)
    public BooleanR2kStruct through;
    @DM2FXOBinding("@wait_time_counter") @DM2LcfBinding(52) @DM2LcfInteger(0)
    public IntegerR2kStruct waitTimeCounter;
    @DM2FXOBinding("@anim_count") @DM2LcfBinding(53) @DM2LcfInteger(0)
    public IntegerR2kStruct animCount;
    @DM2FXOBinding("@wait_time") @DM2LcfBinding(54) @DM2LcfInteger(0)
    public IntegerR2kStruct waitTime;

    @DM2FXOBinding("@jumping") @DM2LcfBinding(61) @DM2LcfBoolean(false)
    public BooleanR2kStruct jumping;
    @DM2FXOBinding("@begin_jump_x") @DM2LcfBinding(62) @DM2LcfInteger(0)
    public IntegerR2kStruct beginJumpX;
    @DM2FXOBinding("@begin_jump_y") @DM2LcfBinding(63) @DM2LcfInteger(0)
    public IntegerR2kStruct beginJumpY;
    @DM2FXOBinding("@unknown_47") @DM2LcfBinding(71) @DM2LcfInteger(0)
    public IntegerR2kStruct unknown47;

    @DM2FXOBinding("@flying") @DM2LcfBinding(72) @DM2LcfBoolean(false)
    public BooleanR2kStruct flying;

    @DM2FXOBinding("@character_name") @DM2LcfBinding(73) @DM2LcfObject
    public StringR2kStruct spriteName;

    @DM2FXOBinding("@character_index") @DM2LcfBinding(74) @DM2LcfInteger(-1)
    public IntegerR2kStruct spriteId;
    @DM2FXOBinding("@moved_on_frame") @DM2LcfBinding(75) @DM2LcfInteger(0)
    public IntegerR2kStruct movedOnFrame;
    @DM2FXOBinding("@flash_red") @DM2LcfBinding(81) @DM2LcfInteger(100)
    public IntegerR2kStruct flashRed;
    @DM2FXOBinding("@flash_green") @DM2LcfBinding(82) @DM2LcfInteger(100)
    public IntegerR2kStruct flashGreen;
    @DM2FXOBinding("@flash_blue") @DM2LcfBinding(83) @DM2LcfInteger(100)
    public IntegerR2kStruct flashBlue;
    @DM2FXOBinding("@flash_position") @DM2LcfBinding(84) @DM2LcfInteger(0)
    public DoubleR2kStruct flashCurrentLevel;
    @DM2FXOBinding("@flash_frames_left") @DM2LcfBinding(83) @DM2LcfInteger(0)
    public IntegerR2kStruct flashTimeLeft;

    public SaveCharacter(String s) {
        super(s);
    }
}

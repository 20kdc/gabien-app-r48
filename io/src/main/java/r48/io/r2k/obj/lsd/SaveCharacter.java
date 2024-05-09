/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
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
    @DMFXOBinding("@active") @DM2LcfBinding(1) @DMCXBoolean(true)
    public BooleanR2kStruct active;
    @DMFXOBinding("@map") @DM2LcfBinding(11) @DMCXInteger(-1)
    public IntegerR2kStruct mapId;
    @DMFXOBinding("@x") @DM2LcfBinding(12) @DMCXInteger(-1)
    public IntegerR2kStruct x;
    @DMFXOBinding("@y") @DM2LcfBinding(13) @DMCXInteger(-1)
    public IntegerR2kStruct y;
    @DMFXOBinding("@dir") @DM2LcfBinding(21) @DMCXInteger(2)
    public IntegerR2kStruct dir;
    @DMFXOBinding("@character_direction") @DM2LcfBinding(22) @DMCXInteger(2)
    public IntegerR2kStruct spriteDir;
    @DMFXOBinding("@character_pattern") @DM2LcfBinding(23) @DMCXInteger(1)
    public IntegerR2kStruct spritePattern;
    @DMFXOBinding("@transparency") @DM2LcfBinding(24) @DMCXInteger(0)
    public IntegerR2kStruct transparency;
    @DMFXOBinding("@remaining_step") @DM2LcfBinding(31) @DMCXInteger(0)
    public IntegerR2kStruct remainingStep;
    @DMFXOBinding("@move_freq") @DM2LcfBinding(32) @DMCXInteger(2)
    public IntegerR2kStruct moveFrequency;
    @DMFXOBinding("@layer") @DM2LcfBinding(33) @DMCXInteger(1)
    public IntegerR2kStruct layer;
    @DMFXOBinding("@block_other_events") @DM2LcfBinding(34) @DMCXBoolean(false)
    public BooleanR2kStruct overlapForbidden;
    @DMFXOBinding("@anim_type") @DM2LcfBinding(35) @DMCXInteger(1)
    public IntegerR2kStruct animType;
    @DMFXOBinding("@lock_dir") @DM2LcfBinding(36) @DMCXBoolean(false)
    public BooleanR2kStruct lockFacing;
    @DMFXOBinding("@move_speed") @DM2LcfBinding(37) @DMCXInteger(3)
    public IntegerR2kStruct moveSpeed;

    @DMFXOBinding("@move_route") @DM2LcfBinding(41) @DMCXObject
    public MoveRoute moveRoute;
    @DMFXOBinding("@move_route_overwrite") @DM2LcfBinding(42) @DMCXBoolean(false)
    public BooleanR2kStruct moveRouteInUse;
    @DMFXOBinding("@move_route_position") @DM2LcfBinding(43) @DMCXInteger(0)
    public IntegerR2kStruct moveRouteIndex;
    @DMFXOBinding("@move_route_has_looped") @DM2LcfBinding(44) @DMCXBoolean(false)
    public BooleanR2kStruct moveRouteRepeated;

    // need more info on this!
    @DMFXOBinding("@anim_paused") @DM2LcfBinding(48) @DMCXInteger(0)
    public IntegerR2kStruct animPaused;

    @DMFXOBinding("@through") @DM2LcfBinding(51) @DMCXBoolean(false)
    public BooleanR2kStruct through;
    @DMFXOBinding("@wait_time_counter") @DM2LcfBinding(52) @DMCXInteger(0)
    public IntegerR2kStruct waitTimeCounter;
    @DMFXOBinding("@anim_count") @DM2LcfBinding(53) @DMCXInteger(0)
    public IntegerR2kStruct animCount;
    @DMFXOBinding("@wait_time") @DM2LcfBinding(54) @DMCXInteger(0)
    public IntegerR2kStruct waitTime;

    @DMFXOBinding("@jumping") @DM2LcfBinding(61) @DMCXBoolean(false)
    public BooleanR2kStruct jumping;
    @DMFXOBinding("@begin_jump_x") @DM2LcfBinding(62) @DMCXInteger(0)
    public IntegerR2kStruct beginJumpX;
    @DMFXOBinding("@begin_jump_y") @DM2LcfBinding(63) @DMCXInteger(0)
    public IntegerR2kStruct beginJumpY;
    @DMFXOBinding("@unknown_47") @DM2LcfBinding(71) @DMCXInteger(0)
    public IntegerR2kStruct unknown47;

    @DMFXOBinding("@flying") @DM2LcfBinding(72) @DMCXBoolean(false)
    public BooleanR2kStruct flying;

    @DMFXOBinding("@character_name") @DM2LcfBinding(73) @DMCXObject
    public StringR2kStruct spriteName;

    @DMFXOBinding("@character_index") @DM2LcfBinding(74) @DMCXInteger(-1)
    public IntegerR2kStruct spriteId;
    @DMFXOBinding("@moved_on_frame") @DM2LcfBinding(75) @DMCXInteger(0)
    public IntegerR2kStruct movedOnFrame;
    @DMFXOBinding("@flash_red") @DM2LcfBinding(81) @DMCXInteger(100)
    public IntegerR2kStruct flashRed;
    @DMFXOBinding("@flash_green") @DM2LcfBinding(82) @DMCXInteger(100)
    public IntegerR2kStruct flashGreen;
    @DMFXOBinding("@flash_blue") @DM2LcfBinding(83) @DMCXInteger(100)
    public IntegerR2kStruct flashBlue;
    @DMFXOBinding("@flash_position") @DM2LcfBinding(84) @DMCXInteger(0)
    public DoubleR2kStruct flashCurrentLevel;
    @DMFXOBinding("@flash_frames_left") @DM2LcfBinding(83) @DMCXInteger(0)
    public IntegerR2kStruct flashTimeLeft;

    public SaveCharacter(DMContext ctx, String s) {
        super(ctx, s);
    }
}

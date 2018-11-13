/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.obj.MoveRoute;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public abstract class SaveCharacter extends R2kObject {
    public BooleanR2kStruct active = new BooleanR2kStruct(true);
    public IntegerR2kStruct mapId = new IntegerR2kStruct(-1);
    public IntegerR2kStruct x = new IntegerR2kStruct(-1);
    public IntegerR2kStruct y = new IntegerR2kStruct(-1);
    public IntegerR2kStruct dir = new IntegerR2kStruct(2);
    public IntegerR2kStruct spriteDir = new IntegerR2kStruct(2);
    public IntegerR2kStruct spritePattern = new IntegerR2kStruct(1);
    public IntegerR2kStruct transparency = new IntegerR2kStruct(0);
    public IntegerR2kStruct remainingStep = new IntegerR2kStruct(0);
    public IntegerR2kStruct moveFrequency = new IntegerR2kStruct(2);
    public IntegerR2kStruct layer = new IntegerR2kStruct(1);
    public BooleanR2kStruct overlapForbidden = new BooleanR2kStruct(false);
    public IntegerR2kStruct animType = new IntegerR2kStruct(1);
    public BooleanR2kStruct lockFacing = new BooleanR2kStruct(false);
    // WARNING: Inconsistent (SPL has a speed of 4)
    public IntegerR2kStruct moveSpeed = new IntegerR2kStruct(-1);
    public MoveRoute moveRoute = new MoveRoute();
    public BooleanR2kStruct moveRouteInUse = new BooleanR2kStruct(false);
    public IntegerR2kStruct moveRouteIndex = new IntegerR2kStruct(0);
    public BooleanR2kStruct moveRouteRepeated = new BooleanR2kStruct(false);
    // need more info on this!
    public IntegerR2kStruct animPaused = new IntegerR2kStruct(0);
    public BooleanR2kStruct through = new BooleanR2kStruct(false);
    public IntegerR2kStruct waitTimeCounter = new IntegerR2kStruct(0);
    public IntegerR2kStruct animCount = new IntegerR2kStruct(0);
    public IntegerR2kStruct waitTime = new IntegerR2kStruct(0);
    public BooleanR2kStruct jumping = new BooleanR2kStruct(false);
    public IntegerR2kStruct beginJumpX = new IntegerR2kStruct(0);
    public IntegerR2kStruct beginJumpY = new IntegerR2kStruct(0);
    public IntegerR2kStruct unknown47 = new IntegerR2kStruct(0);
    public BooleanR2kStruct flying = new BooleanR2kStruct(false);
    public StringR2kStruct spriteName = new StringR2kStruct();
    public IntegerR2kStruct spriteId = new IntegerR2kStruct(-1);
    public IntegerR2kStruct movedOnFrame = new IntegerR2kStruct(0);
    public IntegerR2kStruct flashRed = new IntegerR2kStruct(100);
    public IntegerR2kStruct flashGreen = new IntegerR2kStruct(100);
    public IntegerR2kStruct flashBlue = new IntegerR2kStruct(100);
    public DoubleR2kStruct flashCurrentLevel = new DoubleR2kStruct(0);
    public IntegerR2kStruct flashTimeLeft = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, active, "@active"),
                new Index(0x0B, mapId, "@map"),
                new Index(0x0C, x, "@x"),
                new Index(0x0D, y, "@y"),
                new Index(0x15, dir, "@dir"),
                new Index(0x16, spriteDir, "@character_direction"),
                new Index(0x17, spritePattern, "@character_pattern"),
                new Index(0x18, transparency, "@transparency"),
                new Index(0x1F, remainingStep, "@remaining_step"),
                new Index(0x20, moveFrequency, "@move_freq"),
                new Index(0x21, layer, "@layer"),
                new Index(0x22, overlapForbidden, "@block_other_events"),
                new Index(0x23, animType, "@anim_type"),
                new Index(0x24, lockFacing, "@lock_dir"),
                new Index(0x25, moveSpeed, "@move_speed"),
                new Index(0x29, moveRoute, "@move_route"),
                new Index(0x2A, moveRouteInUse, "@move_route_overwrite"),
                new Index(0x2B, moveRouteIndex, "@move_route_position"),
                new Index(0x2C, moveRouteRepeated, "@move_route_has_looped"),
                // !!!
                new Index(0x30, animPaused, "@anim_paused"),
                new Index(0x33, through, "@through"),
                new Index(0x34, waitTimeCounter, "@wait_time_counter"),
                new Index(0x35, animCount, "@anim_count"),
                new Index(0x36, waitTime, "@wait_time"),
                new Index(0x3D, jumping, "@jumping"),
                new Index(0x3E, beginJumpX, "@begin_jump_x"),
                new Index(0x3F, beginJumpY, "@begin_jump_y"),
                new Index(0x47, unknown47, "@unknown_47"),
                new Index(0x48, flying, "@flying"),
                new Index(0x49, spriteName, "@character_name"),
                new Index(0x4A, spriteId, "@character_index"),
                new Index(0x4B, movedOnFrame, "@moved_on_frame"),
                new Index(0x51, flashRed, "@flash_red"),
                new Index(0x52, flashGreen, "@flash_green"),
                new Index(0x53, flashBlue, "@flash_blue"),
                new Index(0x54, flashCurrentLevel, "@flash_position"),
                new Index(0x55, flashTimeLeft, "@flash_frames_left"),
                // !!!
        };
    }
}

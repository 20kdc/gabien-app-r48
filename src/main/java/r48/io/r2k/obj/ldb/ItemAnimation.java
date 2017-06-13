/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
public class ItemAnimation extends R2kObject {
    public IntegerR2kStruct type = new IntegerR2kStruct(0);
    public IntegerR2kStruct weaponAnim = new IntegerR2kStruct(0);
    public IntegerR2kStruct movement = new IntegerR2kStruct(0);
    public IntegerR2kStruct afterImage = new IntegerR2kStruct(0);
    public IntegerR2kStruct attacks = new IntegerR2kStruct(0);
    public BooleanR2kStruct ranged = new BooleanR2kStruct(false);
    public IntegerR2kStruct rangedAnim = new IntegerR2kStruct(0);
    public IntegerR2kStruct rangedSpeed = new IntegerR2kStruct(0);
    public IntegerR2kStruct battleAnim = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x03, type, "@type"),
                new Index(0x04, weaponAnim, "@weapon_anim_idx"),
                new Index(0x05, movement, "@movement"),
                new Index(0x06, afterImage, "@has_afterimage"),
                new Index(0x07, attacks, "@attacks"),
                new Index(0x08, ranged, "@ranged"),
                new Index(0x09, rangedAnim, "@ranged_anim_idx"),
                new Index(0x0C, rangedSpeed, "@ranged_speed"),
                new Index(0x0D, battleAnim, "@battle_anim"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::ItemAnimation", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

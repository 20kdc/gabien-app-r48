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
                new Index(0x04, weaponAnim, "@weapon_batanim_idx"),
                new Index(0x05, movement, "@movement"),
                new Index(0x06, afterImage, "@has_afterimage"),
                new Index(0x07, attacks, "@loop_count"),
                new Index(0x08, ranged, "@ranged"),
                new Index(0x09, rangedAnim, "@ranged_batanim_idx"),
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
}

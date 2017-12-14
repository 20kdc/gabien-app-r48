/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public class SaveVehicleLocation extends SaveCharacter {
    public IntegerR2kStruct vehicleType = new IntegerR2kStruct(0);
    public IntegerR2kStruct originalMoverouteIndex = new IntegerR2kStruct(0);
    public IntegerR2kStruct remainingAscent = new IntegerR2kStruct(0);
    public IntegerR2kStruct remainingDescent = new IntegerR2kStruct(0);
    public StringR2kStruct sprite2Name = new StringR2kStruct();
    public IntegerR2kStruct sprite2Index = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return R2kUtil.mergeIndices(super.getIndices(), new Index[] {
                new Index(0x65, vehicleType, "@vehicle_type"),
                new Index(0x66, originalMoverouteIndex, "@original_moveroute_index"),
                new Index(0x6A, remainingAscent, "@remaining_ascent"),
                new Index(0x6B, remainingDescent, "@remaining_descent"),
                new Index(0x6F, sprite2Name, "@sprite2_name"),
                new Index(0x70, sprite2Index, "@sprite2_index"),
        });
    }

    @Override
    public RubyIO asRIO() {
        RubyIO root = new RubyIO().setSymlike("RPG::SaveVehicleLocation", true);
        asRIOISF(root);
        return root;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.DoubleR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

public class SaveTitle extends R2kObject {
    public DoubleR2kStruct timestamp = new DoubleR2kStruct();
    public StringR2kStruct hero_name = new StringR2kStruct();
    public IntegerR2kStruct hero_level = new IntegerR2kStruct(0);
    public IntegerR2kStruct hero_hp = new IntegerR2kStruct(0);
    public StringR2kStruct face1_name = new StringR2kStruct();
    public IntegerR2kStruct face1_id = new IntegerR2kStruct(0);
    public StringR2kStruct face2_name = new StringR2kStruct();
    public IntegerR2kStruct face2_id = new IntegerR2kStruct(0);
    public StringR2kStruct face3_name = new StringR2kStruct();
    public IntegerR2kStruct face3_id = new IntegerR2kStruct(0);
    public StringR2kStruct face4_name = new StringR2kStruct();
    public IntegerR2kStruct face4_id = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, timestamp, "@timestamp"),
                new Index(0x0B, hero_name, "@hero_name"),
                new Index(0x0C, hero_level, "@hero_level"),
                new Index(0x0D, hero_hp, "@hero_hp"),
                new Index(0x15, face1_name, "@face1_name"),
                new Index(0x16, face1_id, "@face1_id"),
                new Index(0x17, face2_name, "@face2_name"),
                new Index(0x18, face2_id, "@face2_id"),
                new Index(0x19, face3_name, "@face3_name"),
                new Index(0x1A, face3_id, "@face3_id"),
                new Index(0x1B, face4_name, "@face4_name"),
                new Index(0x1C, face4_id, "@face4_id"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveTitle", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

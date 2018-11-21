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
    public StringR2kStruct heroName = new StringR2kStruct();
    public IntegerR2kStruct heroLevel = new IntegerR2kStruct(0);
    public IntegerR2kStruct heroHp = new IntegerR2kStruct(0);
    public StringR2kStruct face1Name = new StringR2kStruct();
    public IntegerR2kStruct face1Idx = new IntegerR2kStruct(0);
    public StringR2kStruct face2Name = new StringR2kStruct();
    public IntegerR2kStruct face2Idx = new IntegerR2kStruct(0);
    public StringR2kStruct face3Name = new StringR2kStruct();
    public IntegerR2kStruct face3Idx = new IntegerR2kStruct(0);
    public StringR2kStruct face4Name = new StringR2kStruct();
    public IntegerR2kStruct face4Idx = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, timestamp, "@timestamp"),
                new Index(0x0B, heroName, "@hero_name"),
                new Index(0x0C, heroLevel, "@hero_level"),
                new Index(0x0D, heroHp, "@hero_hp"),
                new Index(0x15, face1Name, "@face1_name"),
                new Index(0x16, face1Idx, "@face1_index"),
                new Index(0x17, face2Name, "@face2_name"),
                new Index(0x18, face2Idx, "@face2_index"),
                new Index(0x19, face3Name, "@face3_name"),
                new Index(0x1A, face3Idx, "@face3_index"),
                new Index(0x1B, face4Name, "@face4_name"),
                new Index(0x1C, face4Idx, "@face4_index"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveTitle", true);
        asRIOISF(rio);
        return rio;
    }
}

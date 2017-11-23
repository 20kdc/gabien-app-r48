/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

public class SaveTarget extends R2kObject {
    public IntegerR2kStruct map = new IntegerR2kStruct(0);
    public IntegerR2kStruct x = new IntegerR2kStruct(0);
    public IntegerR2kStruct y = new IntegerR2kStruct(0);
    public BooleanR2kStruct switchValid = new BooleanR2kStruct(false);
    public IntegerR2kStruct switchId = new IntegerR2kStruct(0);


    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, map, "@map"),
                new Index(0x02, x, "@x"),
                new Index(0x03, y, "@y"),
                new Index(0x04, switchValid, "@switch_valid"),
                new Index(0x05, switchId, "@switch_id"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveTarget", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Created on 05/06/17.
 */
public class Learning extends R2kObject {
    public IntegerR2kStruct level = new IntegerR2kStruct(1);
    public IntegerR2kStruct skill = new IntegerR2kStruct(1);

    // Skill = Blob;2b 01 04
    public boolean disableSanity() {
        return true;
    }

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, level, "@level"),
                new Index(0x02, skill, "@skill")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Learning", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

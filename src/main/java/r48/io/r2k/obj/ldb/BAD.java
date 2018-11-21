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
 * Battler Animation Data (used by Skill)
 * Created on 06/06/17.
 */
public class BAD extends R2kObject {

    public IntegerR2kStruct moveType = new IntegerR2kStruct(0);
    public IntegerR2kStruct aiType = new IntegerR2kStruct(0);
    public IntegerR2kStruct pose = new IntegerR2kStruct(-1);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x05, moveType, "@move_type"),
                new Index(0x06, aiType, "@has_afterimage"),
                new Index(0x0E, pose, "@pose"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::BattlerAnimationData", true);
        asRIOISF(rio);
        return rio;
    }
}

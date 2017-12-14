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
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;

/**
 * You know exactly why this inheritance is needed
 * Created on December 13th, 2017
 */
public class SaveMapEvent extends SaveCharacter {
    public BooleanR2kStruct running = new BooleanR2kStruct(false);
    public IntegerR2kStruct originalMoveRouteIndex = new IntegerR2kStruct(0);
    public BooleanR2kStruct pending = new BooleanR2kStruct(false);
    public Interpreter interpreter = new Interpreter();

    @Override
    public Index[] getIndices() {
        return R2kUtil.mergeIndices(super.getIndices(), new Index[] {
                new Index(0x65, running, "@running"),
                new Index(0x66, originalMoveRouteIndex, "@original_moveroute_index"),
                new Index(0x67, pending, "@pending"),
                new Index(0x6C, interpreter, "@interpreter"),
        });
    }

    @Override
    public RubyIO asRIO() {
        RubyIO root = new RubyIO().setSymlike("RPG::SaveMapEvent", true);
        asRIOISF(root);
        return root;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayAR2kStruct;

/**
 * Created on 07/06/17.
 */
public class AnimationFrame extends R2kObject {
    public SparseArrayAR2kStruct<AnimationCell> cells = new SparseArrayAR2kStruct<AnimationCell>(new ISupplier<AnimationCell>() {
        @Override
        public AnimationCell get() {
            return new AnimationCell();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, cells, "@cells")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Animation::Frame", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

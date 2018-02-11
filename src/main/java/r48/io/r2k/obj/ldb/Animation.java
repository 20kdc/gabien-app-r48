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
import r48.io.r2k.chunks.*;

/**
 * COPY jun6-2017
 */
public class Animation extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct animationName = new StringR2kStruct();
    public BooleanR2kStruct unknown3 = new BooleanR2kStruct(false) {
        @Override
        public RubyIO asRIO() {
            // This is to make up for a screwup with previous versions of R48
            return new RubyIO().setBool(i >= 0);
        }
    };
    public SparseArrayHR2kStruct<AnimationTiming> timings = new SparseArrayHR2kStruct<AnimationTiming>(new ISupplier<AnimationTiming>() {
        @Override
        public AnimationTiming get() {
            return new AnimationTiming();
        }
    });
    public IntegerR2kStruct scope = new IntegerR2kStruct(0);
    public IntegerR2kStruct position = new IntegerR2kStruct(2);
    public BlobR2kStruct frames = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[1];
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, animationName, "@animation_name"),
                new Index(0x03, unknown3, "@battle2_2k3"),
                new Index(0x06, timings, "@timings"),
                new Index(0x09, scope, "@scope"),
                new Index(0x0A, position, "@position"),
                new Index(0x0C, frames, "@frames"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Animation", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

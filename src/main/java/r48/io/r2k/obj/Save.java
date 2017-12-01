/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BlobR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayAR2kStruct;
import r48.io.r2k.chunks.SparseArrayHR2kStruct;
import r48.io.r2k.obj.lsd.*;

/**
 * Savefile
 */
public class Save extends R2kObject {
    public SaveTitle title = new SaveTitle();
    public SaveSystem system = new SaveSystem();
    public BlobR2kStruct screen = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct pictures = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct partyPos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct boatPos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct shipPos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct airshipPos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public SparseArrayHR2kStruct<SaveActor> actors = new SparseArrayHR2kStruct<SaveActor>(new ISupplier<SaveActor>() {
        @Override
        public SaveActor get() {
            return new SaveActor();
        }
    });
    public BlobR2kStruct partyItems = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public SparseArrayAR2kStruct<SaveTarget> targets = new SparseArrayAR2kStruct<SaveTarget>(new ISupplier<SaveTarget>() {
        @Override
        public SaveTarget get() {
            return new SaveTarget();
        }
    });
    public BlobR2kStruct mapInfo = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct panorama = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public Interpreter mainInterpreter = new Interpreter();
    public SparseArrayHR2kStruct<R2kObject> commonEvents = new SparseArrayHR2kStruct<R2kObject>(new ISupplier<R2kObject>() {
        @Override
        public R2kObject get() {
            return new R2kObject() {
                public Interpreter interp = new Interpreter();
                @Override
                public Index[] getIndices() {
                    return new Index[] {
                            new Index(0x01, interp, "@i")
                    };
                }

                @Override
                public RubyIO asRIO() {
                    RubyIO root = new RubyIO().setSymlike("RPG::SaveCommonEvent", true);
                    asRIOISF(root);
                    return root;
                }

                @Override
                public void fromRIO(RubyIO src) {
                    fromRIOISF(src);
                }
            };
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x64, title, "@title"),
                new Index(0x65, system, "@system"),
                new Index(0x66, screen, "@screen"),
                new Index(0x67, pictures, "@pictures"),
                new Index(0x68, partyPos, "@party_pos"),
                new Index(0x69, boatPos, "@boat_pos"),
                new Index(0x6A, shipPos, "@ship_pos"),
                new Index(0x6B, airshipPos, "@airship_pos"),
                new Index(0x6C, actors, "@actors"),
                new Index(0x6D, partyItems, "@party"),
                new Index(0x6E, targets, "@targets"),
                new Index(0x6F, mapInfo, "@map_info"),
                new Index(0x70, panorama, "@panorama"),
                new Index(0x71, mainInterpreter, "@main_interpreter"),
                new Index(0x72, commonEvents, "@common_events"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO root = new RubyIO().setSymlike("RPG::Save", true);
        asRIOISF(root);
        return root;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }

    @Override
    public boolean terminatable() {
        return true;
    }
}

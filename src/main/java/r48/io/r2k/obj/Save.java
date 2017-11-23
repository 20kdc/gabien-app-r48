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
import r48.io.r2k.chunks.IR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayHR2kStruct;
import r48.io.r2k.obj.lsd.SaveActor;
import r48.io.r2k.obj.lsd.SaveTitle;

/**
 * Savefile
 */
public class Save extends R2kObject {
    public SaveTitle title = new SaveTitle();
    public BlobR2kStruct system = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
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
    public BlobR2kStruct party_pos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct boat_pos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct ship_pos = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct airship_pos = new BlobR2kStruct(new ISupplier<byte[]>() {
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
    public BlobR2kStruct party_items = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct targets = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct map_info = new BlobR2kStruct(new ISupplier<byte[]>() {
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
    public BlobR2kStruct events = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });
    public BlobR2kStruct common_events = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            return new byte[0];
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x64, title, "@title"),
                new Index(0x65, system, "@system"),
                new Index(0x66, screen, "@screen"),
                new Index(0x67, pictures, "@pictures"),
                new Index(0x68, party_pos, "@party_pos"),
                new Index(0x69, boat_pos, "@boat_pos"),
                new Index(0x6A, ship_pos, "@ship_pos"),
                new Index(0x6B, airship_pos, "@airship_pos"),
                new Index(0x6C, actors, "@actors"),
                new Index(0x6D, party_items, "@party_items"),
                new Index(0x6E, targets, "@targets"),
                new Index(0x6F, map_info, "@map_info"),
                new Index(0x70, panorama, "@panorama"),
                new Index(0x71, events, "@events"),
                new Index(0x72, common_events, "@common_events"),
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

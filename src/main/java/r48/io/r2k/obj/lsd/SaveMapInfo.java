/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

/**
 * Created on December 13th, 2017
 */
public class SaveMapInfo extends R2kObject {
    public IntegerR2kStruct x = new IntegerR2kStruct(0);
    public IntegerR2kStruct y = new IntegerR2kStruct(0);
    // I seriously hope this is correct...
    public IntegerR2kStruct encounterRate = new IntegerR2kStruct(-1);
    public IntegerR2kStruct chipsetId = new IntegerR2kStruct(-1);
    public SparseArrayHR2kStruct<SaveMapEvent> events = new SparseArrayHR2kStruct<SaveMapEvent>(new ISupplier<SaveMapEvent>() {
        @Override
        public SaveMapEvent get() {
            return new SaveMapEvent();
        }
    });
    public BlobR2kStruct lowerTileRemap = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            byte[] blank = new byte[0x90];
            for (int i = 0; i < blank.length; i++)
                blank[i] = (byte) i;
            return blank;
        }
    });
    public BlobR2kStruct upperTileRemap = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            byte[] blank = new byte[0x90];
            for (int i = 0; i < blank.length; i++)
                blank[i] = (byte) i;
            return blank;
        }
    });
    public StringR2kStruct parallaxName = new StringR2kStruct();
    public BooleanR2kStruct parallaxLoopX = new BooleanR2kStruct(false);
    public BooleanR2kStruct parallaxLoopY = new BooleanR2kStruct(false);
    public BooleanR2kStruct parallaxLoopXAuto = new BooleanR2kStruct(false);
    public IntegerR2kStruct parallaxLoopXSpeed = new IntegerR2kStruct(0);
    public BooleanR2kStruct parallaxLoopYAuto = new BooleanR2kStruct(false);
    public IntegerR2kStruct parallaxLoopYSpeed = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, x, "@x"),
                new Index(0x02, y, "@y"),
                new Index(0x03, encounterRate, "@encounter_rate"),
                new Index(0x05, chipsetId, "@tileset_id"),
                new Index(0x0B, events, "@events"),
                new Index(0x15, lowerTileRemap),
                new Index(0x16, upperTileRemap),
                new Index(0x20, parallaxName, "@parallax_name"),
                new Index(0x21, parallaxLoopX, "@parallax_loop_x"),
                new Index(0x22, parallaxLoopY, "@parallax_loop_y"),
                new Index(0x23, parallaxLoopXAuto, "@parallax_autoloop_x"),
                new Index(0x24, parallaxLoopXSpeed, "@parallax_sx"),
                new Index(0x25, parallaxLoopYAuto, "@parallax_autoloop_y"),
                new Index(0x26, parallaxLoopYSpeed, "@parallax_sy"),
        };
    }

    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveMapInfo", true);
        asRIOISF(rio);
        rio.addIVar("@lower_tile_remap", bToTable(lowerTileRemap.dat));
        rio.addIVar("@upper_tile_remap", bToTable(upperTileRemap.dat));
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
        lowerTileRemap.dat = bFromTable(src.getInstVarBySymbol("@lower_tile_remap"));
        upperTileRemap.dat = bFromTable(src.getInstVarBySymbol("@upper_tile_remap"));
    }

    private RubyIO bToTable(byte[] dat) {
        RubyTable rt = new RubyTable(3, dat.length, 1, 1, new int[] {0});
        for (int i = 0; i < dat.length; i++)
            rt.setTiletype(i, 0, 0, (short) (dat[i] & 0xFF));
        return new RubyIO().setUser("Table", rt.innerBytes);
    }

    private byte[] bFromTable(RubyIO instVarBySymbol) {
        RubyTable rt = new RubyTable(instVarBySymbol.userVal);
        byte[] data = new byte[rt.width];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) rt.getTiletype(i, 0, 0);
        return data;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;

/**
 * Another bare-minimum for now
 * Created on 01/06/17.
 */
public class Tileset extends R2kObject {

    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct tilesetName = new StringR2kStruct();
    // Tables? Tables. Don't need to put these as optional explicitly because not Index-based.
    // ...and anyway, I get the feeling they aren't actually SUPPOSED to be optional.
    // excuse me while I go mess around with the schema.
    public BlobR2kStruct terrainTbl = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            byte[] dat = new byte[324];
            for (int i = 0; i < dat.length; i += 2)
                dat[i] = 1;
            return dat;
        }
    });
    public BlobR2kStruct lowPassTbl = new BlobR2kStruct(R2kUtil.supplyBlank(162, (byte) 15));
    public BlobR2kStruct highPassTbl = new BlobR2kStruct(new ISupplier<byte[]>() {
        @Override
        public byte[] get() {
            byte[] dat = new byte[144];
            for (int i = 0; i < dat.length; i++)
                dat[i] = 15;
            dat[0] = 31;
            return dat;
        }
    });

    public BooleanR2kStruct animCyclic = new BooleanR2kStruct(false);
    public IntegerR2kStruct animSpeed = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, tilesetName, "@tileset_name"),
                new Index(0x03, terrainTbl),
                new Index(0x04, lowPassTbl),
                new Index(0x05, highPassTbl),
                new Index(0x0B, animCyclic, "@anim_cyclic"),
                new Index(0x0C, animSpeed, "@anim_speed")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Tileset", true);
        asRIOISF(mt);

        // 162 = 144 (selective) + 18 (AT Field????)
        RubyTable rt = new RubyTable(3, 162, 1, 1, new int[] {0});
        System.arraycopy(terrainTbl.dat, 0, rt.innerBytes, 20, terrainTbl.dat.length);
        mt.addIVar("@terrain_id_data", new RubyIO().setUser("Table", rt.innerBytes));

        mt.addIVar("@lowpass_data", bitfieldTable(lowPassTbl.dat));
        mt.addIVar("@highpass_data", bitfieldTable(highPassTbl.dat));
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
        RubyIO c = src.getInstVarBySymbol("@terrain_id_data");
        terrainTbl.dat = new byte[324];
        System.arraycopy(c.userVal, 20, terrainTbl.dat, 0, terrainTbl.dat.length);
        lowPassTbl.dat = bitfieldTableRV(src.getInstVarBySymbol("@lowpass_data"));
        highPassTbl.dat = bitfieldTableRV(src.getInstVarBySymbol("@highpass_data"));
    }

    private RubyIO bitfieldTable(byte[] dat) {
        RubyTable rt = new RubyTable(3, dat.length, 1, 1, new int[] {0});
        for (int i = 0; i < dat.length; i++)
            rt.innerBytes[20 + (i * 2)] = dat[i];
        return new RubyIO().setUser("Table", rt.innerBytes);
    }

    private byte[] bitfieldTableRV(RubyIO src) {
        RubyTable rt = new RubyTable(src.userVal);
        byte[] r = new byte[rt.width];
        for (int i = 0; i < r.length; i++)
            r[i] = (byte) rt.getTiletype(i, 0, 0);
        return r;
    }
}

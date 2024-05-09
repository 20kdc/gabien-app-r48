/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyTable;
import r48.RubyTableR;
import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.BlobR2kStruct;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

import gabien.uslx.io.ByteArrayMemoryish;
import gabien.uslx.io.MemoryishR;

/**
 * Another bare-minimum for now
 * Created on 01/06/17.
 */
public class Tileset extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(0x01) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@tileset_name") @DM2LcfBinding(0x02) @DMCXObject
    public StringR2kStruct tilesetName;
    // Tables? Tables. Don't need to put these as optional explicitly because not Index-based.
    // ...and anyway, I get the feeling they aren't actually SUPPOSED to be optional.
    // excuse me while I go mess around with the schema.
    // -- AS OF DM2, some blob transformation occurs similar to SaveMapInfo.
    @DMFXOBinding("@terrain_id_data") @DM2LcfBinding(3)
    public BlobR2kStruct terrainTbl;
    public static Consumer<Tileset> terrainTbl_add = (v) -> v.terrainTbl = new BlobR2kStruct(v.context, "Table", RubyTable.initNewTable(3, 162, 1, 1, new int[] {1}).data);
    @DMFXOBinding("@lowpass_data") @DM2LcfBinding(4)
    public BlobR2kStruct lowPassTbl;
    public static Consumer<Tileset> lowPassTbl_add = (v) -> v.lowPassTbl = new BlobR2kStruct(v.context, "Table", v.bitfieldsToTable(R2kUtil.supplyBlank(162, (byte) 15).get()));
    @DMFXOBinding("@highpass_data") @DM2LcfBinding(5)
    public BlobR2kStruct highPassTbl;
    public static Consumer<Tileset> highPassTbl_add = (v) -> {
        byte[] dat = new byte[144];
        for (int i = 0; i < dat.length; i++)
            dat[i] = 15;
        dat[0] = 31;
        v.highPassTbl = new BlobR2kStruct(v.context, "Table", v.bitfieldsToTable(dat));
    };

    @DMFXOBinding("@anim_cyclic") @DM2LcfBinding(0x0B) @DMCXBoolean(false)
    public BooleanR2kStruct animCyclic;
    @DMFXOBinding("@anim_speed") @DM2LcfBinding(0x0C) @DMCXInteger(0)
    public IntegerR2kStruct animSpeed;

    public Tileset(DMContext ctx) {
        super(ctx, "RPG::Tileset");
    }

    @Override
    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        byte[] uv = pcd.get(3);
        if (uv != null) {
            // 162 = 144 (selective) + 18 (AT Field????)
            ByteArrayMemoryish rt = RubyTable.initNewTable(3, 162, 1, 1, new int[] {0});
            // This relies on RubyTable layout to skip some relayout
            System.arraycopy(uv, 0, rt.data, 20, Math.min(uv.length, rt.data.length - 20));
            pcd.put(3, rt.data);
        }
        uv = pcd.get(4);
        if (uv != null)
            pcd.put(4, bitfieldsToTable(uv));
        uv = pcd.get(5);
        if (uv != null)
            pcd.put(5, bitfieldsToTable(uv));
        super.dm2UnpackFromMapDestructively(pcd);
    }

    @Override
    protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
        super.dm2PackIntoMap(pcd);
        byte[] uv = new byte[324];
        // This relies on RubyTable layout to skip some relayout
        MemoryishR terrainBuf = terrainTbl.getBuffer();
        terrainBuf.getBulk(20, uv, 0, Math.min(uv.length, (int) terrainBuf.length - 20));
        pcd.put(3, uv);
        pcd.put(4, tableToBitfields(lowPassTbl.getBufferCopy()));
        pcd.put(5, tableToBitfields(highPassTbl.getBufferCopy()));
    }

    private byte[] bitfieldsToTable(byte[] dat) {
        ByteArrayMemoryish rt = RubyTable.initNewTable(3, dat.length, 1, 1, new int[] {0});
        for (int i = 0; i < dat.length; i++)
            rt.data[20 + (i * 2)] = dat[i];
        return rt.data;
    }

    private byte[] tableToBitfields(byte[] src) {
        RubyTableR rt = new RubyTableR(src);
        byte[] r = new byte[rt.width];
        for (int i = 0; i < r.length; i++)
            r[i] = (byte) rt.getTiletype(i, 0, 0);
        return r;
    }
}

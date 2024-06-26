/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyTable;
import r48.RubyTableR;
import r48.io.data.DMContext;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
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
 * Created on December 13th, 2017
 */
public class SaveMapInfo extends DM2R2kObject {
    @DMFXOBinding("@x") @DM2LcfBinding(0x01) @DMCXInteger(0)
    public IntegerR2kStruct x;
    @DMFXOBinding("@y") @DM2LcfBinding(0x02) @DMCXInteger(0)
    public IntegerR2kStruct y;
    // I seriously hope this is correct...
    @DMFXOBinding("@encounter_rate") @DM2LcfBinding(0x03) @DMCXInteger(-1)
    public IntegerR2kStruct encounterRate;
    @DMFXOBinding("@tileset_id") @DM2LcfBinding(0x05) @DMCXInteger(-1)
    public IntegerR2kStruct chipsetId;
    @DMFXOBinding("@events") @DM2LcfBinding(0x0B) @DMCXSupplier(SaveMapEvent.class)
    public DM2SparseArrayH<SaveMapEvent> events;

    // Transforms are performed on the LCF data before unpack.
    @DMFXOBinding("@lower_tile_remap") @DM2LcfBinding(0x15)
    public BlobR2kStruct lowerTileRemap;
    public static Consumer<SaveMapInfo> lowerTileRemap_add = (v) -> v.lowerTileRemap = v.newBlankRemap();
    @DMFXOBinding("@upper_tile_remap") @DM2LcfBinding(0x16)
    public BlobR2kStruct upperTileRemap;
    public static Consumer<SaveMapInfo> upperTileRemap_add = (v) -> v.upperTileRemap = v.newBlankRemap();

    @DMFXOBinding("@parallax_name") @DM2LcfBinding(0x20) @DMCXObject
    public StringR2kStruct parallaxName;
    @DMFXOBinding("@parallax_loop_x") @DM2LcfBinding(0x21) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopX;
    @DMFXOBinding("@parallax_loop_y") @DM2LcfBinding(0x22) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopY;
    @DMFXOBinding("@parallax_autoloop_x") @DM2LcfBinding(0x23) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopXAuto;
    @DMFXOBinding("@parallax_sx") @DM2LcfBinding(0x24) @DMCXInteger(0)
    public IntegerR2kStruct parallaxLoopXSpeed;
    @DMFXOBinding("@parallax_autoloop_y") @DM2LcfBinding(0x25) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopYAuto;
    @DMFXOBinding("@parallax_sy") @DM2LcfBinding(0x26) @DMCXInteger(0)
    public IntegerR2kStruct parallaxLoopYSpeed;

    public SaveMapInfo(DMContext ctx) {
        super(ctx, "RPG::SaveMapInfo");
    }

    private BlobR2kStruct newBlankRemap() {
        byte[] blank = new byte[0x90];
        for (int i = 0; i < blank.length; i++)
            blank[i] = (byte) i;
        return new BlobR2kStruct(context, "Table", bToTable(blank));
    }

    @Override
    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        byte[] lowerMap = pcd.get(0x15);
        if (lowerMap != null)
            pcd.put(0x15, bToTable(lowerMap));
        byte[] upperMap = pcd.get(0x16);
        if (upperMap != null)
            pcd.put(0x16, bToTable(upperMap));
        super.dm2UnpackFromMapDestructively(pcd);
    }

    @Override
    protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
        // This stores the wrong data for the blobs...
        super.dm2PackIntoMap(pcd);
        // This stores the correct data for the blobs.
        pcd.put(0x15, bFromTable(lowerTileRemap.getBuffer()));
        pcd.put(0x16, bFromTable(upperTileRemap.getBuffer()));
    }

    private byte[] bToTable(byte[] dat) {
        ByteArrayMemoryish bam = RubyTable.initNewTable(3, dat.length, 1, 1, new int[] {0});
        RubyTable rt = new RubyTable(bam);
        for (int i = 0; i < dat.length; i++)
            rt.setTiletype(i, 0, 0, (short) (dat[i] & 0xFF));
        return bam.data;
    }

    private byte[] bFromTable(MemoryishR instVarBySymbol) {
        RubyTableR rt = new RubyTableR(instVarBySymbol);
        byte[] data = new byte[rt.width];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) rt.getTiletype(i, 0, 0);
        return data;
    }
}

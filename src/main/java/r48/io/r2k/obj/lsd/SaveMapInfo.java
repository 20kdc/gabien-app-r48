/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.RubyTable;
import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BlobR2kStruct;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created on December 13th, 2017
 */
public class SaveMapInfo extends DM2R2kObject {
    @DM2FXOBinding("@x") @DM2LcfBinding(0x01) @DM2LcfInteger(0)
    public IntegerR2kStruct x;
    @DM2FXOBinding("@y") @DM2LcfBinding(0x02) @DM2LcfInteger(0)
    public IntegerR2kStruct y;
    // I seriously hope this is correct...
    @DM2FXOBinding("@encounter_rate") @DM2LcfBinding(0x03) @DM2LcfInteger(-1)
    public IntegerR2kStruct encounterRate;
    @DM2FXOBinding("@tileset_id") @DM2LcfBinding(0x05) @DM2LcfInteger(-1)
    public IntegerR2kStruct chipsetId;
    @DM2FXOBinding("@events") @DM2LcfBinding(0x0B) @DM2LcfSparseArray(SaveMapEvent.class)
    public DM2SparseArrayH<SaveMapEvent> events;

    // Transforms are performed on the LCF data before unpack.
    @DM2FXOBinding("@lower_tile_remap") @DM2LcfBinding(0x15)
    public BlobR2kStruct lowerTileRemap;
    @DM2FXOBinding("@upper_tile_remap") @DM2LcfBinding(0x16)
    public BlobR2kStruct upperTileRemap;

    @DM2FXOBinding("@parallax_name") @DM2LcfBinding(0x20) @DM2LcfObject
    public StringR2kStruct parallaxName;
    @DM2FXOBinding("@parallax_loop_x") @DM2LcfBinding(0x21) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxLoopX;
    @DM2FXOBinding("@parallax_loop_y") @DM2LcfBinding(0x22) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxLoopY;
    @DM2FXOBinding("@parallax_autoloop_x") @DM2LcfBinding(0x23) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxLoopXAuto;
    @DM2FXOBinding("@parallax_sx") @DM2LcfBinding(0x24) @DM2LcfInteger(0)
    public IntegerR2kStruct parallaxLoopXSpeed;
    @DM2FXOBinding("@parallax_autoloop_y") @DM2LcfBinding(0x25) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxLoopYAuto;
    @DM2FXOBinding("@parallax_sy") @DM2LcfBinding(0x26) @DM2LcfInteger(0)
    public IntegerR2kStruct parallaxLoopYSpeed;

    public SaveMapInfo() {
        super("RPG::SaveMapInfo");
    }

    private BlobR2kStruct newBlankRemap() {
        byte[] blank = new byte[0x90];
        for (int i = 0; i < blank.length; i++)
            blank[i] = (byte) i;
        return new BlobR2kStruct("Table", bToTable(blank));
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@lower_tile_remap"))
            return lowerTileRemap = newBlankRemap();
        if (sym.equals("@upper_tile_remap"))
            return upperTileRemap = newBlankRemap();
        return super.dm2AddIVar(sym);
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
        pcd.put(0x15, bFromTable(lowerTileRemap.userVal));
        pcd.put(0x16, bFromTable(upperTileRemap.userVal));
    }

    private byte[] bToTable(byte[] dat) {
        RubyTable rt = new RubyTable(3, dat.length, 1, 1, new int[] {0});
        for (int i = 0; i < dat.length; i++)
            rt.setTiletype(i, 0, 0, (short) (dat[i] & 0xFF));
        return rt.innerBytes;
    }

    private byte[] bFromTable(byte[] instVarBySymbol) {
        RubyTable rt = new RubyTable(instVarBySymbol);
        byte[] data = new byte[rt.width];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) rt.getTiletype(i, 0, 0);
        return data;
    }
}

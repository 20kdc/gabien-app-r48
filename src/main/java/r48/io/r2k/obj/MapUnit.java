/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyTable;
import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedUser;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;
import r48.io.r2k.dm2chk.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * First test of the new system for figuring out what the hell to do with this alien format
 * Writing this here for lack of a better place:
 * Unknown attributes are going to get stuffed into some "@unknownMeta" place,
 * containing raw blobs of whatever.
 * Created on 31/05/17.
 */
public class MapUnit extends DM2R2kObject {
    @DM2FXOBinding(optional = false, iVar = "@tileset_id") @DM2LcfBinding(index = 1) @DM2LcfInteger(1)
    public IntegerR2kStruct tilesetId;
    @DM2FXOBinding(optional = false, iVar = "@width") @DM2LcfBinding(index = 2) @DM2LcfInteger(20)
    public IntegerR2kStruct width;
    @DM2FXOBinding(optional = false, iVar = "@height") @DM2LcfBinding(index = 3) @DM2LcfInteger(15)
    public IntegerR2kStruct height;
    @DM2FXOBinding(optional = false, iVar = "@scroll_type") @DM2LcfBinding(index = 11) @DM2LcfInteger(0)
    public IntegerR2kStruct scrollType;
    @DM2FXOBinding(optional = false, iVar = "@parallax_flag") @DM2LcfBinding(index = 31) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxFlag;
    @DM2FXOBinding(optional = false, iVar = "@parallax_name") @DM2LcfBinding(index = 32) @DM2LcfString()
    public StringR2kStruct parallaxName;
    @DM2FXOBinding(optional = false, iVar = "@parallax_loop_x") @DM2LcfBinding(index = 33) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxLoopX;
    @DM2FXOBinding(optional = false, iVar = "@parallax_loop_y") @DM2LcfBinding(index = 34) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxLoopY;
    @DM2FXOBinding(optional = false, iVar = "@parallax_autoloop_x") @DM2LcfBinding(index = 35) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxAutoloopX;
    @DM2FXOBinding(optional = false, iVar = "@parallax_sx") @DM2LcfBinding(index = 36) @DM2LcfInteger(0)
    public IntegerR2kStruct parallaxSX;
    @DM2FXOBinding(optional = false, iVar = "@parallax_autoloop_y") @DM2LcfBinding(index = 37) @DM2LcfBoolean(false)
    public BooleanR2kStruct parallaxAutoloopY;
    @DM2FXOBinding(optional = false, iVar = "@parallax_sy") @DM2LcfBinding(index = 38) @DM2LcfInteger(0)
    public IntegerR2kStruct parallaxSY;
    @DM2FXOBinding(optional = false, iVar = "@top_level") @DM2LcfBinding(index = 42) @DM2LcfBoolean(false)
    public BooleanR2kStruct topLevel;

    // This is where things get interesting. During unpack & repack, these are initialized and destroyed.
    @DM2LcfBinding(index = 71)
    public BlobR2kStruct layer0;
    @DM2LcfBinding(index = 72)
    public BlobR2kStruct layer1;

    // And this is the actual FXO side.
    @DM2FXOBinding(optional = false, iVar = "@data")
    public IRIOFixedUser map;

    // Remaining things are back to normal.
    @DM2FXOBinding(optional = false, iVar = "@events") @DM2LcfBinding(index = 81)
    public DM2SparseArrayH<Event> events;
    @DM2FXOBinding(optional = true, iVar = "@save_count_2k3en") @DM2LcfBinding(index = 90) @DM2LcfInteger(0)
    public IntegerR2kStruct magicNumberA;
    @DM2FXOBinding(optional = true, iVar = "@save_count_2k3en") @DM2LcfBinding(index = 91) @DM2LcfInteger(0)
    public IntegerR2kStruct magicNumberB;


    public MapUnit() {
        super("RPG::Map");
    }

    @Override
    protected IR2kInterpretable dm2ReinitializeBound(Field f, boolean present) {
        if (f.getName().equals("layer0"))
            return layer0 = new BlobR2kStruct(R2kUtil.supplyBlank(20 * 15 * 2, (byte) 0));
        if (f.getName().equals("layer1"))
            return layer1 = new BlobR2kStruct(R2kUtil.supplyBlank(20 * 15 * 2, (byte) 0));
        return super.dm2ReinitializeBound(f, present);
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@events"))
            return events = new DM2SparseArrayH<Event>(new ISupplier<Event>() {
                @Override
                public Event get() {
                    return new Event();
                }
            });
        return super.dm2AddIVar(sym);
    }

    @Override
    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        // This creates layer0/layer1 in the process, which must be nulled.
        super.dm2UnpackFromMapDestructively(pcd);
        makeLmuData();
        layer0 = null;
        layer1 = null;
    }

    @Override
    protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
        // Creates layer0/layer1
        decLmuData();
        super.dm2PackIntoMap(pcd);
        layer0 = null;
        layer1 = null;
    }

    private void decLmuData() {
        // Assuming all consistency is fine, width/height are fine too
        layer0 = new BlobR2kStruct(new byte[width.i * height.i * 2]);
        layer1 = new BlobR2kStruct(new byte[width.i * height.i * 2]);
        byte[] innerBytes = map.getBuffer();
        System.arraycopy(innerBytes, 20, layer0.userVal, 0, layer0.userVal.length);
        System.arraycopy(innerBytes, 20 + (width.i * height.i * 2), layer1.userVal, 0, layer1.userVal.length);
    }

    private void makeLmuData() {
        // -- transform the lower-layer and upper-layer data...
        RubyTable rt = new RubyTable(3, width.i, height.i, 2, new int[] {0, 0});
        System.arraycopy(layer0.userVal, 0, rt.innerBytes, 20, layer0.userVal.length);
        System.arraycopy(layer1.userVal, 0, rt.innerBytes, 20 + (width.i * height.i * 2), layer1.userVal.length);
        map = new IRIOFixedUser("Table", rt.innerBytes);
    }
}

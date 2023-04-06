/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.RubyTable;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedUser;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
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
    @DM2FXOBinding("@tileset_id") @DM2LcfBinding(1) @DMCXInteger(1)
    public IntegerR2kStruct tilesetId;
    @DM2FXOBinding("@width") @DM2LcfBinding(2) @DMCXInteger(20)
    public IntegerR2kStruct width;
    @DM2FXOBinding("@height") @DM2LcfBinding(3) @DMCXInteger(15)
    public IntegerR2kStruct height;
    @DM2FXOBinding("@scroll_type") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct scrollType;
    @DM2FXOBinding("@parallax_flag") @DM2LcfBinding(31) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxFlag;
    @DM2FXOBinding("@parallax_name") @DM2LcfBinding(32) @DMCXObject
    public StringR2kStruct parallaxName;
    @DM2FXOBinding("@parallax_loop_x") @DM2LcfBinding(33) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopX;
    @DM2FXOBinding("@parallax_loop_y") @DM2LcfBinding(34) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopY;
    @DM2FXOBinding("@parallax_autoloop_x") @DM2LcfBinding(35) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxAutoloopX;
    @DM2FXOBinding("@parallax_sx") @DM2LcfBinding(36) @DMCXInteger(0)
    public IntegerR2kStruct parallaxSX;
    @DM2FXOBinding("@parallax_autoloop_y") @DM2LcfBinding(37) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxAutoloopY;
    @DM2FXOBinding("@parallax_sy") @DM2LcfBinding(38) @DMCXInteger(0)
    public IntegerR2kStruct parallaxSY;
    @DM2FXOBinding("@top_level") @DM2LcfBinding(42) @DMCXBoolean(false)
    public BooleanR2kStruct topLevel;

    // This is where things get interesting. During unpack & repack, these are initialized and destroyed.
    @DM2LcfBinding(71)
    public BlobR2kStruct layer0;
    @DM2LcfBinding(72)
    public BlobR2kStruct layer1;

    // And this is the actual FXO side.
    @DM2FXOBinding("@data")
    public IRIOFixedUser map;

    // Remaining things are back to normal.
    @DM2FXOBinding("@events") @DM2LcfBinding(81)
    public DM2SparseArrayH<Event> events;
    @DM2Optional @DM2FXOBinding("@save_count_2k3en") @DM2LcfBinding(90) @DMCXInteger(0)
    public IntegerR2kStruct magicNumberA;
    @DM2Optional @DM2FXOBinding("@save_count_other") @DM2LcfBinding(91) @DMCXInteger(0)
    public IntegerR2kStruct magicNumberB;


    public MapUnit(DM2Context ctx) {
        super(ctx, "RPG::Map");
    }

    @Override
    protected Object dm2AddField(Field f) {
        if (f.getName().equals("layer0"))
            return layer0 = new BlobR2kStruct(R2kUtil.supplyBlank(20 * 15 * 2, (byte) 0));
        if (f.getName().equals("layer1"))
            return layer1 = new BlobR2kStruct(R2kUtil.supplyBlank(20 * 15 * 2, (byte) 0));
        return super.dm2AddField(f);
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@data"))
            return map = new IRIOFixedUser("Table", new RubyTable(3, 20, 15, 2, new int[] {0, 0}).innerBytes);
        if (sym.equals("@events"))
            return events = new DM2SparseArrayH<Event>(() -> new Event(context));
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

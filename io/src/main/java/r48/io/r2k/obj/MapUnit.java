/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import r48.RubyTable;
import r48.io.data.DMContext;
import r48.io.data.IRIOFixedUser;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.data.obj.DMCXSupplier;
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
 * First test of the new system for figuring out what the hell to do with this alien format
 * Writing this here for lack of a better place:
 * Unknown attributes are going to get stuffed into some "@unknownMeta" place,
 * containing raw blobs of whatever.
 * Created on 31/05/17.
 */
public class MapUnit extends DM2R2kObject {
    @DMFXOBinding("@tileset_id") @DM2LcfBinding(1) @DMCXInteger(1)
    public IntegerR2kStruct tilesetId;
    @DMFXOBinding("@width") @DM2LcfBinding(2) @DMCXInteger(20)
    public IntegerR2kStruct width;
    @DMFXOBinding("@height") @DM2LcfBinding(3) @DMCXInteger(15)
    public IntegerR2kStruct height;
    @DMFXOBinding("@scroll_type") @DM2LcfBinding(11) @DMCXInteger(0)
    public IntegerR2kStruct scrollType;
    @DMFXOBinding("@parallax_flag") @DM2LcfBinding(31) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxFlag;
    @DMFXOBinding("@parallax_name") @DM2LcfBinding(32) @DMCXObject
    public StringR2kStruct parallaxName;
    @DMFXOBinding("@parallax_loop_x") @DM2LcfBinding(33) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopX;
    @DMFXOBinding("@parallax_loop_y") @DM2LcfBinding(34) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxLoopY;
    @DMFXOBinding("@parallax_autoloop_x") @DM2LcfBinding(35) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxAutoloopX;
    @DMFXOBinding("@parallax_sx") @DM2LcfBinding(36) @DMCXInteger(0)
    public IntegerR2kStruct parallaxSX;
    @DMFXOBinding("@parallax_autoloop_y") @DM2LcfBinding(37) @DMCXBoolean(false)
    public BooleanR2kStruct parallaxAutoloopY;
    @DMFXOBinding("@parallax_sy") @DM2LcfBinding(38) @DMCXInteger(0)
    public IntegerR2kStruct parallaxSY;
    @DMFXOBinding("@top_level") @DM2LcfBinding(42) @DMCXBoolean(false)
    public BooleanR2kStruct topLevel;

    // 71/72 handled by custom handlers

    // And this is the actual FXO side.
    @DMFXOBinding("@data")
    public IRIOFixedUser map;
    public static Consumer<MapUnit> map_add = (v) -> v.map = new IRIOFixedUser(v.context, "Table", RubyTable.initNewTable(3, 20, 15, 2, new int[] {0, 0}).data);

    // Remaining things are back to normal.
    @DMFXOBinding("@events") @DM2LcfBinding(81) @DMCXSupplier(Event.class)
    public DM2SparseArrayH<Event> events;
    @DMOptional @DMFXOBinding("@save_count_2k3en") @DM2LcfBinding(90) @DMCXInteger(0)
    public IntegerR2kStruct magicNumberA;
    @DMOptional @DMFXOBinding("@save_count_other") @DM2LcfBinding(91) @DMCXInteger(0)
    public IntegerR2kStruct magicNumberB;


    public MapUnit(DMContext ctx) {
        super(ctx, "RPG::Map");
    }

    @Override
    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        super.dm2UnpackFromMapDestructively(pcd);
        // Manually deserialize the layer data because of the transform involved here.
        byte[] layer0 = pcd.remove(71);
        byte[] layer1 = pcd.remove(72);
        if (layer0 == null)
            layer0 = new byte[20 * 15 * 2];
        if (layer1 == null)
            layer1 = new byte[20 * 15 * 2];
        // -- transform the lower-layer and upper-layer data...
        int w = (int) width.getFX();
        int h = (int) height.getFX();
        ByteArrayMemoryish bam = RubyTable.initNewTable(3, w, h, 2, new int[] {0, 0});
        System.arraycopy(layer0, 0, bam.data, 20, layer0.length);
        System.arraycopy(layer1, 0, bam.data, 20 + (w * h * 2), layer1.length);
        map = new IRIOFixedUser(context, "Table", bam.data);
    }

    @Override
    protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
        super.dm2PackIntoMap(pcd);
        // Assuming all consistency is fine, width/height are fine too
        int w = (int) width.getFX();
        int h = (int) height.getFX();
        byte[] layer0 = new byte[w * h * 2];
        byte[] layer1 = new byte[w * h * 2];
        MemoryishR innerBytes = map.getBuffer();
        innerBytes.getBulk(20, layer0, 0, layer0.length);
        innerBytes.getBulk(20 + (w * h * 2), layer1, 0, layer1.length);
        pcd.put(71, layer0);
        pcd.put(72, layer1);
    }
}

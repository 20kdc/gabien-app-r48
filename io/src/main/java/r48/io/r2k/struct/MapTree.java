/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.data.*;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.data.obj.DMCXSupplier;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2SparseArrayH;
import r48.io.r2k.obj.MapInfo;
import r48.io.r2k.obj.MapTreeStart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * This isn't even standard LCF madness. This is something *else*.
 * Created on 31/05/17.
 */
public class MapTree extends IRIOFixedObject implements IR2kInterpretable {
    @DMFXOBinding("@map_infos") @DMCXSupplier(MapInfo.class)
    public DM2SparseArrayH<MapInfo> mapInfos;
    @DMFXOBinding("@map_order")
    public IRIOFixedArray<IRIOFixnum> mapOrder;
    public static Consumer<MapTree> mapOrder_add = (v) -> v.mapOrder = new IRIOFixedArray<IRIOFixnum>(v.context) {
        @Override
        public IRIOFixnum newValue() {
            return new IRIOFixnum(v.context, 0);
        }
    };
    @DMFXOBinding("@active_node") @DMCXInteger(0)
    public IntegerR2kStruct activeNode;
    @DMFXOBinding("@start") @DMCXObject
    public MapTreeStart start;

    public MapTree(DMContext ctx) {
        super(ctx, "RPG::MapTree");
    }

    @Override
    public void importData(InputStream fis) throws IOException {
        // Sparse list
        mapInfos.importData(fis);
        // Non-sparse list
        mapOrder.arrVal = new IRIO[R2kUtil.readLcfVLI(fis)];
        for (int i = 0; i < mapOrder.arrVal.length; i++)
            mapOrder.arrVal[i] = new IRIOFixnum(context, R2kUtil.readLcfVLI(fis));
        activeNode.importData(fis);
        start = new MapTreeStart(context);
        start.importData(fis);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        mapInfos.exportData(baos);
        // the rest of it
        R2kUtil.writeLcfVLI(baos, mapOrder.arrVal.length);
        for (int i = 0; i < mapOrder.arrVal.length; i++)
            R2kUtil.writeLcfVLI(baos, (int) ((IRIOFixnum) mapOrder.arrVal[i]).getFX());
        activeNode.exportData(baos);
        start.exportData(baos);
    }
}

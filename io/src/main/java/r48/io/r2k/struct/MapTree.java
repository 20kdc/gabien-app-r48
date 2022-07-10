/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import gabien.uslx.append.*;
import r48.io.data.*;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.dm2chk.DM2SparseArrayH;
import r48.io.r2k.obj.MapInfo;
import r48.io.r2k.obj.MapTreeStart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This isn't even standard LCF madness. This is something *else*.
 * Created on 31/05/17.
 */
public class MapTree extends IRIOFixedObject implements IR2kInterpretable {
    @DM2FXOBinding("@map_infos")
    public DM2SparseArrayH<MapInfo> mapInfos;
    @DM2FXOBinding("@map_order")
    public IRIOFixedArray<IRIOFixnum> mapOrder;
    @DM2FXOBinding("@active_node")
    public IntegerR2kStruct activeNode;
    @DM2FXOBinding("@start")
    public MapTreeStart start;

    public MapTree() {
        super("RPG::MapTree");
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@map_infos"))
            return mapInfos = new DM2SparseArrayH<MapInfo>(new ISupplier<MapInfo>() {
                @Override
                public MapInfo get() {
                    return new MapInfo();
                }
            });
        if (sym.equals("@map_order"))
            return mapOrder = new IRIOFixedArray<IRIOFixnum>() {
                @Override
                public IRIOFixnum newValue() {
                    return new IRIOFixnum(0);
                }
            };
        if (sym.equals("@active_node"))
            return activeNode = new IntegerR2kStruct(0);
        if (sym.equals("@start"))
            return start = new MapTreeStart();
        return null;
    }

    @Override
    public void importData(InputStream fis) throws IOException {
        // Sparse list
        mapInfos.importData(fis);
        // Non-sparse list
        mapOrder.arrVal = new IRIO[R2kUtil.readLcfVLI(fis)];
        for (int i = 0; i < mapOrder.arrVal.length; i++)
            mapOrder.arrVal[i] = new IRIOFixnum(R2kUtil.readLcfVLI(fis));
        activeNode.importData(fis);
        start = new MapTreeStart();
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
            R2kUtil.writeLcfVLI(baos, (int) ((IRIOFixnum) mapOrder.arrVal[i]).val);
        activeNode.exportData(baos);
        start.exportData(baos);
    }
}

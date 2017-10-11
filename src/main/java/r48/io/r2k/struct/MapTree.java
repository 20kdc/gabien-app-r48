/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kStruct;
import r48.io.r2k.chunks.SparseArrayHR2kStruct;
import r48.io.r2k.obj.MapInfo;
import r48.io.r2k.obj.MapTreeStart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This isn't even standard LCF madness. This is something *else*.
 * Created on 31/05/17.
 */
public class MapTree implements IR2kStruct {
    public SparseArrayHR2kStruct<MapInfo> mapInfos = new SparseArrayHR2kStruct<MapInfo>(new ISupplier<MapInfo>() {
        @Override
        public MapInfo get() {
            return new MapInfo();
        }
    });
    public int[] mapOrder;
    public int activeNode;
    public MapTreeStart start;

    @Override
    public void importData(InputStream fis) throws IOException {
        // Sparse list
        mapInfos.map.clear();
        int len = R2kUtil.readLcfVLI(fis);
        for (int i = 0; i < len; i++) {
            int key = R2kUtil.readLcfVLI(fis);
            MapInfo target = new MapInfo();
            target.importData(fis);
            //System.out.println(R2kUtil.decodeLcfString(target.name.data));
            mapInfos.map.put(key, target);
        }
        // Non-sparse list
        mapOrder = new int[R2kUtil.readLcfVLI(fis)];
        for (int i = 0; i < mapOrder.length; i++)
            mapOrder[i] = R2kUtil.readLcfVLI(fis);
        activeNode = R2kUtil.readLcfVLI(fis);
        start = new MapTreeStart();
        start.importData(fis);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        mapInfos.exportData(baos);
        // the rest of it
        R2kUtil.writeLcfVLI(baos, mapOrder.length);
        for (int i = 0; i < mapOrder.length; i++)
            R2kUtil.writeLcfVLI(baos, mapOrder[i]);
        R2kUtil.writeLcfVLI(baos, activeNode);
        start.exportData(baos);
        return false;
    }

    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::MapTree", true);

        RubyIO mapOrderArray = new RubyIO();
        mapOrderArray.type = '[';
        mapOrderArray.arrVal = new RubyIO[mapOrder.length];

        for (int i = 0; i < mapOrder.length; i++)
            mapOrderArray.arrVal[i] = new RubyIO().setFX(mapOrder[i]);

        mt.addIVar("@map_infos", mapInfos.asRIO());
        mt.addIVar("@map_order", mapOrderArray);
        mt.addIVar("@active_node", new RubyIO().setFX(activeNode));
        mt.addIVar("@start", start.asRIO());
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        RubyIO mapInfoHash = src.getInstVarBySymbol("@map_infos");
        RubyIO mapOrderArray = src.getInstVarBySymbol("@map_order");

        mapInfos.fromRIO(mapInfoHash);

        mapOrder = new int[mapOrderArray.arrVal.length];
        for (int i = 0; i < mapOrder.length; i++)
            mapOrder[i] = (int) mapOrderArray.arrVal[i].fixnumVal;

        activeNode = (int) src.getInstVarBySymbol("@active_node").fixnumVal;
        start = new MapTreeStart();
        start.fromRIO(src.getInstVarBySymbol("@start"));
    }
}

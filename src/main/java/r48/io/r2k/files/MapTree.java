/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.files;

import r48.RubyIO;
import r48.io.r2k.chunks.BlobR2kProp;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IntegerR2kProp;
import r48.io.r2k.obj.MapInfo;
import r48.io.r2k.obj.MapTreeStart;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This isn't even standard LCF madness. This is something *else*.
 * Anyway, this isn't an interpretable because CBA.
 * Created on 31/05/17.
 */
public class MapTree {
    public HashMap<Integer, MapInfo> mapInfos;
    public int[] mapOrder;
    public int activeNode;
    public MapTreeStart start;

    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::MapTree", true);

        RubyIO mapInfoArray = new RubyIO().setHash();
        RubyIO mapOrderArray = new RubyIO();
        mapOrderArray.type = '[';
        mapOrderArray.arrVal = new RubyIO[mapOrder.length];

        for (Map.Entry<Integer, MapInfo> e : mapInfos.entrySet())
            mapInfoArray.hashVal.put(new RubyIO().setFX(e.getKey()), e.getValue().asRIO());
        for (int i = 0; i < mapOrder.length; i++)
            mapOrderArray.arrVal[i] = new RubyIO().setFX(mapOrder[i]);

        mt.iVars.put("@map_infos", mapInfoArray);
        mt.iVars.put("@map_order", mapOrderArray);
        mt.iVars.put("@active_node", new RubyIO().setFX(activeNode));
        mt.iVars.put("@start", start.asRIO());
        return mt;
    }
}

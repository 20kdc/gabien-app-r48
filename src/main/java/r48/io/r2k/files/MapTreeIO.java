/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.files;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.obj.MapInfo;
import r48.io.r2k.obj.MapTreeStart;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * It's like MapIO, but not!
 * Created on 31/05/17.
 */
public class MapTreeIO {
    public static RubyIO readLmt(InputStream fis) throws IOException {
        String magic = R2kUtil.decodeLcfString(R2kUtil.readLcfBytes(fis, R2kUtil.readLcfVLI(fis)));
        if (!magic.equals("LcfMapTree"))
            throw new IOException("Not an LcfMapTree");
        // Try to follow the standard...
        MapTree mu = new MapTree();
        // Sparse list
        mu.mapInfos = new HashMap<Integer, MapInfo>();
        int len = R2kUtil.readLcfVLI(fis);
        for (int i = 0; i < len; i++) {
            int key = R2kUtil.readLcfVLI(fis);
            MapInfo target = new MapInfo();
            target.importData(fis);
            mu.mapInfos.put(key, target);
        }
        // Non-sparse list
        mu.mapOrder = new int[R2kUtil.readLcfVLI(fis)];
        for (int i = 0; i < mu.mapOrder.length; i++)
            mu.mapOrder[i] = R2kUtil.readLcfVLI(fis);
        mu.activeNode = R2kUtil.readLcfVLI(fis);
        mu.start = new MapTreeStart();
        mu.start.importData(fis);
        return mu.asRIO();
    }
}

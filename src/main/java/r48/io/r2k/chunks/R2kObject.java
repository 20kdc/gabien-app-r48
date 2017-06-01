/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Needed for SparseArrayblahblahblah to exist.
 * Created on 31/05/17.
 */
public abstract class R2kObject implements IR2kInterpretable {
    public final HashMap<Integer, byte[]> unknownChunks = new HashMap<Integer, byte[]>();
    public abstract Index[] getIndices();

    public boolean terminatable() {
        return false;
    }

    public void importData(InputStream src) throws IOException {
        Index[] t = getIndices();
        while (true) {
            if (src.available() == 0)
                if (terminatable())
                    break;
            int cid = R2kUtil.readLcfVLI(src);
            if (cid == 0)
                break;
            int len = R2kUtil.readLcfVLI(src);
            byte[] data = R2kUtil.readLcfBytes(src, len);
            boolean handled = false;
            for (int i = 0; i < t.length; i++)
                if (cid == t[i].index) {
                    t[i].chunk.importData(data);
                    handled = true;
                    break;
                }
            if (!handled)
                unknownChunks.put(cid, data);
        }
    }
}

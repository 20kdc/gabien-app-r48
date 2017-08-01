/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Needed for SparseArrayblahblahblah to exist.
 * Created on 31/05/17.
 */
public abstract class R2kObject implements IR2kStruct {
    public final HashMap<Integer, byte[]> unknownChunks = new HashMap<Integer, byte[]>();

    public abstract Index[] getIndices();

    public boolean terminatable() {
        return false;
    }
    public boolean disableSanity() {
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
            // System.out.println(this + " -> 0x" + Integer.toHexString(cid) + " [" + len + "]");
            byte[] data = R2kUtil.readLcfBytes(src, len);
            boolean handled = false;
            for (int i = 0; i < t.length; i++)
                if (cid == t[i].index) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    try {
                        t[i].chunk.importData(bais);
                    } catch (IOException e) {
                        throw new IOException("In " + t[i] + " of " + this, e);
                    } catch (RuntimeException e) {
                        throw new RuntimeException("In " + t[i] + " of " + this, e);
                    }
                    if (!disableSanity())
                        if (bais.available() != 0)
                            throw new IOException("Not all of the chunk interpreted by " + t[i] + " in " + this);
                    handled = true;
                    break;
                }
            if (!handled)
                unknownChunks.put(cid, data);
            // System.out.println("<<");
        }
    }

    public boolean exportData(OutputStream baos2) throws IOException {
        // Collate all chunks
        HashMap<Integer, byte[]> chunks = new HashMap<Integer, byte[]>();
        chunks.putAll(unknownChunks);
        for (Index i : getIndices()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!i.chunk.exportData(baos)) {
                byte[] r = baos.toByteArray();
                chunks.put(i.index, r);
            }
        }
        LinkedList<Integer> keys = new LinkedList<Integer>(chunks.keySet());
        Collections.sort(keys);
        for (Integer i : keys) {
            byte[] data = chunks.get(i);
            R2kUtil.writeLcfVLI(baos2, i);
            R2kUtil.writeLcfVLI(baos2, data.length);
            baos2.write(data);
        }
        if (!terminatable())
            baos2.write(0);
        return false;
    }

    protected void asRIOISF(RubyIO mt) {
        for (Index i : getIndices())
            if (i.rioHelperName != null) {
                // Could be actually null for optionals.
                RubyIO rio = ((IR2kStruct) i.chunk).asRIO();
                if (rio != null)
                    mt.addIVar(i.rioHelperName, rio);
            }
        R2kUtil.unkToRio(mt, unknownChunks);
    }

    protected void fromRIOISF(RubyIO mt) {
        for (Index i : getIndices())
            if (i.rioHelperName != null)
                ((IR2kStruct) i.chunk).fromRIO(mt.getInstVarBySymbol(i.rioHelperName));
        R2kUtil.rioToUnk(mt, unknownChunks);
    }
}

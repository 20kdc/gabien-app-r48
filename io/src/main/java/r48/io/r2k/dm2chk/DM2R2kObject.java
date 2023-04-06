/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.dm2chk;

import r48.io.IntUtils;
import r48.io.data.*;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IR2kSizable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * How this works:
 * It's in two states (decided by unpackedDataValid)
 * false: Packed. All fields are INVALID, apart from packedChunkData.
 * true: Unpacked. Fields become valid, packedChunkData nulled.
 * DM2LcfBinding must be present on all fields that act as serializable chunks.
 *
 * NOTE: IRIO Fixed
 *
 * Modified from R2kObject on December 4th 2018.
 */
public class DM2R2kObject extends IRIOFixedObject implements IR2kInterpretable {
    @DM2Optional @DM2FXOBinding("@__LCF__unknown")
    public IRIOFixedHash<Integer, IRIOFixedUser> unknownChunks;

    // --

    // This data becomes potentially out of date the moment the object is unpacked, so this should be nulled then.
    // A note here:
    private HashMap<Integer, byte[]> packedChunkData = new HashMap<Integer, byte[]>();

    public DM2R2kObject(DM2Context ctx, String sym) {
        super(ctx, sym);
    }

    @Override
    protected final void initialize() {
        // Disable automatic initialization, permanently
    }

    private void setUnknownChunks() {
        unknownChunks = new IRIOFixedHash<Integer, IRIOFixedUser>() {
            @Override
            public Integer convertIRIOtoKey(RORIO i) {
                return (int) i.getFX();
            }

            @Override
            public DMKey convertKeyToIRIO(Integer i) {
                return DMKey.of(i);
            }

            @Override
            public IRIOFixedUser newValue() {
                return new IRIOFixedUser("Blob", new byte[0]);
            }
        };
    }

    // Packed data management

    // May actually return packedChunkData.
    protected final HashMap<Integer, byte[]> dm2Pack() throws IOException {
        if (packedChunkData != null)
            return packedChunkData;
        HashMap<Integer, byte[]> ws = new HashMap<Integer, byte[]>();
        dm2PackIntoMap(ws);
        if (unknownChunks != null)
            for (Map.Entry<Integer, IRIOFixedUser> uv : unknownChunks.hashVal.entrySet())
                ws.put(uv.getKey(), uv.getValue().userVal);
        return ws;
    }

    protected final void dm2Unpack() {
        if (packedChunkData != null) {
            HashMap<Integer, byte[]> pcd = packedChunkData;
            packedChunkData = null;
            unknownChunks = null;
            // Removes chunks as they are found so that unknown chunks can eat the rest
            dm2UnpackFromMapDestructively(pcd);
            if (pcd.size() > 0) {
                setUnknownChunks();
                for (Map.Entry<Integer, byte[]> me : pcd.entrySet())
                    unknownChunks.hashVal.put(me.getKey(), new IRIOFixedUser("Blob", me.getValue()));
            }
        }
    }

    protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
        for (Field f : cachedFields) {
            DM2LcfBinding dlb = f.getAnnotation(DM2LcfBinding.class);
            if (dlb != null) {
                IR2kInterpretable iri;
                try {
                    iri = (IR2kInterpretable) f.get(this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (iri != null) {
                    if (!iri.canOmitChunk()) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        iri.exportData(baos);
                        pcd.put(dlb.value(), baos.toByteArray());
                    }
                    DM2LcfSizeBinding dlb2 = f.getAnnotation(DM2LcfSizeBinding.class);
                    if (dlb2 != null) {
                        IR2kSizable isi;
                        try {
                            isi = (IR2kSizable) f.get(this);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        if (isi != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            isi.exportSize(baos);
                            pcd.put(dlb2.value(), baos.toByteArray());
                        }
                    }
                }
            }
        }
    }

    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        // Perform initialization & size chunk removal
        for (Field f : cachedFields) {
            DM2FXOBinding dlbx = f.getAnnotation(DM2FXOBinding.class);
            DM2LcfBinding dlb = f.getAnnotation(DM2LcfBinding.class);

            // Only target fields that are definitely relevant
            if ((dlbx != null) || (dlb != null)) {
                // Null all fields by default,
                //  but if they have an LCF or FXO binding try to revive them
                try {
                    f.set(this, null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                boolean needsInitialize = !f.isAnnotationPresent(DM2Optional.class);
                if (dlb != null)
                    if (pcd.containsKey(dlb.value()))
                        needsInitialize = true;

                if (needsInitialize)
                    if (addField(f) == null)
                        throw new RuntimeException("Chunk " + f + " wasn't created on time");

                // Remove any size chunk that does exist without reading it.
                DM2LcfSizeBinding dlb2 = f.getAnnotation(DM2LcfSizeBinding.class);
                if (dlb2 != null)
                    pcd.remove(dlb2.value());
            }
        }
        // Now actually unpack
        for (Field f : cachedFields) {
            DM2LcfBinding dlb = f.getAnnotation(DM2LcfBinding.class);
            if (dlb != null) {
                byte[] relevantChunk = pcd.remove(dlb.value());
                if (relevantChunk != null) {
                    try {
                        IR2kInterpretable iri = null;
                        try {
                            iri = (IR2kInterpretable) f.get(this);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        ByteArrayInputStream bais = new ByteArrayInputStream(relevantChunk);
                        iri.importData(bais);
                        if (bais.available() > 0)
                            if (disableSanity())
                                throw new RuntimeException("Chunk " + dlb.value() + " ( " + f + " ) had too much data");
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                }
            }
        }
    }

    // --- Actual IRIO impl.

    @Override
    public IRIO setObject(String symbol) {
        super.setObject(symbol);
        // Switch this into unpacked by force. Deletion of all data is totally okay here.
        packedChunkData = null;
        // unknownChunks & main members (since normal initialize() disabled)
        // Any remaining stuff 'should be fine'
        super.initialize();
        return this;
    }

    @Override
    public final String[] getIVars() {
        dm2Unpack();
        return dm2GetIVars();
    }

    @Override
    public final IRIO getIVar(String sym) {
        dm2Unpack();
        return dm2GetIVar(sym);
    }

    @Override
    public final IRIO addIVar(String sym) {
        dm2Unpack();
        if (sym.equals("@__LCF__unknown")) {
            setUnknownChunks();
            return unknownChunks;
        }
        for (Field f : cachedFields) {
            DM2FXOBinding fxo = f.getAnnotation(DM2FXOBinding.class);
            if (fxo != null) {
                if (sym.equals(fxo.value())) {
                    IRIO r = (IRIO) dm2AddField(f);
                    if (r != null)
                        return r;
                    break;
                }
            }
        }
        return dm2AddIVar(sym);
    }

    // This function tries to add a field.
    public final Object addField(Field f) {
        DM2FXOBinding fxo = f.getAnnotation(DM2FXOBinding.class);
        if (fxo != null) {
            IRIO t = dm2AddIVar(fxo.value());
            if (t != null)
                return t;
        }
        return dm2AddField(f);
    }

    @Override
    public final void rmIVar(String sym) {
        dm2Unpack();
        dm2RmIVar(sym);
    }

    // --- Override These w/ super calls

    protected String[] dm2GetIVars() {
        return super.getIVars();
    }

    protected IRIO dm2GetIVar(String sym) {
        return super.getIVar(sym);
    }

    // NOTE: THE ONLY CALLER FOR EITHER OF THESE SHOULD BE addIVar and addField!
    protected IRIO dm2AddIVar(String sym) {
        return null;
    }

    // Must not handle translation into dm2AddIVar due to the 2 callers.
    // This instead happens in addIVar and addField.
    protected Object dm2AddField(final Field f) {
        Object obj = context.createObjectFor(f);
        try {
            f.set(this, obj);
        } catch (Exception ex) {
            throw new RuntimeException("At field: " + f, ex);
        }
        return obj;
    }

    protected void dm2RmIVar(String sym) {
        super.rmIVar(sym);
    }

    // ---

    // Equivalent of liblcf's conditional_zero.
    // If true, then no zero is required and no zero is written.
    protected boolean terminatable() {
        return false;
    }

    // If enabled, it's okay for the interpretable for a chunk to not read 100% of the chunk.
    protected boolean disableSanity() {
        return false;
    }

    // ---

    public void importData(InputStream src) throws IOException {
        // Doing this sets the object back into the packed state.
        packedChunkData = new HashMap<Integer, byte[]>();
        while (true) {
            if (src.available() == 0)
                if (terminatable())
                    break;
            int cid = R2kUtil.readLcfVLI(src);
            if (cid == 0)
                break;
            int len = R2kUtil.readLcfVLI(src);
            // System.out.println(this + " -> 0x" + Integer.toHexString(cid) + " [" + len + "]");
            byte[] data = IntUtils.readBytes(src, len);
            packedChunkData.put(cid, data);
            // System.out.println("<<");
        }
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        HashMap<Integer, byte[]> packed = dm2Pack();
        // Collate all chunks
        LinkedList<Integer> keys = new LinkedList<Integer>(packed.keySet());
        Collections.sort(keys);
        for (Integer i : keys) {
            byte[] data = packed.get(i);
            R2kUtil.writeLcfVLI(baos, i);
            R2kUtil.writeLcfVLI(baos, data.length);
            baos.write(data);
        }
        if (!terminatable())
            baos.write(0);
    }
}

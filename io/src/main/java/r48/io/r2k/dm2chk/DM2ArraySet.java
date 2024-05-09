/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.dm2chk;

import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedHash;
import r48.io.data.RORIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IR2kSizable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copied from DM2Array on December 06, 2018.
 */
public abstract class DM2ArraySet<V extends IRIO> extends IRIOFixedHash<Integer, V> implements IR2kInterpretable, IR2kSizable {
    public final int sizeMode;
    public final boolean sizeUnit, trustData;

    public DM2ArraySet(DMContext dm2, int smode, boolean sunit, boolean trust) {
        super(dm2);
        sizeMode = smode;
        sizeUnit = sunit;
        trustData = trust;
    }

    public DM2ArraySet(DMContext dm2) {
        this(dm2, 0, false, true);
    }

    @Override
    public Integer convertIRIOtoKey(RORIO i) {
        return (int) i.getFX();
    }

    @Override
    public DMKey convertKeyToIRIO(Integer i) {
        return DMKey.of(i);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        hashVal.clear();
        int k = 0;
        while (bais.available() > 0) {
            try {
                V v = newValue();
                ((IR2kInterpretable) v).importData(bais);
                hashVal.put(k++, v);
            } catch (IOException re) {
                if (trustData)
                    throw new IOException("While parsing in array of " + (newValue().getClass()), re);
            } catch (RuntimeException re) {
                if (trustData)
                    throw new IOException("While parsing in array of " + (newValue().getClass()), re);
            }
        }
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        int maxIdx = -1;
        for (Integer rio : hashVal.keySet())
            if (rio > maxIdx)
                maxIdx = rio;

        for (int i = 0; i <= maxIdx; i++) {
            V v = hashVal.get(i);
            if (v == null)
                v = newValue();
            ((IR2kInterpretable) v).exportData(baos);
        }
    }

    @Override
    public void exportSize(OutputStream baos) throws IOException {
        int v;
        if (!sizeUnit) {
            ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            exportData(b2);
            v = b2.size();
        } else {
            v = 0;
            for (Integer rio : hashVal.keySet())
                if (rio >= v)
                    v = rio + 1;
        }
        switch (sizeMode) {
            case 0:
                R2kUtil.writeLcfVLI(baos, v);
                break;
            case 1:
                if (v > 255)
                    throw new IOException("Too big array.");
                baos.write(v);
                break;
            default:
                throw new RuntimeException("unknown B " + sizeMode);
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.dm2chk;

import r48.io.data.IRIO;
import r48.io.data.IRIOFixedHash;
import r48.io.data.IRIOFixnum;
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

    public DM2ArraySet(int smode, boolean sunit, boolean trust) {
        sizeMode = smode;
        sizeUnit = sunit;
        trustData = trust;
    }

    public DM2ArraySet() {
        this(0, false, true);
    }

    @Override
    public Integer convertIRIOtoKey(IRIO i) {
        return (int) i.getFX();
    }

    @Override
    public IRIO convertKeyToIRIO(Integer i) {
        return new IRIOFixnum(i);
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
    public boolean exportData(OutputStream baos) throws IOException {
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
        return false;
    }

    @Override
    public boolean exportSize(OutputStream baos) throws IOException {

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
        return false;
    }
}

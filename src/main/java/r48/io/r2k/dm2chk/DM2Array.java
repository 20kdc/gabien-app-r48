/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.dm2chk;

import r48.io.data.IRIO;
import r48.io.data.IRIOFixedArray;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.IR2kSizable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * A replacement for ArrayR2kInterpretable with better size chunk handling.
 * Created on December 05, 2018.
 */
public abstract class DM2Array<V extends IRIO> extends IRIOFixedArray<V> implements IR2kInterpretable, IR2kSizable {
    public final int sizeMode;
    public final boolean sizeUnit, trustData;

    public DM2Array(int smode, boolean sunit, boolean trust) {
        sizeMode = smode;
        sizeUnit = sunit;
        trustData = trust;
    }

    public DM2Array() {
        this(0, false, true);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        LinkedList<V> array = new LinkedList<V>();
        while (bais.available() > 0) {
            try {
                V v = newValue();
                ((IR2kInterpretable) v).importData(bais);
                array.add(v);
            } catch (IOException re) {
                if (trustData)
                    throw new IOException("While parsing in array of " + (newValue().getClass()), re);
            } catch (RuntimeException re) {
                if (trustData)
                    throw new IOException("While parsing in array of " + (newValue().getClass()), re);
            }
        }
        arrVal = array.toArray(new IRIO[0]);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        for (IRIO v : arrVal)
            ((IR2kInterpretable) v).exportData(baos);
        return false;
    }

    @Override
    public boolean exportSize(OutputStream baos) throws IOException {
        int v = arrVal.length;
        if (!sizeUnit) {
            ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            exportData(b2);
            v = b2.size();
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

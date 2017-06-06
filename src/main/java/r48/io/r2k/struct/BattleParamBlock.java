/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.struct;

import r48.ArrayUtils;
import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BPB
 * Created on 06/06/17.
 */
public class BattleParamBlock implements IR2kStruct {
    public short[] array = new short[6];
    @Override
    public RubyIO asRIO() {
        RubyIO arr = new RubyIO();
        arr.type = '[';
        arr.arrVal = new RubyIO[6];
        for (int i = 0; i < 6; i++)
            arr.arrVal[i] = new RubyIO().setFX(array[i] & 0xFFFF);
        return arr;
    }

    @Override
    public void fromRIO(RubyIO src) {
        for (int i = 0; i < 6; i++)
            array[i] = (short) src.arrVal[i].fixnumVal;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        for (int i = 0; i < 6; i++)
            array[i] = (short) R2kUtil.readLcfU16(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        for (int i = 0; i < 6; i++)
            R2kUtil.writeLcfU16(baos, array[i]);
        return false;
    }
}

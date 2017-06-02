/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.struct;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * True proof this thing hates me.
 * Created on 31/05/17.
 */
public class TRect implements IR2kStruct {
    public int l, u, r, d;

    @Override
    public void importData(InputStream bais) throws IOException {
        l = R2kUtil.readLcfS32(bais);
        u = R2kUtil.readLcfS32(bais);
        r = R2kUtil.readLcfS32(bais);
        d = R2kUtil.readLcfS32(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfS32(baos, l);
        R2kUtil.writeLcfS32(baos, u);
        R2kUtil.writeLcfS32(baos, r);
        R2kUtil.writeLcfS32(baos, d);
        return false;
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rb = new RubyIO().setSymlike("Rect", true);
        rb.iVars.put("@left", new RubyIO().setFX(l));
        rb.iVars.put("@up", new RubyIO().setFX(u));
        rb.iVars.put("@right", new RubyIO().setFX(r));
        rb.iVars.put("@down", new RubyIO().setFX(d));
        return rb;
    }

    @Override
    public void fromRIO(RubyIO src) {
        l = (int) src.getInstVarBySymbol("@left").fixnumVal;
        u = (int) src.getInstVarBySymbol("@up").fixnumVal;
        r = (int) src.getInstVarBySymbol("@right").fixnumVal;
        d = (int) src.getInstVarBySymbol("@down").fixnumVal;
    }
}

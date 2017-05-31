/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.struct;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;

/**
 * True proof this thing hates me.
 * Created on 31/05/17.
 */
public class TRect implements IR2kInterpretable {
    public int l, u, r, d;

    @Override
    public void importData(InputStream bais) throws IOException {
        l = R2kUtil.readLcfS32(bais);
        u = R2kUtil.readLcfS32(bais);
        r = R2kUtil.readLcfS32(bais);
        d = R2kUtil.readLcfS32(bais);
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
}

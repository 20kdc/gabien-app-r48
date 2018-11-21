/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import r48.RubyIO;
import r48.io.data.IRIO;
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
        rb.addIVar("@left", new RubyIO().setFX(l));
        rb.addIVar("@up", new RubyIO().setFX(u));
        rb.addIVar("@right", new RubyIO().setFX(r));
        rb.addIVar("@down", new RubyIO().setFX(d));
        return rb;
    }

    @Override
    public void fromRIO(IRIO src) {
        l = (int) src.getIVar("@left").getFX();
        u = (int) src.getIVar("@up").getFX();
        r = (int) src.getIVar("@right").getFX();
        d = (int) src.getIVar("@down").getFX();
    }
}

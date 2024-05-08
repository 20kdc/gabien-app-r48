/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.IntUtils;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * True proof this thing hates me.
 * Created on 31/05/17.
 */
public class TRect extends IRIOFixedObject implements IR2kInterpretable {
    @DMFXOBinding("@left")
    public IRIOFixnum l;
    @DMFXOBinding("@up")
    public IRIOFixnum u;
    @DMFXOBinding("@right")
    public IRIOFixnum r;
    @DMFXOBinding("@down")
    public IRIOFixnum d;

    public TRect(DMContext dm2c) {
        super(dm2c, "Rect");
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        l.val = IntUtils.readS32(bais);
        u.val = IntUtils.readS32(bais);
        r.val = IntUtils.readS32(bais);
        d.val = IntUtils.readS32(bais);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        IntUtils.writeS32(baos, (int) l.val);
        IntUtils.writeS32(baos, (int) u.val);
        IntUtils.writeS32(baos, (int) r.val);
        IntUtils.writeS32(baos, (int) d.val);
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@left"))
            return l = new IRIOFixnum(context, 0);
        if (sym.equals("@up"))
            return u = new IRIOFixnum(context, 0);
        if (sym.equals("@right"))
            return r = new IRIOFixnum(context, 0);
        if (sym.equals("@down"))
            return d = new IRIOFixnum(context, 0);
        return null;
    }
}

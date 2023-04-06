/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.ika;

import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.IRIOFixedObject;

/**
 * Created on November 24, 2018.
 */
public class IkaEvent extends IRIOFixedObject {
    @DM2FXOBinding("@x")
    public IRIOFixnum x;
    @DM2FXOBinding("@y")
    public IRIOFixnum y;
    @DM2FXOBinding("@tOX")
    public IRIOFixnum tox;
    @DM2FXOBinding("@tOY")
    public IRIOFixnum toy;
    @DM2FXOBinding("@type")
    public IRIOFixnum type;
    @DM2FXOBinding("@status")
    public IRIOFixnum status;
    @DM2FXOBinding("@scriptId")
    public IRIOFixnum scriptId;
    @DM2FXOBinding("@collisionType")
    public IRIOFixnum collisionType;

    public IkaEvent(DM2Context ctx) {
        super(ctx, "IkachanEvent");
        initialize();
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@x"))
            return x = new IRIOFixnum(0);
        if (sym.equals("@y"))
            return y = new IRIOFixnum(0);
        if (sym.equals("@tOX"))
            return tox = new IRIOFixnum(0);
        if (sym.equals("@tOY"))
            return toy = new IRIOFixnum(0);
        if (sym.equals("@type"))
            return type = new IRIOFixnum(0);
        if (sym.equals("@status"))
            return status = new IRIOFixnum(0);
        if (sym.equals("@scriptId"))
            return scriptId = new IRIOFixnum(0);
        if (sym.equals("@collisionType"))
            return collisionType = new IRIOFixnum(0);
        return null;
    }
}

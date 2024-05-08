/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.ika;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.IRIOFixedObject;

/**
 * Created on November 24, 2018.
 */
public class IkaEvent extends IRIOFixedObject {
    @DMFXOBinding("@x")
    public IRIOFixnum x;
    @DMFXOBinding("@y")
    public IRIOFixnum y;
    @DMFXOBinding("@tOX")
    public IRIOFixnum tox;
    @DMFXOBinding("@tOY")
    public IRIOFixnum toy;
    @DMFXOBinding("@type")
    public IRIOFixnum type;
    @DMFXOBinding("@status")
    public IRIOFixnum status;
    @DMFXOBinding("@scriptId")
    public IRIOFixnum scriptId;
    @DMFXOBinding("@collisionType")
    public IRIOFixnum collisionType;

    public IkaEvent(DMContext ctx) {
        super(ctx, "IkachanEvent");
        initialize();
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@x"))
            return x = new IRIOFixnum(context, 0);
        if (sym.equals("@y"))
            return y = new IRIOFixnum(context, 0);
        if (sym.equals("@tOX"))
            return tox = new IRIOFixnum(context, 0);
        if (sym.equals("@tOY"))
            return toy = new IRIOFixnum(context, 0);
        if (sym.equals("@type"))
            return type = new IRIOFixnum(context, 0);
        if (sym.equals("@status"))
            return status = new IRIOFixnum(context, 0);
        if (sym.equals("@scriptId"))
            return scriptId = new IRIOFixnum(context, 0);
        if (sym.equals("@collisionType"))
            return collisionType = new IRIOFixnum(context, 0);
        return null;
    }
}

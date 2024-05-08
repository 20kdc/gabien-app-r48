/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.ika;

import r48.io.data.DMContext;
import r48.io.data.IRIOFixnum;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.IRIOFixedObject;

/**
 * Created on November 24, 2018.
 */
public class IkaEvent extends IRIOFixedObject {
    @DMFXOBinding("@x") @DMCXInteger(0)
    public IRIOFixnum x;
    @DMFXOBinding("@y") @DMCXInteger(0)
    public IRIOFixnum y;
    @DMFXOBinding("@tOX") @DMCXInteger(0)
    public IRIOFixnum tox;
    @DMFXOBinding("@tOY") @DMCXInteger(0)
    public IRIOFixnum toy;
    @DMFXOBinding("@type") @DMCXInteger(0)
    public IRIOFixnum type;
    @DMFXOBinding("@status") @DMCXInteger(0)
    public IRIOFixnum status;
    @DMFXOBinding("@scriptId") @DMCXInteger(0)
    public IRIOFixnum scriptId;
    @DMFXOBinding("@collisionType") @DMCXInteger(0)
    public IRIOFixnum collisionType;

    public IkaEvent(DMContext ctx) {
        super(ctx, "IkachanEvent");
        initialize();
    }
}

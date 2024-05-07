/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.ika;

import r48.RubyTable;
import r48.io.data.*;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.IRIOFixedObject;

/**
 * Data Model 2 proof of concept
 * Created on November 22, 2018.
 */
public class IkaMap extends IRIOFixedObject {
    @DM2FXOBinding("@data")
    public IRIOFixedUser data;
    @DM2FXOBinding("@palette")
    public IRIOFixedUser palette;
    @DM2FXOBinding("@events")
    public IRIOFixedHash<Integer, IkaEvent> events;

    public final int defaultWidth;
    public final int defaultHeight;

    public IkaMap(DM2Context ctx, int w, int h) {
        super(ctx, "IkachanMap");
        defaultWidth = w;
        defaultHeight = h;
        initialize();
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@data"))
            return data = new IRIOFixedUser(dm2Ctx.dm3, "Table", new RubyTable(3, defaultWidth, defaultHeight, 1, new int[1]).innerBytes);
        if (sym.equals("@palette"))
            return palette = new IRIOFixedUser(dm2Ctx.dm3, "Table", new RubyTable(3, 256, 1, 4, new int[4]).innerBytes);
        if (sym.equals("@events"))
            return events = new IRIOFixedHash<Integer, IkaEvent>(dm2Ctx.dm3) {
                @Override
                public Integer convertIRIOtoKey(RORIO i) {
                    return (int) i.getFX();
                }

                @Override
                public DMKey convertKeyToIRIO(Integer i) {
                    return DMKey.of(i);
                }

                @Override
                public IkaEvent newValue() {
                    return new IkaEvent(IkaMap.this.dm2Ctx);
                }
            };
        return null;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.ika;

import r48.RubyTable;
import r48.io.data.*;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.IRIOFixedObject;

/**
 * Data Model 2 proof of concept
 * Created on November 22, 2018.
 */
public class IkaMap extends IRIOFixedObject {
    @DMFXOBinding("@data")
    public IRIOFixedUser data;
    @DMFXOBinding("@palette")
    public IRIOFixedUser palette;
    @DMFXOBinding("@events")
    public IRIOFixedHash<Integer, IkaEvent> events;

    public final int defaultWidth;
    public final int defaultHeight;

    public IkaMap(DMContext ctx, int w, int h) {
        super(ctx, "IkachanMap");
        defaultWidth = w;
        defaultHeight = h;
        initialize();
    }

    public void data_add() {
        data = new IRIOFixedUser(context, "Table", RubyTable.initNewTable(3, defaultWidth, defaultHeight, 1, new int[1]).data);
    }

    public void palette_add() {
        palette = new IRIOFixedUser(context, "Table", RubyTable.initNewTable(3, 256, 1, 4, new int[4]).data);
    }

    public void events_add() {
        events = new IRIOFixedHash<Integer, IkaEvent>(context) {
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
                return new IkaEvent(IkaMap.this.context);
            }
        };
    }
}

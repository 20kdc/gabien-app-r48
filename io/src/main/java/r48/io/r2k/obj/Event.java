/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import java.util.function.Supplier;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 31/05/17.
 */
public class Event extends DM2R2kObject {
    @DMFXOBinding("@name") @DM2LcfBinding(1) @DMCXObject
    public StringR2kStruct name;
    @DMFXOBinding("@x") @DM2LcfBinding(2) @DMCXInteger(0)
    public IntegerR2kStruct x;
    @DMFXOBinding("@y") @DM2LcfBinding(3) @DMCXInteger(0)
    public IntegerR2kStruct y;
    @DMFXOBinding("@pages") @DM2LcfBinding(5)
    public DM2SparseArrayA<EventPage> pages;

    public Event(DMContext ctx) {
        super(ctx, "RPG::Event");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@pages"))
            return pages = new DM2SparseArrayA<EventPage>(context, new Supplier<EventPage>() {
                @Override
                public EventPage get() {
                    return new EventPage(context);
                }
            });
        return super.dm2AddIVar(sym);
    }
}

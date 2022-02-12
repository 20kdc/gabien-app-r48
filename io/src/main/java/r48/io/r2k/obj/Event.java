/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.uslx.append.*;
import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

/**
 * Created on 31/05/17.
 */
public class Event extends DM2R2kObject {
    @DM2FXOBinding("@name") @DM2LcfBinding(1) @DM2LcfObject
    public StringR2kStruct name = new StringR2kStruct();
    @DM2FXOBinding("@x") @DM2LcfBinding(2) @DM2LcfInteger(0)
    public IntegerR2kStruct x = new IntegerR2kStruct(0);
    @DM2FXOBinding("@y") @DM2LcfBinding(3) @DM2LcfInteger(0)
    public IntegerR2kStruct y = new IntegerR2kStruct(0);
    @DM2FXOBinding("@pages") @DM2LcfBinding(5)
    public DM2SparseArrayA<EventPage> pages;

    public Event() {
        super("RPG::Event");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@pages"))
            return pages = new DM2SparseArrayA<EventPage>(new ISupplier<EventPage>() {
                @Override
                public EventPage get() {
                    return new EventPage();
                }
            });
        return super.dm2AddIVar(sym);
    }
}

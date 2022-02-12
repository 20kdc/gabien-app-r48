/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.MoveCommand;

/**
 * You know, I have tons of classes in here just for SERIALIZING LCF STUFF.
 * seriously, it's getting ridiculous.
 * Created on 02/06/17.
 */
public class MoveRoute extends DM2R2kObject {
    @DM2FXOBinding("@list") @DM2LcfSizeBinding(11) @DM2LcfBinding(12)
    public DM2Array<MoveCommand> list;

    @DM2FXOBinding("@repeat") @DM2LcfBinding(21) @DM2LcfBoolean(true)
    public BooleanR2kStruct repeat;
    @DM2FXOBinding("@skippable") @DM2LcfBinding(22) @DM2LcfBoolean(false)
    public BooleanR2kStruct skippable;

    public MoveRoute() {
        super("RPG::MoveRoute");
    }

    @Override
    protected IRIO dm2AddIVar(String sym) {
        if (sym.equals("@list"))
            return list = new DM2Array<MoveCommand>(0, false, false) {
                @Override
                public MoveCommand newValue() {
                    return new MoveCommand();
                }
            };
        return super.dm2AddIVar(sym);
    }
}

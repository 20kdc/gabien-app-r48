/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj;

import java.util.function.Consumer;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.dm2chk.*;
import r48.io.r2k.struct.MoveCommand;

/**
 * You know, I have tons of classes in here just for SERIALIZING LCF STUFF.
 * seriously, it's getting ridiculous.
 * Created on 02/06/17.
 */
public class MoveRoute extends DM2R2kObject {
    @DMFXOBinding("@list") @DM2LcfSizeBinding(11) @DM2LcfBinding(12)
    public DM2Array<MoveCommand> list;
    public static Consumer<MoveRoute> list_add = (v) -> v.list = new DM2Array<MoveCommand>(v.context, 0, false, false) {
        @Override
        public MoveCommand newValue() {
            return new MoveCommand(v.context);
        }
    };

    @DMFXOBinding("@repeat") @DM2LcfBinding(21) @DMCXBoolean(true)
    public BooleanR2kStruct repeat;
    @DMFXOBinding("@skippable") @DM2LcfBinding(22) @DMCXBoolean(false)
    public BooleanR2kStruct skippable;

    public MoveRoute(DMContext ctx) {
        super(ctx, "RPG::MoveRoute");
    }
}

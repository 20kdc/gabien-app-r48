/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;
import r48.io.r2k.struct.EventCommand;

/**
 * COPY jun6-2017
 * fixed up later that day along with part of Item and all of Skill.
 */
public class CommonEvent extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct trigger = new IntegerR2kStruct(0);
    public BooleanR2kStruct conditionSwitch = new BooleanR2kStruct(false);
    public IntegerR2kStruct switchId = new IntegerR2kStruct(1);
    public ArraySizeR2kInterpretable<EventCommand> listSize = new ArraySizeR2kInterpretable<EventCommand>();
    public ArrayR2kStruct<EventCommand> list = new ArrayR2kStruct<EventCommand>(listSize, new ISupplier<EventCommand>() {
        @Override
        public EventCommand get() {
            return new EventCommand();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x0B, trigger, "@trigger"),
                new Index(0x0C, conditionSwitch, "@condition_switch"),
                new Index(0x0D, switchId, "@condition_switch_id"),
                new Index(0x15, listSize),
                new Index(0x16, list, "@list")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::CommonEvent", true);
        asRIOISF(rio);
        return rio;
    }
}

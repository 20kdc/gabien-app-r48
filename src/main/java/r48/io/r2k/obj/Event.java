/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IntegerR2kProp;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayR2kProp;
import r48.io.r2k.chunks.StringR2kProp;

import java.util.HashMap;

/**
 * Created on 31/05/17.
 */
public class Event extends R2kObject {
    public StringR2kProp name = new StringR2kProp();
    public IntegerR2kProp x = new IntegerR2kProp(0);
    public IntegerR2kProp y = new IntegerR2kProp(0);
    public SparseArrayR2kProp<EventPage> pages = new SparseArrayR2kProp<EventPage>(new ISupplier<EventPage>() {
        @Override
        public EventPage get() {
            return new EventPage();
        }
    });

    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name),
                new Index(0x02, x),
                new Index(0x03, y),
                new Index(0x05, pages)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Event", true);
        mt.iVars.put("@name", new RubyIO().setString(name.data));
        mt.iVars.put("@x", new RubyIO().setFX(x.i));
        mt.iVars.put("@y", new RubyIO().setFX(y.i));
        // Pages... prepare!
        mt.iVars.put("@pages", pages.toRIOArray());
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}

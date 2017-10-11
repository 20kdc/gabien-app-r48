/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.SparseArrayAR2kStruct;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * Created on 31/05/17.
 */
public class Event extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct x = new IntegerR2kStruct(0);
    public IntegerR2kStruct y = new IntegerR2kStruct(0);
    public SparseArrayAR2kStruct<EventPage> pages = new SparseArrayAR2kStruct<EventPage>(new ISupplier<EventPage>() {
        @Override
        public EventPage get() {
            return new EventPage();
        }
    });

    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, x, "@x"),
                new Index(0x03, y, "@y"),
                new Index(0x05, pages, "@pages")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Event", true);
        asRIOISF(mt);
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

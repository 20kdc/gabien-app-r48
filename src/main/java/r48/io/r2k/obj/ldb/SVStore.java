/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * Created on 05/06/17.
 */
public class SVStore extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO str = name.asRIO();
        asRIOISF(str);
        return str;
    }

    @Override
    public void fromRIO(RubyIO src) {
        name.fromRIO(src);
        fromRIOISF(src);
    }
}

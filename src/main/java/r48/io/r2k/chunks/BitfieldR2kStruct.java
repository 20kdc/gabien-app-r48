/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.RubyIO;

/**
 * Created on 02/06/17.
 */
public class BitfieldR2kStruct extends ByteR2kStruct {

    // Ascending
    public final String[] flags;

    public BitfieldR2kStruct(String[] f, int def) {
        super(def);
        flags = f;
    }

    @Override
    public RubyIO asRIO() {
        RubyIO r = new RubyIO().setSymlike("__bitfield__", true);
        int pwr = 1;
        for (String s : flags) {
            r.addIVar(s, new RubyIO().setBool((pwr & value) != 0));
            pwr <<= 1;
        }
        return r;
    }

    @Override
    public void fromRIO(RubyIO src) {
        int pwr = 1;
        value = 0;
        for (String s : flags) {
            if (src.getInstVarBySymbol(s).type == 'T')
                value |= pwr;
            pwr <<= 1;
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * As the street-lights are turning on outside...
 * Created on 31/05/17.
 */
public class Music extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct fadeTime = new IntegerR2kStruct(0);
    public IntegerR2kStruct volume = new IntegerR2kStruct(100);
    public IntegerR2kStruct tempo = new IntegerR2kStruct(100);
    public IntegerR2kStruct balance = new IntegerR2kStruct(50);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, fadeTime, "@fadeTime"),
                new Index(0x03, volume, "@volume"),
                new Index(0x04, tempo, "@tempo"),
                new Index(0x05, balance, "@balance")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Music", true);
        asRIOISF(rio);
        return rio;
    }
}

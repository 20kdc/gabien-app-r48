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
import r48.io.r2k.chunks.OptionalR2kStruct;
import r48.io.r2k.chunks.R2kObject;

/**
 * Just boring stuff, really...
 * Created on 31/05/17.
 */
public class MapTreeStart extends R2kObject {
    public IntegerR2kStruct playerMap = new IntegerR2kStruct(0);
    public IntegerR2kStruct playerX = new IntegerR2kStruct(0);
    public IntegerR2kStruct playerY = new IntegerR2kStruct(0);

    private ISupplier<IntegerR2kStruct> intX = new ISupplier<IntegerR2kStruct>() {
        @Override
        public IntegerR2kStruct get() {
            return new IntegerR2kStruct(0);
        }
    };

    public OptionalR2kStruct<IntegerR2kStruct> boatMap = new OptionalR2kStruct<IntegerR2kStruct>(intX);
    public OptionalR2kStruct<IntegerR2kStruct> boatX = new OptionalR2kStruct<IntegerR2kStruct>(intX);
    public OptionalR2kStruct<IntegerR2kStruct> boatY = new OptionalR2kStruct<IntegerR2kStruct>(intX);

    public OptionalR2kStruct<IntegerR2kStruct> shipMap = new OptionalR2kStruct<IntegerR2kStruct>(intX);
    public OptionalR2kStruct<IntegerR2kStruct> shipX = new OptionalR2kStruct<IntegerR2kStruct>(intX);
    public OptionalR2kStruct<IntegerR2kStruct> shipY = new OptionalR2kStruct<IntegerR2kStruct>(intX);

    public OptionalR2kStruct<IntegerR2kStruct> airshipMap = new OptionalR2kStruct<IntegerR2kStruct>(intX);
    public OptionalR2kStruct<IntegerR2kStruct> airshipX = new OptionalR2kStruct<IntegerR2kStruct>(intX);
    public OptionalR2kStruct<IntegerR2kStruct> airshipY = new OptionalR2kStruct<IntegerR2kStruct>(intX);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, playerMap, "@player_map"),
                new Index(0x02, playerX, "@player_x"),
                new Index(0x03, playerY, "@player_y"),
                new Index(0x0B, boatMap, "@boat_map"),
                new Index(0x0C, boatX, "@boat_x"),
                new Index(0x0D, boatY, "@boat_y"),
                new Index(0x15, shipMap, "@ship_map"),
                new Index(0x16, shipX, "@ship_x"),
                new Index(0x17, shipY, "@ship_y"),
                new Index(0x1F, airshipMap, "@airship_map"),
                new Index(0x20, airshipX, "@airship_x"),
                new Index(0x21, airshipY, "@airship_y"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Start", true);
        asRIOISF(mt);
        return mt;
    }
}

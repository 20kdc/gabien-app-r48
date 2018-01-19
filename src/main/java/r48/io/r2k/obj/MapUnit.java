/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;

/**
 * First test of the new system for figuring out what the hell to do with this alien format
 * Writing this here for lack of a better place:
 * Unknown attributes are going to get stuffed into some "@unknownMeta" place,
 * containing raw blobs of whatever.
 * Created on 31/05/17.
 */
public class MapUnit extends R2kObject {
    public IntegerR2kStruct tilesetId = new IntegerR2kStruct(1);
    public IntegerR2kStruct width = new IntegerR2kStruct(20);
    public IntegerR2kStruct height = new IntegerR2kStruct(15);
    public IntegerR2kStruct scrollType = new IntegerR2kStruct(0);
    public BooleanR2kStruct parallaxFlag = new BooleanR2kStruct(false);
    public StringR2kStruct parallaxName = new StringR2kStruct();
    public BooleanR2kStruct parallaxLoopX = new BooleanR2kStruct(false);
    public BooleanR2kStruct parallaxLoopY = new BooleanR2kStruct(false);
    public BooleanR2kStruct parallaxAutoloopX = new BooleanR2kStruct(false);
    public BooleanR2kStruct parallaxAutoloopY = new BooleanR2kStruct(false);
    public IntegerR2kStruct parallaxSX = new IntegerR2kStruct(0);
    public IntegerR2kStruct parallaxSY = new IntegerR2kStruct(0);
    public BooleanR2kStruct topLevel = new BooleanR2kStruct(false);
    public BlobR2kStruct layer0 = new BlobR2kStruct(R2kUtil.supplyBlank(20 * 15 * 2, (byte) 0));
    public BlobR2kStruct layer1 = new BlobR2kStruct(R2kUtil.supplyBlank(20 * 15 * 2, (byte) 0));
    public SparseArrayHR2kStruct<Event> events = new SparseArrayHR2kStruct<Event>(new ISupplier<Event>() {
        @Override
        public Event get() {
            return new Event();
        }
    });
    public IntegerR2kStruct magicNumber = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, tilesetId, "@tileset_id"),
                new Index(0x02, width, "@width"),
                new Index(0x03, height, "@height"),
                new Index(0x0B, scrollType, "@scroll_type"),
                new Index(0x1F, parallaxFlag, "@parallax_flag"),
                new Index(0x20, parallaxName, "@parallax_name"),
                new Index(0x21, parallaxLoopX, "@parallax_loop_x"),
                new Index(0x22, parallaxLoopY, "@parallax_loop_y"),
                new Index(0x23, parallaxAutoloopX, "@parallax_autoloop_x"),
                new Index(0x24, parallaxSX, "@parallax_sx"),
                new Index(0x25, parallaxAutoloopY, "@parallax_autoloop_y"),
                new Index(0x26, parallaxSY, "@parallax_sy"),
                new Index(0x2A, topLevel, "@top_level"),
                new Index(0x47, layer0),
                new Index(0x48, layer1),
                new Index(0x51, events, "@events"),
                new Index(0x5B, magicNumber, "@save_count")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO map = new RubyIO().setSymlike("RPG::Map", true);
        asRIOISF(map);
        map.addIVar("@data", makeLmuData());
        return map;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
        decLmuData(src.getInstVarBySymbol("@data"));
    }

    private void decLmuData(RubyIO instVarBySymbol) {
        // Assuming all consistency is fine, width/height are fine too
        layer0.dat = new byte[width.i * height.i * 2];
        layer1.dat = new byte[width.i * height.i * 2];
        byte[] innerBytes = instVarBySymbol.userVal;
        System.arraycopy(innerBytes, 20, layer0.dat, 0, layer0.dat.length);
        System.arraycopy(innerBytes, 20 + (width.i * height.i * 2), layer1.dat, 0, layer1.dat.length);
    }

    private RubyIO makeLmuData() {
        // -- transform the lower-layer and upper-layer data...
        RubyTable rt = new RubyTable(3, width.i, height.i, 2, new int[] {0, 0});
        System.arraycopy(layer0.dat, 0, rt.innerBytes, 20, layer0.dat.length);
        System.arraycopy(layer1.dat, 0, rt.innerBytes, 20 + (width.i * height.i * 2), layer1.dat.length);
        return new RubyIO().setUser("Table", rt.innerBytes);
    }
}

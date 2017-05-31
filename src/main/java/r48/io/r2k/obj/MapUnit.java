/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.RubyTable;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;

import java.util.HashMap;
import java.util.Map;

/**
 * First test of the new system for figuring out what the hell to do with this alien format
 * Writing this here for lack of a better place:
 *  Unknown attributes are going to get stuffed into some "@unknownMeta" place,
 *   containing raw blobs of whatever.
 * Created on 31/05/17.
 */
public class MapUnit extends R2kObject {
    public IntegerR2kProp tilesetId = new IntegerR2kProp(1);
    public IntegerR2kProp width = new IntegerR2kProp(20);
    public IntegerR2kProp height = new IntegerR2kProp(15);
    public IntegerR2kProp scrollType = new IntegerR2kProp(0);
    public IntegerR2kProp parallaxFlag = new IntegerR2kProp(0);
    public StringR2kProp parallaxName = new StringR2kProp("");
    public IntegerR2kProp parallaxLoopX = new IntegerR2kProp(0); // B
    public IntegerR2kProp parallaxLoopY = new IntegerR2kProp(0); // B
    public IntegerR2kProp parallaxAutoloopX = new IntegerR2kProp(0); // B
    public IntegerR2kProp parallaxAutoloopY = new IntegerR2kProp(0); // B
    public IntegerR2kProp parallaxSX = new IntegerR2kProp(0);
    public IntegerR2kProp parallaxSY = new IntegerR2kProp(0);
    public IntegerR2kProp topLevel = new IntegerR2kProp(0);
    public BlobR2kProp layer0 = new BlobR2kProp();
    public BlobR2kProp layer1 = new BlobR2kProp();
    public SparseArrayR2kProp<Event> events = new SparseArrayR2kProp<Event>(new ISupplier<Event>() {
        @Override
        public Event get() {
            return new Event();
        }
    });
    public IntegerR2kProp magicNumber = new IntegerR2kProp(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, tilesetId),
                new Index(0x02, width),
                new Index(0x03, height),
                new Index(0x0B, scrollType),
                new Index(0x1F, parallaxFlag),
                new Index(0x20, parallaxName),
                new Index(0x21, parallaxLoopX),
                new Index(0x22, parallaxLoopY),
                new Index(0x23, parallaxAutoloopX),
                new Index(0x24, parallaxSX),
                new Index(0x25, parallaxAutoloopY),
                new Index(0x26, parallaxSY),
                new Index(0x2A, topLevel),
                new Index(0x47, layer0),
                new Index(0x48, layer1),
                new Index(0x51, events),
                new Index(0x5B, magicNumber)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO map = new RubyIO().setSymlike("RPG::Map", true);

        map.iVars.put("@tileset_id", new RubyIO().setFX(tilesetId.i));
        map.iVars.put("@width", new RubyIO().setFX(width.i));
        map.iVars.put("@height", new RubyIO().setFX(height.i));
        map.iVars.put("@scroll_type", new RubyIO().setFX(scrollType.i));
        map.iVars.put("@parallax_flag", new RubyIO().setBool(parallaxFlag.i != 0));
        map.iVars.put("@parallax_name", new RubyIO().setString(parallaxName.text));
        map.iVars.put("@parallax_loop_x", new RubyIO().setBool(parallaxLoopX.i != 0));
        map.iVars.put("@parallax_loop_y", new RubyIO().setBool(parallaxLoopY.i != 0));
        map.iVars.put("@parallax_autoloop_x", new RubyIO().setBool(parallaxAutoloopX.i != 0));
        map.iVars.put("@parallax_sx", new RubyIO().setFX(parallaxSX.i));
        map.iVars.put("@parallax_autoloop_y", new RubyIO().setBool(parallaxAutoloopY.i != 0));
        map.iVars.put("@parallax_sy", new RubyIO().setFX(parallaxSY.i));
        map.iVars.put("@top_level", new RubyIO().setBool(topLevel.i != 0));
        map.iVars.put("@data", makeLmuData());
        map.iVars.put("@events", events.toRIOHash());
        map.iVars.put("@magic_number", new RubyIO().setFX(magicNumber.i));

        R2kUtil.unkToRio(map, unknownChunks);
        return map;
    }
    private RubyIO makeLmuData() {
        // -- transform the lower-layer and upper-layer data...
        RubyTable rt = new RubyTable(width.i, height.i, 2);
        for (int i = 0; i < layer0.dat.length; i++)
            rt.innerBytes[20 + i] = layer0.dat[i];
        for (int i = 0; i < layer1.dat.length; i++)
            rt.innerBytes[20 + i + (width.i * height.i * 2)] = layer1.dat[i];
        RubyIO encap = new RubyIO();
        encap.type = 'u';
        encap.symVal = "Table";
        encap.userVal = rt.innerBytes;
        return encap;
    }
}

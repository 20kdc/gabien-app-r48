/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.*;
import r48.io.r2k.struct.EventCommand;

/**
 * Created on 31/05/17.
 */
public class EventPage extends R2kObject {
    public BlobR2kProp condition = new BlobR2kProp();
    public StringR2kProp graphicCName = new StringR2kProp();
    public IntegerR2kProp graphicCIndex = new IntegerR2kProp(0);
    public IntegerR2kProp graphicCDirection = new IntegerR2kProp(2);
    public IntegerR2kProp graphicCPattern = new IntegerR2kProp(1);
    public IntegerR2kProp graphicCBlendMode = new IntegerR2kProp(0);
    public IntegerR2kProp moveType = new IntegerR2kProp(1);
    public IntegerR2kProp moveFreq = new IntegerR2kProp(3);
    public IntegerR2kProp trigger = new IntegerR2kProp(0);
    public IntegerR2kProp layer = new IntegerR2kProp(0);
    public IntegerR2kProp blocking = new IntegerR2kProp(0);
    public IntegerR2kProp animType = new IntegerR2kProp(0);
    public IntegerR2kProp moveSpeed = new IntegerR2kProp(0);
    public BlobR2kProp moveRoute = new BlobR2kProp();
    public ArraySizeR2kProp<EventCommand> listSize = new ArraySizeR2kProp<EventCommand>();
    public ArrayR2kProp<EventCommand> list = new ArrayR2kProp<EventCommand>(listSize, new ISupplier<EventCommand>() {
        @Override
        public EventCommand get() {
            return new EventCommand();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x02, condition),
                new Index(0x15, graphicCName),
                new Index(0x16, graphicCIndex),
                new Index(0x17, graphicCDirection),
                new Index(0x18, graphicCPattern),
                new Index(0x19, graphicCBlendMode),
                new Index(0x1F, moveType),
                new Index(0x20, moveFreq),
                new Index(0x21, trigger),
                new Index(0x22, layer),
                new Index(0x23, blocking),
                new Index(0x24, animType),
                new Index(0x25, moveSpeed),
                new Index(0x29, moveRoute),
                new Index(0x33, listSize),
                new Index(0x34, list)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::EventPage", true);
        mt.iVars.put("@condition", new RubyIO().setUser("Blob", condition.dat));
        mt.iVars.put("@character_name", new RubyIO().setString(graphicCName.data));
        mt.iVars.put("@character_index", new RubyIO().setFX(graphicCIndex.i));
        mt.iVars.put("@character_direction", new RubyIO().setFX(graphicCDirection.i));
        mt.iVars.put("@character_pattern", new RubyIO().setFX(graphicCPattern.i));
        mt.iVars.put("@character_blend_mode", new RubyIO().setFX(graphicCBlendMode.i));
        mt.iVars.put("@move_type", new RubyIO().setFX(moveType.i));
        mt.iVars.put("@move_freq", new RubyIO().setFX(moveFreq.i));
        mt.iVars.put("@trigger", new RubyIO().setFX(trigger.i));
        mt.iVars.put("@layer", new RubyIO().setFX(layer.i));
        mt.iVars.put("@blocking", new RubyIO().setFX(blocking.i));
        mt.iVars.put("@anim_type", new RubyIO().setFX(animType.i));
        mt.iVars.put("@move_speed", new RubyIO().setFX(moveSpeed.i));
        mt.iVars.put("@move_route", new RubyIO().setUser("Blob", moveRoute.dat));
        mt.iVars.put("@list", list.toRIOArray());
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}

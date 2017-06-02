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
    public EventPageCondition condition = new EventPageCondition();
    public StringR2kStruct graphicCName = new StringR2kStruct();
    public IntegerR2kStruct graphicCIndex = new IntegerR2kStruct(0);
    public IntegerR2kStruct graphicCDirection = new IntegerR2kStruct(2);
    public IntegerR2kStruct graphicCPattern = new IntegerR2kStruct(1);
    public IntegerR2kStruct graphicCBlendMode = new IntegerR2kStruct(0);
    public IntegerR2kStruct moveType = new IntegerR2kStruct(1);
    public IntegerR2kStruct moveFreq = new IntegerR2kStruct(3);
    public IntegerR2kStruct trigger = new IntegerR2kStruct(0);
    public IntegerR2kStruct layer = new IntegerR2kStruct(0);
    public IntegerR2kStruct blocking = new IntegerR2kStruct(0);
    public IntegerR2kStruct animType = new IntegerR2kStruct(0);
    public IntegerR2kStruct moveSpeed = new IntegerR2kStruct(0);
    public BlobR2kStruct moveRoute = new BlobR2kStruct(R2kUtil.supplyBlank(0, (byte) 0));
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
                new Index(0x02, condition, "@condition"),
                new Index(0x15, graphicCName, "@character_name"),
                new Index(0x16, graphicCIndex, "@character_index"),
                new Index(0x17, graphicCDirection, "@character_direction"),
                new Index(0x18, graphicCPattern, "@character_pattern"),
                new Index(0x19, graphicCBlendMode, "@character_blend_mode"),
                new Index(0x1F, moveType, "@move_type"),
                new Index(0x20, moveFreq, "@move_freq"),
                new Index(0x21, trigger, "@trigger"),
                new Index(0x22, layer, "@layer"),
                new Index(0x23, blocking, "@blocking"),
                new Index(0x24, animType, "@anim_type"),
                new Index(0x25, moveSpeed, "@move_speed"),
                new Index(0x29, moveRoute, "@move_route"),
                new Index(0x33, listSize),
                new Index(0x34, list, "@list")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::EventPage", true);
        asRIOISF(mt);
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

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
import r48.io.r2k.struct.TRect;

/**
 *
 * Created on 31/05/17.
 */
public class MapInfo extends R2kObject {
    public StringR2kProp name = new StringR2kProp();
    public IntegerR2kProp parent = new IntegerR2kProp(0);
    public IntegerR2kProp indent = new IntegerR2kProp(0);
    public IntegerR2kProp type = new IntegerR2kProp(0);
    public IntegerR2kProp editPosX = new IntegerR2kProp(0);
    public IntegerR2kProp editPosY = new IntegerR2kProp(0);
    public IntegerR2kProp expanded = new IntegerR2kProp(0);
    public IntegerR2kProp musicType = new IntegerR2kProp(0);
    public InterpretableR2kProp<Music> music = new InterpretableR2kProp<Music>(new ISupplier<Music>() {
        @Override
        public Music get() {
            return new Music();
        }
    });
    public IntegerR2kProp backgroundType = new IntegerR2kProp(0);
    public StringR2kProp backgroundName = new StringR2kProp();
    public IntegerR2kProp teleportState = new IntegerR2kProp(0);
    public IntegerR2kProp escapeState = new IntegerR2kProp(0);
    public IntegerR2kProp saveState = new IntegerR2kProp(0);
    public SparseArrayR2kProp<Encounter> encounters = new SparseArrayR2kProp<Encounter>(new ISupplier<Encounter>() {
        @Override
        public Encounter get() {
            return new Encounter();
        }
    });
    public IntegerR2kProp encounterSteps = new IntegerR2kProp(0);
    public InterpretableR2kProp<TRect> areaRect = new InterpretableR2kProp<TRect>(new ISupplier<TRect>() {
        @Override
        public TRect get() {
            return new TRect();
        }
    });

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name),
                new Index(0x02, parent),
                new Index(0x03, indent),
                new Index(0x04, type),
                new Index(0x05, editPosX),
                new Index(0x06, editPosY),
                new Index(0x07, expanded),
                new Index(0x0B, musicType),
                new Index(0x0C, music),
                new Index(0x15, backgroundType),
                new Index(0x16, backgroundName),
                new Index(0x1F, teleportState),
                new Index(0x20, escapeState),
                new Index(0x21, saveState),
                new Index(0x29, encounters),
                new Index(0x2C, encounterSteps),
                new Index(0x33, areaRect)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::MapInfo", true);
        mt.iVars.put("@name", new RubyIO().setString(name.data));
        mt.iVars.put("@parent", new RubyIO().setFX(parent.i));
        mt.iVars.put("@indent", new RubyIO().setFX(indent.i));
        mt.iVars.put("@type", new RubyIO().setFX(type.i));
        mt.iVars.put("@edit_pos_x", new RubyIO().setFX(editPosX.i));
        mt.iVars.put("@edit_pos_y", new RubyIO().setFX(editPosY.i));
        mt.iVars.put("@expanded", new RubyIO().setBool(expanded.i != 0));
        mt.iVars.put("@music_type", new RubyIO().setFX(musicType.i));
        mt.iVars.put("@music", music.instance.asRIO());
        mt.iVars.put("@background_type", new RubyIO().setFX(backgroundType.i));
        mt.iVars.put("@background_name", new RubyIO().setString(backgroundName.data));
        mt.iVars.put("@teleport_state", new RubyIO().setFX(teleportState.i));
        mt.iVars.put("@escape_state", new RubyIO().setFX(escapeState.i));
        mt.iVars.put("@save_state", new RubyIO().setFX(saveState.i));
        mt.iVars.put("@encounters", encounters.toRIOArray());
        mt.iVars.put("@area_rect", areaRect.instance.asRIO());
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}

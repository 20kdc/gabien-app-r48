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
    public StringR2kStruct name = new StringR2kStruct();
    public IntegerR2kStruct parent = new IntegerR2kStruct(0);
    public IntegerR2kStruct indent = new IntegerR2kStruct(0);
    public IntegerR2kStruct type = new IntegerR2kStruct(-1);
    public IntegerR2kStruct editPosX = new IntegerR2kStruct(0);
    public IntegerR2kStruct editPosY = new IntegerR2kStruct(0);
    public BooleanR2kStruct expanded = new BooleanR2kStruct(false);
    public IntegerR2kStruct musicType = new IntegerR2kStruct(0);
    public Music music = new Music();
    public IntegerR2kStruct backgroundType = new IntegerR2kStruct(0);
    public StringR2kStruct backgroundName = new StringR2kStruct();
    public IntegerR2kStruct teleportState = new IntegerR2kStruct(0);
    public IntegerR2kStruct escapeState = new IntegerR2kStruct(0);
    public IntegerR2kStruct saveState = new IntegerR2kStruct(0);
    public SparseArrayAR2kStruct<Encounter> encounters = new SparseArrayAR2kStruct<Encounter>(new ISupplier<Encounter>() {
        @Override
        public Encounter get() {
            return new Encounter();
        }
    });
    public IntegerR2kStruct encounterSteps = new IntegerR2kStruct(25);
    public TRect areaRect = new TRect();

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, parent, "@parent_id"),
                new Index(0x03, indent, "@indent"),
                new Index(0x04, type, "@type"),
                new Index(0x05, editPosX, "@edit_pos_x"),
                new Index(0x06, editPosY, "@edit_pos_y"),
                new Index(0x07, expanded, "@expanded"),
                new Index(0x0B, musicType, "@music_type"),
                new Index(0x0C, music, "@music"),
                new Index(0x15, backgroundType, "@background_type"),
                new Index(0x16, backgroundName, "@background_name"),
                new Index(0x1F, teleportState, "@teleport_state"),
                new Index(0x20, escapeState, "@escape_state"),
                new Index(0x21, saveState, "@save_state"),
                new Index(0x29, encounters, "@encounters"),
                new Index(0x2C, encounterSteps, "@encounter_steps"),
                new Index(0x33, areaRect, "@area_rect")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::MapInfo", true);
        asRIOISF(mt);
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

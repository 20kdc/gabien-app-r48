/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

/**
 * They're pieces on the board that are waiting to be moved...
 */
public class SaveActor extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct title = new StringR2kStruct();
    public StringR2kStruct sprite_name = new StringR2kStruct();
    public IntegerR2kStruct sprite_id = new IntegerR2kStruct(0);
    public IntegerR2kStruct sprite_flags = new IntegerR2kStruct(0);
    public StringR2kStruct face_name = new StringR2kStruct();
    public IntegerR2kStruct face_id = new IntegerR2kStruct(0);
    public IntegerR2kStruct level = new IntegerR2kStruct(-1);
    public IntegerR2kStruct exp = new IntegerR2kStruct(-1);
    public IntegerR2kStruct hp_mod = new IntegerR2kStruct(-1);
    public IntegerR2kStruct sp_mod = new IntegerR2kStruct(-1);
    public IntegerR2kStruct attack_mod = new IntegerR2kStruct(0);
    public IntegerR2kStruct defense_mod = new IntegerR2kStruct(0);
    public IntegerR2kStruct spirit_mod = new IntegerR2kStruct(0);
    public IntegerR2kStruct agility_mod = new IntegerR2kStruct(0);
    // WARNING: DOUBLE PLUS UNGOOD MISSING OR UNTAGGED SIZEFIELDS!
    public IntegerR2kStruct skills_size = new IntegerR2kStruct(-1);
    public ArrayR2kStruct<ShortR2kStruct> skills = new ArrayR2kStruct<ShortR2kStruct>(null, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    }, true);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, title, "@title"),
                new Index(0x0B, sprite_name, "@sprite_name"),
                new Index(0x0C, sprite_id, "@sprite_id"),
                new Index(0x0D, sprite_flags, "@sprite_flags"),
                new Index(0x15, face_name, "@face_name"),
                new Index(0x16, face_id, "@face_id"),
                new Index(0x1F, level, "@level"),
                new Index(0x20, exp, "@exp"),
                new Index(0x21, hp_mod, "@hp_mod"),
                new Index(0x22, sp_mod, "@sp_mod"),
                new Index(0x29, attack_mod, "@attack_mod"),
                new Index(0x2A, defense_mod, "@defense_mod"),
                new Index(0x2B, spirit_mod, "@spirit_mod"),
                new Index(0x2C, agility_mod, "@agility_mod"),
                new Index(0x33, skills_size, "@skills_size"),
                new Index(0x34, skills, "@skills"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO root = new RubyIO().setSymlike("RPG::SaveActor", true);
        asRIOISF(root);
        return root;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

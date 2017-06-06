/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * Created on 05/06/17.
 */
public class Item extends R2kObject {
    public StringR2kStruct name = new StringR2kStruct();
    public StringR2kStruct description = new StringR2kStruct();
    public IntegerR2kStruct type = new IntegerR2kStruct(0);
    public IntegerR2kStruct price = new IntegerR2kStruct(0);
    public IntegerR2kStruct uses = new IntegerR2kStruct(1);
    public IntegerR2kStruct atkPoints1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct defPoints1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct spiPoints1 = new IntegerR2kStruct(0);
    public IntegerR2kStruct agiPoints1 = new IntegerR2kStruct(0);
    public BooleanR2kStruct twoHanded = new BooleanR2kStruct(false);
    public IntegerR2kStruct spCost = new IntegerR2kStruct(0);
    public IntegerR2kStruct hit = new IntegerR2kStruct(90);
    public IntegerR2kStruct crit = new IntegerR2kStruct(0);
    public IntegerR2kStruct animation = new IntegerR2kStruct(1);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, description, "@description"),
                new Index(0x03, type, "@type"),
                new Index(0x05, price, "@price"),
                new Index(0x06, uses, "@use_amount"),
                new Index(0x0B, atkPoints1, "@equipbuff_atk"),
                new Index(0x0C, defPoints1, "@equipbuff_def"),
                new Index(0x0D, spiPoints1, "@equipbuff_spi"),
                new Index(0x0E, agiPoints1, "@equipbuff_agi"),
                new Index(0x0F, twoHanded, "@two_handed"),
                new Index(0x10, spCost, "@use_sp_cost"),
                new Index(0x11, hit, "@hit_chance"),
                new Index(0x12, crit, "@crit_chance"),
                new Index(0x14, animation, "@animation")
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Item", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}

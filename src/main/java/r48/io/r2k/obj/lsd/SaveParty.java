/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.*;

public class SaveParty extends R2kObject {
    public ArraySizeR2kInterpretable<ShortR2kStruct> partySize = new ArraySizeR2kInterpretable<ShortR2kStruct>(true);
    public ArrayR2kStruct<ShortR2kStruct> party = new ArrayR2kStruct<ShortR2kStruct>(partySize, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    });
    // NOTE: These *don't* get put in directly
    public IntegerR2kStruct inventorySize = new IntegerR2kStruct(0);
    public ArrayR2kInterpretable<ShortR2kStruct> inventoryIds = new ArrayR2kInterpretable<ShortR2kStruct>(null, new ISupplier<ShortR2kStruct>() {
        @Override
        public ShortR2kStruct get() {
            return new ShortR2kStruct(0);
        }
    }, true);
    public ArrayR2kInterpretable<ByteR2kStruct> inventoryCounts = new ArrayR2kInterpretable<ByteR2kStruct>(null, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(0);
        }
    }, true);
    public ArrayR2kInterpretable<ByteR2kStruct> inventoryUsage = new ArrayR2kInterpretable<ByteR2kStruct>(null, new ISupplier<ByteR2kStruct>() {
        @Override
        public ByteR2kStruct get() {
            return new ByteR2kStruct(0);
        }
    }, true);

    public IntegerR2kStruct partyGold = new IntegerR2kStruct(0);
    public IntegerR2kStruct timer1Seconds = new IntegerR2kStruct(0);
    public BooleanR2kStruct timer1Active = new BooleanR2kStruct(false);
    public BooleanR2kStruct timer1Visible = new BooleanR2kStruct(false);
    public BooleanR2kStruct timer1ActiveDuringBattle = new BooleanR2kStruct(false);
    public IntegerR2kStruct timer2Seconds = new IntegerR2kStruct(0);
    public BooleanR2kStruct timer2Active = new BooleanR2kStruct(false);
    public BooleanR2kStruct timer2Visible = new BooleanR2kStruct(false);
    public BooleanR2kStruct timer2ActiveDuringBattle = new BooleanR2kStruct(false);

    public IntegerR2kStruct battles = new IntegerR2kStruct(0);
    public IntegerR2kStruct defeats = new IntegerR2kStruct(0);
    public IntegerR2kStruct escapes = new IntegerR2kStruct(0);
    public IntegerR2kStruct victories = new IntegerR2kStruct(0);

    public IntegerR2kStruct turns = new IntegerR2kStruct(0);
    public IntegerR2kStruct steps = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, partySize),
                new Index(0x02, party, "@party"),
                new Index(0x0B, inventorySize),
                new Index(0x0C, inventoryIds),
                new Index(0x0D, inventoryCounts),
                new Index(0x0E, inventoryUsage),
                new Index(0x15, partyGold, "@party_gold"),
                new Index(0x17, timer1Seconds, "@timer1_seconds"),
                new Index(0x18, timer1Active, "@timer1_on"),
                new Index(0x19, timer1Visible, "@timer1_visible"),
                new Index(0x1A, timer1ActiveDuringBattle, "@timer1_on_battle"),
                new Index(0x1B, timer2Seconds, "@timer2_seconds"),
                new Index(0x1C, timer2Active, "@timer2_on"),
                new Index(0x1D, timer2Visible, "@timer2_visible"),
                new Index(0x1E, timer2ActiveDuringBattle, "@timer2_on_battle"),
                new Index(0x20, battles, "@stats_battles"),
                new Index(0x21, defeats, "@stats_defeats"),
                new Index(0x22, escapes, "@stats_escapes"),
                new Index(0x23, victories, "@stats_victories"),
                new Index(0x29, turns, "@stats_turns"),
                new Index(0x2A, steps, "@stats_steps"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::SaveParty", true);
        asRIOISF(rio);
        // inventory
        RubyIO inv = new RubyIO();
        inv.type = '[';
        inv.arrVal = new IRIO[inventorySize.i];
        for (int i = 0; i < inv.arrVal.length; i++) {
            RubyIO sl = new RubyIO().setSymlike("RPG::SaveItem", true);
            inv.arrVal[i] = sl;
            sl.addIVar("@id", inventoryIds.array.get(i).asRIO());
            sl.addIVar("@count", inventoryCounts.array.get(i).asRIO());
            sl.addIVar("@usage", inventoryUsage.array.get(i).asRIO());
        }
        rio.addIVar("@inventory", inv);
        return rio;
    }

    @Override
    public void fromRIO(IRIO src) {
        fromRIOISF(src);
        // inventory
        IRIO inv = src.getIVar("@inventory");
        inventorySize.i = inv.getALen();
        inventoryIds.array.clear();
        inventoryCounts.array.clear();
        inventoryUsage.array.clear();
        for (int i = 0; i < inventorySize.i; i++) {
            ShortR2kStruct id = new ShortR2kStruct(0);
            ByteR2kStruct count = new ByteR2kStruct(0);
            ByteR2kStruct usage = new ByteR2kStruct(0);
            id.fromRIO(inv.getAElem(i).getIVar("@id"));
            count.fromRIO(inv.getAElem(i).getIVar("@count"));
            usage.fromRIO(inv.getAElem(i).getIVar("@usage"));
            inventoryIds.array.add(id);
            inventoryCounts.array.add(count);
            inventoryUsage.array.add(usage);
        }
    }
}

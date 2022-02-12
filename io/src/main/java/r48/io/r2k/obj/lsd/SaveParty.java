/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DM2FXOBinding;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedArray;
import r48.io.data.IRIOFixedObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.ShortR2kStruct;
import r48.io.r2k.dm2chk.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

public class SaveParty extends DM2R2kObject {
    @DM2FXOBinding("@party") @DM2LcfSizeBinding(1) @DM2LcfBinding(0x02)
    public DM2Array<ShortR2kStruct> party;
    // NOTE: These *don't* get put in directly.
    @DM2LcfBinding(0x0B) @DM2LcfInteger(0)
    public IntegerR2kStruct inventorySize;
    @DM2LcfBinding(0x0C)
    public DM2Array<ShortR2kStruct> inventoryIds;
    @DM2LcfBinding(0x0D)
    public DM2Array<ByteR2kStruct> inventoryCounts;
    @DM2LcfBinding(0x0E)
    public DM2Array<ByteR2kStruct> inventoryUsage;

    @DM2FXOBinding("@inventory")
    public IRIOFixedArray<SaveItem> inventoryView;

    @DM2FXOBinding("@party_gold") @DM2LcfBinding(0x15) @DM2LcfInteger(0)
    public IntegerR2kStruct partyGold;
    @DM2FXOBinding("@timer1_seconds") @DM2LcfBinding(0x17) @DM2LcfInteger(0)
    public IntegerR2kStruct timer1Seconds;
    @DM2FXOBinding("@timer1_on") @DM2LcfBinding(0x18) @DM2LcfBoolean(false)
    public BooleanR2kStruct timer1Active;
    @DM2FXOBinding("@timer1_visible") @DM2LcfBinding(0x19) @DM2LcfBoolean(false)
    public BooleanR2kStruct timer1Visible;
    @DM2FXOBinding("@timer1_on_battle") @DM2LcfBinding(0x1A) @DM2LcfBoolean(false)
    public BooleanR2kStruct timer1ActiveDuringBattle;
    @DM2FXOBinding("@timer2_seconds") @DM2LcfBinding(0x1B) @DM2LcfInteger(0)
    public IntegerR2kStruct timer2Seconds;
    @DM2FXOBinding("@timer2_on") @DM2LcfBinding(0x1C) @DM2LcfBoolean(false)
    public BooleanR2kStruct timer2Active;
    @DM2FXOBinding("@timer2_visible") @DM2LcfBinding(0x1D) @DM2LcfBoolean(false)
    public BooleanR2kStruct timer2Visible;
    @DM2FXOBinding("@timer2_on_battle") @DM2LcfBinding(0x1E) @DM2LcfBoolean(false)
    public BooleanR2kStruct timer2ActiveDuringBattle;

    @DM2FXOBinding("@stats_battles") @DM2LcfBinding(0x20) @DM2LcfInteger(0)
    public IntegerR2kStruct battles;
    @DM2FXOBinding("@stats_defeats") @DM2LcfBinding(0x21) @DM2LcfInteger(0)
    public IntegerR2kStruct defeats;
    @DM2FXOBinding("@stats_escapes") @DM2LcfBinding(0x22) @DM2LcfInteger(0)
    public IntegerR2kStruct escapes;
    @DM2FXOBinding("@stats_victories") @DM2LcfBinding(0x23) @DM2LcfInteger(0)
    public IntegerR2kStruct victories;

    @DM2FXOBinding("@stats_turns") @DM2LcfBinding(0x29) @DM2LcfInteger(0)
    public IntegerR2kStruct turns;
    @DM2FXOBinding("@stats_steps") @DM2LcfBinding(0x2A) @DM2LcfInteger(0)
    public IntegerR2kStruct steps;

    public SaveParty() {
        super("RPG::SaveParty");
    }

    @Override
    protected Object dm2AddField(Field f) {
        if (f.getName().equals("party"))
            return party = newShortArray();
        if (f.getName().equals("inventoryIds"))
            return inventoryIds = newShortArray();
        if (f.getName().equals("inventoryCounts"))
            return inventoryCounts = newByteArray();
        if (f.getName().equals("inventoryUsage"))
            return inventoryUsage = newByteArray();
        if (f.getName().equals("inventoryView"))
            return inventoryView = new IRIOFixedArray<SaveItem>() {
                @Override
                public SaveItem newValue() {
                    return new SaveItem();
                }
            };
        return super.dm2AddField(f);
    }

    private DM2Array<ByteR2kStruct> newByteArray() {
        return new DM2Array<ByteR2kStruct>() {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(0);
            }
        };
    }

    private DM2Array<ShortR2kStruct> newShortArray() {
        return new DM2Array<ShortR2kStruct>(0, true, true) {
            @Override
            public ShortR2kStruct newValue() {
                return new ShortR2kStruct(0);
            }
        };
    }

    @Override
    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        super.dm2UnpackFromMapDestructively(pcd);
        inventoryView.arrVal = new IRIO[inventorySize.i];
        // This uses the loaded IRIOs as-is to simplify things.
        for (int i = 0; i < inventoryView.arrVal.length; i++) {
            SaveItem si = new SaveItem();
            si.id = (ShortR2kStruct) inventoryIds.arrVal[i];
            si.count = (ByteR2kStruct) inventoryCounts.arrVal[i];
            si.usage = (ByteR2kStruct) inventoryUsage.arrVal[i];
            inventoryView.arrVal[i] = si;
        }
        // Done moving stuff.
        inventorySize = null;
        inventoryIds = null;
        inventoryCounts = null;
        inventoryUsage = null;
    }

    @Override
    protected void dm2PackIntoMap(HashMap<Integer, byte[]> pcd) throws IOException {
        inventorySize = new IntegerR2kStruct(0);
        inventoryIds = newShortArray();
        inventoryCounts = newByteArray();
        inventoryUsage = newByteArray();
        super.dm2PackIntoMap(pcd);
        inventorySize = null;
        inventoryIds = null;
        inventoryCounts = null;
        inventoryUsage = null;
    }

    public static class SaveItem extends IRIOFixedObject {
        @DM2FXOBinding("@id")
        public ShortR2kStruct id;

        @DM2FXOBinding("@count")
        public ByteR2kStruct count;

        @DM2FXOBinding("@usage")
        public ByteR2kStruct usage;

        public SaveItem() {
            super("RPG::SaveItem");
        }

        @Override
        public IRIO addIVar(String sym) {
            if (sym.equals("@id"))
                return id = new ShortR2kStruct(0);
            if (sym.equals("@count"))
                return count = new ByteR2kStruct(0);
            if (sym.equals("@usage"))
                return usage = new ByteR2kStruct(0);
            return null;
        }
    }
}

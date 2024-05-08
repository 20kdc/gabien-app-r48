/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.lsd;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedArray;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMCXBoolean;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.ByteR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.ShortR2kStruct;
import r48.io.r2k.dm2chk.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

public class SaveParty extends DM2R2kObject {
    @DMFXOBinding("@party") @DM2LcfSizeBinding(1) @DM2LcfBinding(0x02)
    public DM2Array<ShortR2kStruct> party;
    public static Consumer<SaveParty> party_add = (v) -> v.party = v.newShortArray();
    // NOTE: These *don't* get put in directly.
    @DM2LcfBinding(0x0B) @DMCXInteger(0)
    public IntegerR2kStruct inventorySize;
    @DM2LcfBinding(0x0C)
    public DM2Array<ShortR2kStruct> inventoryIds;
    public static Consumer<SaveParty> inventoryIds_add = (v) -> v.inventoryIds = v.newShortArray();
    @DM2LcfBinding(0x0D)
    public DM2Array<ByteR2kStruct> inventoryCounts;
    public static Consumer<SaveParty> inventoryCounts_add = (v) -> v.inventoryCounts = v.newByteArray();
    @DM2LcfBinding(0x0E)
    public DM2Array<ByteR2kStruct> inventoryUsage;
    public static Consumer<SaveParty> inventoryUsage_add = (v) -> v.inventoryUsage = v.newByteArray();

    @DMFXOBinding("@inventory")
    public IRIOFixedArray<SaveItem> inventoryView;
    public static Consumer<SaveParty> inventoryView_add = (v) -> v.inventoryView = new IRIOFixedArray<SaveItem>(v.context) {
        @Override
        public SaveItem newValue() {
            return new SaveItem(context);
        }
    };

    @DMFXOBinding("@party_gold") @DM2LcfBinding(0x15) @DMCXInteger(0)
    public IntegerR2kStruct partyGold;
    @DMFXOBinding("@timer1_seconds") @DM2LcfBinding(0x17) @DMCXInteger(0)
    public IntegerR2kStruct timer1Seconds;
    @DMFXOBinding("@timer1_on") @DM2LcfBinding(0x18) @DMCXBoolean(false)
    public BooleanR2kStruct timer1Active;
    @DMFXOBinding("@timer1_visible") @DM2LcfBinding(0x19) @DMCXBoolean(false)
    public BooleanR2kStruct timer1Visible;
    @DMFXOBinding("@timer1_on_battle") @DM2LcfBinding(0x1A) @DMCXBoolean(false)
    public BooleanR2kStruct timer1ActiveDuringBattle;
    @DMFXOBinding("@timer2_seconds") @DM2LcfBinding(0x1B) @DMCXInteger(0)
    public IntegerR2kStruct timer2Seconds;
    @DMFXOBinding("@timer2_on") @DM2LcfBinding(0x1C) @DMCXBoolean(false)
    public BooleanR2kStruct timer2Active;
    @DMFXOBinding("@timer2_visible") @DM2LcfBinding(0x1D) @DMCXBoolean(false)
    public BooleanR2kStruct timer2Visible;
    @DMFXOBinding("@timer2_on_battle") @DM2LcfBinding(0x1E) @DMCXBoolean(false)
    public BooleanR2kStruct timer2ActiveDuringBattle;

    @DMFXOBinding("@stats_battles") @DM2LcfBinding(0x20) @DMCXInteger(0)
    public IntegerR2kStruct battles;
    @DMFXOBinding("@stats_defeats") @DM2LcfBinding(0x21) @DMCXInteger(0)
    public IntegerR2kStruct defeats;
    @DMFXOBinding("@stats_escapes") @DM2LcfBinding(0x22) @DMCXInteger(0)
    public IntegerR2kStruct escapes;
    @DMFXOBinding("@stats_victories") @DM2LcfBinding(0x23) @DMCXInteger(0)
    public IntegerR2kStruct victories;

    @DMFXOBinding("@stats_turns") @DM2LcfBinding(0x29) @DMCXInteger(0)
    public IntegerR2kStruct turns;
    @DMFXOBinding("@stats_steps") @DM2LcfBinding(0x2A) @DMCXInteger(0)
    public IntegerR2kStruct steps;

    public SaveParty(DMContext ctx) {
        super(ctx, "RPG::SaveParty");
    }

    private DM2Array<ByteR2kStruct> newByteArray() {
        return new DM2Array<ByteR2kStruct>(context) {
            @Override
            public ByteR2kStruct newValue() {
                return new ByteR2kStruct(context, 0);
            }
        };
    }

    private DM2Array<ShortR2kStruct> newShortArray() {
        return new DM2Array<ShortR2kStruct>(context, 0, true, true) {
            @Override
            public ShortR2kStruct newValue() {
                return new ShortR2kStruct(context, 0);
            }
        };
    }

    @Override
    protected void dm2UnpackFromMapDestructively(HashMap<Integer, byte[]> pcd) {
        super.dm2UnpackFromMapDestructively(pcd);
        inventoryView.arrVal = new IRIO[inventorySize.i];
        // This uses the loaded IRIOs as-is to simplify things.
        for (int i = 0; i < inventoryView.arrVal.length; i++) {
            SaveItem si = new SaveItem(context);
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
        inventorySize = new IntegerR2kStruct(context, 0);
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
        @DMFXOBinding("@id") @DMCXInteger(0)
        public ShortR2kStruct id;

        @DMFXOBinding("@count") @DMCXInteger(0)
        public ByteR2kStruct count;

        @DMFXOBinding("@usage") @DMCXInteger(0)
        public ByteR2kStruct usage;

        public SaveItem(DMContext ctx) {
            super(ctx, "RPG::SaveItem");
        }
    }
}

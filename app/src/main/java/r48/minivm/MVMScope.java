/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

/**
 * This is for stuff that doesn't fit or work with the 8 fast-locals.
 * Created 28th February 2023.
 */
public final class MVMScope {
    public static final MVMScope ROOT = new MVMScope();
    // To avoid allocating one object per variable, slots are grouped into blocks. Each MVMScope is one block.
    // The elements of the slots array are simply the blocks.
    // Slot indices are the indexes of slots within the block.
    private final Object[][] slots;
    private final int[] slotIndices;

    private MVMScope() {
        slots = new Object[0][];
        slotIndices = new int[0];
    }
    private MVMScope(MVMScope base, int addedAlloc) {
        slots = new Object[base.slots.length + addedAlloc][];
        slotIndices = new int[base.slots.length + addedAlloc];
        System.arraycopy(base.slots, 0, slots, 0, base.slots.length);
        System.arraycopy(base.slotIndices, 0, slotIndices, 0, base.slotIndices.length);
        int slotIdx = base.slots.length;
        Object[] myStorage = new Object[addedAlloc];
        for (int i = 0; i < addedAlloc; i++) {
            slots[slotIdx] = myStorage;
            slotIndices[slotIdx] = i;
            slotIdx++;
        }
    }
    public MVMScope extend(int addedAlloc) {
        if (addedAlloc == 0)
            return this;
        return new MVMScope(this, addedAlloc);
    }

    public Object get(int i) {
        Object[] base = slots[i];
        return base[slotIndices[i]];
    }

    public void set(int i, Object v) {
        Object[] base = slots[i];
        base[slotIndices[i]] = v;
    }
}

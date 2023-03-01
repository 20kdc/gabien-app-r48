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
    // To avoid allocating one object per variable, slots are grouped into frames. Each MVMScope is one frame.
    // The elements of the outer slots array are simply the frames.
    // So a slot is indexed as [frame][slot].
    // Blocks are ordered outermost-in (so that block number is independent of the accessor's depth).
    private final Object[][] slots;

    private MVMScope() {
        slots = new Object[0][];
    }
    public MVMScope(MVMScope base, int alloc) {
        slots = new Object[base.slots.length + 1][];
        System.arraycopy(base.slots, 0, slots, 0, base.slots.length);
        slots[base.slots.length] = new Object[alloc];
    }

    public Object get(int f, int i) {
        Object[] base = slots[f];
        return base[i];
    }

    public void set(int f, int i, Object v) {
        Object[] base = slots[f];
        base[i] = v;
    }
}

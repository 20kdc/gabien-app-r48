/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import gabien.uslx.io.ByteArrayMemoryish;

/**
 * Change-tracked data blob with accessors.
 * Created 9th May, 2024.
 */
public final class DMBlob extends ByteArrayMemoryish implements IDM3Data {
    private boolean clean;
    public final DMContext context;

    /**
     * Beware that the array is donated to the DMBlob.
     */
    public DMBlob(DMContext context, byte[] data) {
        super(data);
        this.context = context;
        context.changes.register(this);
    }

    @Override
    public void set8(long at, int v) {
        trackingWillChange();
        super.set8(at, v);
    }

    @Override
    public void setBulk(long at, byte[] data, int offset, int length) {
        trackingWillChange();
        super.setBulk(at, data, offset, length);
    }

    @Override
    public Runnable saveState() {
        byte[] copy = data.clone();
        return () -> {
            System.arraycopy(copy, 0, data, 0, data.length);
        };
    }

    @Override
    public void trackingMarkClean() {
        clean = true;
    }

    private void trackingWillChange() {
        if (clean) {
            if (context.changes.getLicensesToUnpackData() != 0)
                return;
            clean = false;
            context.changes.modifying(this);
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Created on November 24, 2018.
 */
public class IRIOFixnum extends IRIOFixedData {
    private long val;

    public IRIOFixnum(@NonNull DMContext context, long t) {
        super(context, 'i');
        val = t;
    }

    /**
     * This is just so DMCX works properly...
     */
    public IRIOFixnum(@NonNull DMContext context, int t) {
        this(context, (long) t);
    }

    @Override
    public Runnable saveState() {
        final long saved = val;
        return () -> val = saved;
    }

    @Override
    public String[] getIVars() {
        return new String[0];
    }

    @Override
    public IRIO addIVar(String sym) {
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        return null;
    }

    @Override
    public long getFX() {
        return val;
    }

    @Override
    public IRIO setFX(long fx) {
        trackingWillChange();
        val = fx;
        return this;
    }
}

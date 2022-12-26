/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k;

import r48.io.r2k.chunks.IR2kInterpretable;

/**
 * Created on 31/05/17.
 */
public class Index {
    public final int index;
    public final IR2kInterpretable chunk;
    public final String rioHelperName;

    public Index(int i, IR2kInterpretable c) {
        index = i;
        chunk = c;
        rioHelperName = null;
    }

    public Index(int i, IR2kInterpretable c, String rhn) {
        index = i;
        chunk = c;
        rioHelperName = rhn;
    }

    @Override
    public String toString() {
        if (rioHelperName != null)
            return "0x" + Integer.toHexString(index) + " -> " + rioHelperName;
        return "0x" + Integer.toHexString(index);
    }
}

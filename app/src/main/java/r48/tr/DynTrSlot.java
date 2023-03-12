/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import r48.minivm.MVMSlot;
import r48.minivm.fn.MVMFn;

/**
 * Dynamic translation slot.
 * Created 12th March 2023.
 */
public final class DynTrSlot {
    public final MVMSlot underlyingSlot;
    public DynTrSlot(MVMSlot slot) {
        underlyingSlot = slot;
    }

    private String resolve(int ac, Object a0, Object a1, Object a2, Object a3) {
        try {
            Object obj = underlyingSlot.v;
            if (obj instanceof String)
                return (String) obj;
            if (obj instanceof MVMFn) {
                switch (ac) {
                case 0:
                    return (String) ((MVMFn) obj).clDirect();
                case 1:
                    return (String) ((MVMFn) obj).clDirect(a0);
                case 2:
                    return (String) ((MVMFn) obj).clDirect(a0, a1);
                case 3:
                    return (String) ((MVMFn) obj).clDirect(a0, a1, a2);
                case 4:
                    return (String) ((MVMFn) obj).clDirect(a0, a1, a2, a3);
                }
            }
            return obj.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "!!!" + underlyingSlot.s.id + "!!!";
        }
    }

    public String get() {
        return resolve(0, null, null, null, null);
    }

    public String get(Object a0) {
        return resolve(1, a0, null, null, null);
    }

    public String get(Object a0, Object a1) {
        return resolve(2, a0, a1, null, null);
    }

    public String get(Object a0, Object a1, Object a2) {
        return resolve(3, a0, a1, a2, null);
    }

    public String get(Object a0, Object a1, Object a2, Object a3) {
        return resolve(4, a0, a1, a2, a3);
    }
}

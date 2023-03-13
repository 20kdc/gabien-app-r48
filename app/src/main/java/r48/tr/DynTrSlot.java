/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import gabien.uslx.append.ISupplier;
import r48.minivm.MVMSlot;
import r48.minivm.fn.MVMFn;
import r48.tr.TrPage.FF0;
import r48.tr.TrPage.FF1;
import r48.tr.TrPage.FF2;
import r48.tr.TrPage.FF3;
import r48.tr.TrPage.FF4;

/**
 * Dynamic translation slot.
 * Created 12th March 2023.
 */
public final class DynTrSlot implements ISupplier<String>, FF1, FF2, FF3, FF4 {
    public final MVMSlot underlyingSlot;
    public DynTrSlot(MVMSlot slot) {
        underlyingSlot = slot;
    }

    /**
     * Translates a lambda/etc. into a value to initialize an MVM dynamic translation slot.
     */
    public static Object translateIntoMVM(String nh, Object obj) {
        if (obj instanceof String) {
            return obj;
        } else if (obj instanceof FF0) {
            FF0 oc = (FF0) obj;
            return new MVMFn.Fixed(nh) {
                @Override
                public Object callDirect() {
                    return oc.r();
                }
            };
        } else if (obj instanceof FF1) {
            FF1 oc = (FF1) obj;
            return new MVMFn.Fixed(nh) {
                @Override
                public Object callDirect(Object a0) {
                    return oc.r(a0);
                }
            };
        } else if (obj instanceof FF2) {
            FF2 oc = (FF2) obj;
            return new MVMFn.Fixed(nh) {
                @Override
                public Object callDirect(Object a0, Object a1) {
                    return oc.r(a0, a1);
                }
            };
        } else if (obj instanceof FF3) {
            FF3 oc = (FF3) obj;
            return new MVMFn.Fixed(nh) {
                @Override
                public Object callDirect(Object a0, Object a1, Object a2) {
                    return oc.r(a0, a1, a2);
                }
            };
        } else if (obj instanceof FF4) {
            FF4 oc = (FF4) obj;
            return new MVMFn.Fixed(nh) {
                @Override
                public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
                    return oc.r(a0, a1, a2, a3);
                }
            };
        }
        throw new RuntimeException("Cannot translate " + obj + " into DynTrSlot " + nh + "!");
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

    public String r(Object a0) {
        return resolve(1, a0, null, null, null);
    }

    public String r(Object a0, Object a1) {
        return resolve(2, a0, a1, null, null);
    }

    public String r(Object a0, Object a1, Object a2) {
        return resolve(3, a0, a1, a2, null);
    }

    public String r(Object a0, Object a1, Object a2, Object a3) {
        return resolve(4, a0, a1, a2, a3);
    }
}

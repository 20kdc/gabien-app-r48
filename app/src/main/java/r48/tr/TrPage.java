/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.IConsumer;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMSlot;
import r48.minivm.fn.MVMFn;

/**
 * Translation page base.
 * Created 2nd March 2023.
 */
public class TrPage {
    public final void fillFromVM(MVMEnvR48 env, IConsumer<String> logTrIssues) {
        for (Field f : getClass().getFields()) {
            try {
                Type ty = f.getGenericType();
                if (ty instanceof Class) {
                    if (TrPage.class.isAssignableFrom((Class<?>) ty)) {
                        ((TrPage) f.get(this)).fillFromVM(env, logTrIssues);
                        continue;
                    }
                }
                f.set(this, calculateValueFor(env, f.getDeclaringClass().getSimpleName(), f.getName(), ty, logTrIssues));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Object calculateValueFor(MVMEnvR48 env, String cName, String name, Type type, IConsumer<String> logTrIssues) {
        name = cName + "." + name;
        String errMsg = "!!!" + name + "!!!";
        MVMSlot s = env.getSlot(new DatumSymbol(name));
        if (s == null)
            return missingTerm(name, type, logTrIssues, errMsg);
        Object v = s.v;
        if (v == null)
            return missingTerm(name, type, logTrIssues, errMsg);
        if (type == String.class) {
            if (v instanceof String)
                return v;
        } else if (type == FF0.class) {
            if (v instanceof MVMFn) {
                MVMFn f = (MVMFn) v;
                return (FF0) () -> {
                    try {
                        return (String) f.clDirect();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return errMsg;
                    }
                };
            }
        } else if (type == FF1.class) {
            if (v instanceof MVMFn) {
                MVMFn f = (MVMFn) v;
                return (FF1) (a) -> {
                    try {
                        return (String) f.clDirect(a);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return errMsg;
                    }
                };
            }
        } else if (type == FF2.class) {
            if (v instanceof MVMFn) {
                MVMFn f = (MVMFn) v;
                return (FF2) (a, b) -> {
                    try {
                        return (String) f.clDirect(a, b);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return errMsg;
                    }
                };
            }
        } else if (type == FF3.class) {
            if (v instanceof MVMFn) {
                MVMFn f = (MVMFn) v;
                return (FF3) (a, b, c) -> {
                    try {
                        return (String) f.clDirect(a, b, c);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return errMsg;
                    }
                };
            }
        } else if (type == FF4.class) {
            if (v instanceof MVMFn) {
                MVMFn f = (MVMFn) v;
                return (FF4) (a, b, c, d) -> {
                    try {
                        return (String) f.clDirect(a, b, c, d);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return errMsg;
                    }
                };
            }
        } else {
            throw new RuntimeException("TrPage: Unable to handle " + name + " type " + type);
        }
        return missingTerm(name, type, logTrIssues, errMsg);
    }

    private final Object missingTerm(String name, Type type, IConsumer<String> logTrIssues, final String msg) {
        System.err.println("TrPage: Missing term " + name);
        if (type == String.class) {
            return msg;
        } else if (type == FF0.class) {
            return (FF0) () -> msg;
        } else if (type == FF1.class) {
            return (FF1) (a) -> msg;
        } else if (type == FF2.class) {
            return (FF2) (a, b) -> msg;
        } else if (type == FF3.class) {
            return (FF3) (a, b, c) -> msg;
        } else if (type == FF4.class) {
            return (FF4) (a, b, c, d) -> msg;
        } else {
            throw new RuntimeException("TrPage: Unable to handle " + name + " type " + type);
        }
    }

    public interface FF0 {
        String r();
    }

    public interface FF1 {
        String r(Object a0);
    }

    public interface FF2 {
        String r(Object a0, Object a1);
    }

    public interface FF3 {
        String r(Object a0, Object a1, Object a2);
    }

    public interface FF4 {
        String r(Object a0, Object a1, Object a2, Object a3);
    }
}

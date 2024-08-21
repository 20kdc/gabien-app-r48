/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.harvester;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMSlot;
import r48.minivm.MVMType;
import r48.minivm.fn.MVMFn;

/**
 * Harvester. Simplifying the creation of a large library of functions since August 2024.
 * Beware: Harvester is heavy on reflection and should not be used on high-performance functions.
 * It is primarily intended to cover the large variety of SDB setup functions.
 * Created 21st August, 2024.
 */
public class Harvester {
    /**
     * Runs the Harvester against a target library.
     */
    public static void harvest(MVMEnv into, final Object library) {
        for (final Method m : library.getClass().getMethods()) {
            // custom additions
            if (m.getName().equals("add")) {
                try {
                    m.invoke(library, into);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            Defun def = m.getAnnotation(Defun.class);
            if (def == null)
                continue;
            Help defH = m.getAnnotation(Help.class);
            // ...alright, now that's over, we can get on with the hard part! ...I mean, fun stuff!
            Class<?> retType = m.getReturnType();
            MVMType retTypeReal = MVMType.ANY;
            if (retType != void.class) {
                // ok, actually try and figure this out then
                retTypeReal = MVMType.typeOfClass(retType);
            }
            Parameter[] paramsJ = m.getParameters();
            MVMType[] paramsV = new MVMType[paramsJ.length];
            for (int i = 0; i < paramsV.length; i++)
                paramsV[i] = MVMType.typeOfClass(paramsJ[i].getType());

            StringBuilder helpBuilder = new StringBuilder();
            helpBuilder.append("(");
            helpBuilder.append(def.n());
            for (int i = 0; i < paramsJ.length; i++) {
                helpBuilder.append(" A");
                helpBuilder.append(i);
            }
            helpBuilder.append("): ");
            if (defH != null) {
                helpBuilder.append(defH.value());
            } else {
                helpBuilder.append(MVMSlot.DEFAULT_HELP);
            }

            into.defineSlot(new DatumSymbol(def.n()), new MVMFn.VATyped(new MVMType.Fn(retTypeReal, def.r(), paramsV, null), def.n()) {
                @Override
                protected Object callIndirect(Object[] args) {
                    if (args.length < def.r())
                        throw new RuntimeException("under required arg count @ " + this);
                    if (args.length > paramsV.length) {
                        throw new RuntimeException("too many args @ " + this);
                    } else if (args.length < paramsV.length) {
                        // pad
                        Object[] argsPadded = new Object[paramsV.length];
                        System.arraycopy(args, 0, argsPadded, 0, args.length);
                        try {
                            return m.invoke(library, argsPadded);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        // 1:1 match with paramsV
                        try {
                            return m.invoke(library, args);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }).help = helpBuilder.toString();
        }
    }
}

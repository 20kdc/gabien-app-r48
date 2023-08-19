/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.uslx.append.IFunction;
import r48.App;
import r48.dbs.RPGCommand;
import r48.io.IObjectBackend.ILoadedObject;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.schema.specialized.cmgb.RPGCommandSchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Operational mode for universal string finder/universal string replacer
 */
public abstract class USFROperationMode {
    public abstract String translate(App app);

    public abstract int locate(App app, @Nullable SchemaElement se, @NonNull ILoadedObject oi, IFunction<IRIO, Integer> string, boolean writing);

    public static USFROperationMode[] listForApp(App app) {
        LinkedList<USFROperationMode> lls = new LinkedList<>();
        lls.add(All.INSTANCE);
        for (ICommandClassifier icc : app.cmdClassifiers)
            lls.add(new CmdTag(icc));
        return lls.toArray(new USFROperationMode[0]);
    }

    public static final class All extends USFROperationMode {
        public static final All INSTANCE = new All();

        @Override
        public String translate(App app) {
            return app.t.u.usl_modeAll;
        }

        @Override
        public int locate(App app, @Nullable SchemaElement se, @NonNull ILoadedObject oi, IFunction<IRIO, Integer> string, boolean writing) {
            return locate(app, oi.getObject(), string, writing);
        }

        public static int locate(App app, IRIO rio, IFunction<IRIO, Integer> string, boolean writing) {
            // NOTE: Hash keys, ivar keys are not up for modification.
            int total = 0;
            int type = rio.getType();
            if (type == '"')
                total += string.apply(rio);
            if ((type == '{') || (type == '}'))
                for (DMKey me : rio.getHashKeys())
                    total += locate(app, rio.getHashVal(me), string, writing);
            if (type == '[') {
                int arrLen = rio.getALen();
                for (int i = 0; i < arrLen; i++)
                    total += locate(app, rio.getAElem(i), string, writing);
            }
            for (String k : rio.getIVars())
                total += locate(app, rio.getIVar(k), string, writing);
            IMagicalBinder b = MagicalBinders.getBinderFor(app, rio);
            if (b != null) {
                IRIO bound = MagicalBinders.toBoundWithCache(app, b, rio);
                int c = locate(app, bound, string, writing);
                total += c;
                if (writing)
                    if (c != 0)
                        b.applyBoundToTarget(bound, rio);
            }
            return total;
        }
    }

    public static final class CmdTag extends USFROperationMode {
        public final ICommandClassifier base;

        public CmdTag(ICommandClassifier b) {
            base = b;
        }

        @Override
        public String translate(App app) {
            return app.t.u.usl_modeCTag.r(base.getName());
        }

        @Override
        public int locate(App app, @Nullable SchemaElement se, @NonNull ILoadedObject oi, IFunction<IRIO, Integer> string, boolean writing) {
            if (se == null)
                return 0;
            SchemaPath sp = new SchemaPath(se, oi);
            AtomicInteger ai = new AtomicInteger(0);
            se.visit(sp.targetElement, sp, (element, target, path) -> {
                if (element instanceof RPGCommandSchemaElement) {
                    RPGCommand rc = ((RPGCommandSchemaElement) element).getRPGCommand(target);
                    if (base.matches(rc))
                        ai.addAndGet(All.locate(app, target, string, writing));
                }
            });
            return ai.get();
        }
    }
}

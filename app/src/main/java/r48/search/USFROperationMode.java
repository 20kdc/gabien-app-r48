/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.App;
import r48.dbs.RPGCommand;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.SchemaElement.Visitor;
import r48.schema.specialized.cmgb.RPGCommandSchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Operational mode for universal string finder/universal string replacer
 */
public abstract class USFROperationMode {
    public abstract String translate(App app);

    public @Nullable UIElement makeEditor() {
        return null;
    }

    public abstract void locate(App app, SchemaPath root, Visitor visitor, boolean detailedPaths);

    public static USFROperationMode[] listForApp(App app) {
        LinkedList<USFROperationMode> lls = new LinkedList<>();
        lls.add(All.INSTANCE);
        for (ICommandClassifier icc : app.cmdClassifiers)
            if (icc instanceof ICommandClassifier.Immutable)
                lls.add(new CmdTag((ICommandClassifier.Immutable) icc));
        return lls.toArray(new USFROperationMode[0]);
    }

    public static final class All extends USFROperationMode {
        public static final All INSTANCE = new All();

        @Override
        public String translate(App app) {
            return app.t.u.usl_modeAll;
        }

        @Override
        public void locate(App app, SchemaPath root, Visitor visitor, boolean detailedPaths) {
            root.editor.visit(root.targetElement, root, makeMyVisitor(visitor), detailedPaths);
        }

        public static Visitor makeMyVisitor(final Visitor base) {
            return new Visitor() {
                @Override
                public boolean visit(SchemaElement element, IRIO target, SchemaPath path) {
                    if (target.getType() == '"') {
                        return base.visit(element, target, path);
                    } else {
                        return true;
                    }
                }
            };
        }
    }

    public static final class CmdTag extends USFROperationMode {
        public final ICommandClassifier.Immutable base;

        public CmdTag(ICommandClassifier.Immutable b) {
            base = b;
        }

        @Override
        public String translate(App app) {
            return app.t.u.usl_modeCTag.r(base.getName(app));
        }

        @Override
        public void locate(App app, SchemaPath root, Visitor visitor, boolean detailedPaths) {
            Visitor mod = All.makeMyVisitor(visitor);
            root.editor.visit(root.targetElement, root, (element, target, path) -> {
                if (element instanceof RPGCommandSchemaElement) {
                    RPGCommand rc = ((RPGCommandSchemaElement) element).getRPGCommand(target);
                    if (base.matches(rc, target)) {
                        element.visit(target, path, mod, detailedPaths);
                        return false;
                    }
                }
                return true;
            }, detailedPaths);
        }
    }
}

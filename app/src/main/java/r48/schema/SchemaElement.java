/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import java.io.PrintWriter;
import java.io.StringWriter;

import gabien.ui.UIElement;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UIScrollLayout;
import r48.R48;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.pages.TrRoot;

/**
 * Acts as a bridge so that backbone code can be switched to IRIOs.
 * (Oh dear it had to be merged to prevent typing issues systemwide)
 * Created on November 21, 2018.
 */
public abstract class SchemaElement extends SchemaElementIOP {
    public final R48 app;
    public final TrRoot T;

    public SchemaElement(R48 app) {
        this.app = app;
        this.T = app.t;
    }

    // For lack of a better place.
    public static boolean checkType(RORIO tgt, int t, String objType, boolean setDefault) {
        if (tgt.getType() != t)
            return true;
        if (objType != null)
            if (!tgt.getSymbol().equals(objType))
                return true;
        return setDefault;
    }

    /**
     * Creates a UI element to indicate the target object has become invalid.
     */
    public final UIElement objectHasBecomeInvalidScreen(SchemaPath sp) {
        return new UIScrollLayout(true, app.f.generalS, new UILabel(T.s.objectHasBecomeInvalid.r(sp, this), app.f.schemaFieldTH));
    }

    /**
     * Creates the editor control.
     */
    public final UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        try {
            return buildHoldingEditorImpl(target, launcher, path);
        } catch (Exception ex) {
            if (app.ilg.strict)
                throw ex;
            ex.printStackTrace();
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            return new UIScrollLayout(true, app.f.generalS, new UILabel(T.s.seInternalError + sw.toString(), app.f.schemaFieldTH));
        }
    }

    // Creates the editor control.
    // Ground rules:
    // 1. If the control changes the value, path.changeOccurred(false) MUST be called.
    //    However, it must NOT be called if the value does not change.
    //    (This also applies to modifyVal, but true should be passed, not false.)
    //    Ordering is Modify Value/path.changeOccurred/Update UI (just in case)
    // 2. If this element monitors subelements, use the tagSEMonitor!
    //    (This also applies to modifyVal.)
    // 3. If you do any error checking at all,
    //     make it fail fast.
    //    (This does not apply to modifyVal :
    //      modifyVal's rule is
    //      "Fix if possible, but failing that, nuke it.")
    // 4. Always, ALWAYS give the element being returned the correct vertical size.
    //    Horizontal does not matter.
    //    (Obviously doesn't apply to modifyVal.)
    // 5. When possible, write a generic element that
    //     conveniently covers the other cases you'll find,
    //     which you can put into the SDB language.
    //    Failing that, throw it in specialized.
    // And finally, a reminder.
    // These rules were determined by trial and error over 5 and a half days.
    // Before the system was even *completed.*
    // Probably best not to break them.
    protected abstract UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path);

    /**
     * Has no sub-paths.
     */
    public static abstract class Leaf extends SchemaElement {
        public Leaf(R48 app) {
            super(app);
        }

        @Override
        public final void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        }
    }

    /**
     * This function is kept static so it's easy to find calls. 
     */
    public static SchemaElement cast(SchemaElementIOP ise) {
        return (SchemaElement) ise;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.IOException;

/**
 * Magical Binding!
 * Maintains independent roots, allowing you to turn any structure into any other structure while still loading and saving in the original format.
 * The catch is, a modifyVal on this doesn't really work.
 * <p/>
 * Normal Structure <-> Data blob <-> MAGICAL FORMAT TRANSLATOR <-> {schema editors}
 * <p/>
 * Created on 29/07/17.
 */
public class MagicalBindingSchemaElement extends SchemaElement {
    public SchemaElement inner;
    public IMagicalBinder binder;

    public MagicalBindingSchemaElement(IMagicalBinder b, SchemaElement inn) {
        binder = b;
        inner = inn;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO trueTarget, ISchemaHost launcher, final SchemaPath truePath) {
        // Use subwatchers to create the backwards binding flow
        SchemaPath sp = createPath(trueTarget, truePath);
        return sp.editor.buildHoldingEditor(sp.targetElement, launcher, sp);
    }

    private SchemaPath createPath(final RubyIO trueTarget, final SchemaPath truePath) {
        final RubyIO bound = MagicalBinders.toBoundWithCache(binder, trueTarget);
        SchemaPath sp = new SchemaPath(new SchemaElement() {
            // This is a fake root element used for binding
            @Override
            public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                path = path.tagSEMonitor(target, this, true);
                return inner.buildHoldingEditor(target, launcher, path);
            }

            @Override
            public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                // Regarding what's going on here.
                // If we're being checked "externally" (think Autocorrect check),
                //  don't export just yet.
                // If inner gets modified, it'll trigger a cascading Path call.
                boolean wasTagged = path.monitorsSubelements;
                path = path.tagSEMonitor(target, this, true);
                inner.modifyVal(target, path, setDefault);
                if (wasTagged)
                    if (binder.applyBoundToTarget(target, trueTarget))
                        truePath.changeOccurred(setDefault);
            }
        }, new IObjectBackend.ILoadedObject() {
            @Override
            public IRIO getObject() {
                return bound;
            }

            @Override
            public void save() throws IOException {

            }
        });
        sp.contextualSchemas.putAll(truePath.contextualSchemas);
        return sp;
    }

    @Override
    public void modifyVal(final RubyIO trueTarget, final SchemaPath truePath, boolean setDefault) {
        if (binder.modifyVal(trueTarget, setDefault))
            truePath.changeOccurred(true);
        SchemaPath sp = createPath(trueTarget, truePath);
        sp.editor.modifyVal(sp.targetElement, sp, setDefault);
    }
}

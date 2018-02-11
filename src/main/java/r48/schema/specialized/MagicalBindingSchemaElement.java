/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

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
        // Bootstrap. Note the additional level of indirection to make sure you can return to your starting point.
        SchemaPath vrp = sp.otherIndex("");
        return sp.editor.buildHoldingEditor(sp.targetElement, new VirtualizedSchemaHost(truePath.findBack(), sp, truePath, sp, launcher), vrp);
    }

    private SchemaPath createPath(final RubyIO trueTarget, final SchemaPath truePath) {
        RubyIO bound = MagicalBinders.toBoundWithCache(binder, trueTarget);
        return new SchemaPath(new SchemaElement() {
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
        }, bound);
    }

    @Override
    public void modifyVal(final RubyIO trueTarget, final SchemaPath truePath, boolean setDefault) {
        SchemaPath sp = createPath(trueTarget, truePath);
        sp.editor.modifyVal(sp.targetElement, sp, setDefault);
    }

    // This is a fake schema host, sandboxing the "inner" root to avoid screwing up things royally.
    private class VirtualizedSchemaHost implements ISchemaHost {
        // When we go to the virtual path root, that means we're leaving.
        public SchemaPath pathRootReal, pathRootVirt, lastPathReal, lastPathVirt;
        public ISchemaHost trueHost;

        public VirtualizedSchemaHost(SchemaPath prr, SchemaPath prv, SchemaPath lpr, SchemaPath lpv, ISchemaHost parent) {
            pathRootReal = prr;
            pathRootVirt = prv;

            lastPathReal = lpr;
            lastPathVirt = lpv;

            trueHost = parent;
        }

        @Override
        public void switchObject(final SchemaPath nextVirt) {
            // This is where things get weird, to maintain a sense of a virtual stack.
            if (nextVirt.findBack() == lastPathVirt) {
                // Forward step?
                lastPathReal = switchObjectInner(lastPathReal, nextVirt);
            } else if (nextVirt.findBack() == lastPathVirt.findBack()) {
                // Side-step?
                lastPathReal = switchObjectInner(lastPathReal.findBack(), nextVirt);
            } else {
                // Backward-step?
                lastPathReal = switchObjectInner(lastPathReal.findBack().findBack(), nextVirt);
            }
            nextVirt.hasBeenUsed = true;
            lastPathVirt = nextVirt;
            trueHost.switchObject(lastPathReal);
        }

        private SchemaPath switchObjectInner(SchemaPath parent, final SchemaPath nextVirt) {
            return parent.newWindow(new SchemaElement() {
                @Override
                public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                    VirtualizedSchemaHost targ = VirtualizedSchemaHost.this;
                    if (launcher != trueHost) {
                        // THIS IS A CLONE! Generate a new VSH.
                        targ = new VirtualizedSchemaHost(pathRootReal, pathRootVirt, lastPathReal, lastPathVirt, launcher);
                    }
                    return nextVirt.editor.buildHoldingEditor(target, targ, nextVirt);
                }

                @Override
                public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                    nextVirt.editor.modifyVal(target, nextVirt, setDefault);
                }
            }, nextVirt.targetElement);
        }

        @Override
        public void launchOther(UIElement uiTest) {
            trueHost.launchOther(uiTest);
        }

        @Override
        public StuffRenderer getContextRenderer() {
            return trueHost.getContextRenderer();
        }

        @Override
        public ISchemaHost newBlank() {
            return trueHost.newBlank();
        }

        @Override
        public boolean isActive() {
            return trueHost.isActive();
        }

        @Override
        public SchemaPath getCurrentObject() {
            return lastPathVirt;
        }
    }
}

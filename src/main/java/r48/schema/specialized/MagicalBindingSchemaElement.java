/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.UIElement;
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
        // Bootstrap.
        return sp.editor.buildHoldingEditor(sp.targetElement, new VirtualizedSchemaHost(truePath, sp, launcher), sp);
    }

    private SchemaPath createPath(final RubyIO trueTarget, final SchemaPath truePath) {
        RubyIO bound = binder.targetToBound(trueTarget);
        return new SchemaPath(new SchemaElement() {
            // This is a fake root element used for binding
            @Override
            public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                path = path.tagSEMonitor(target, this);
                return inner.buildHoldingEditor(target, launcher, path);
            }

            @Override
            public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                inner.modifyVal(target, path, setDefault);
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

    // This is a fake schema host, sandboxing the "inner" root to avoid screwing up things royally
    private class VirtualizedSchemaHost implements ISchemaHost {
        public SchemaPath pathRootReal, lastPathVirt, pathRootVirt, lastPathReal;
        public ISchemaHost trueHost;

        public VirtualizedSchemaHost(SchemaPath path, SchemaPath virtR, ISchemaHost parent) {
            pathRootReal = path.findBack();
            lastPathReal = pathRootReal;
            lastPathVirt = virtR;
            pathRootVirt = virtR;
            trueHost = parent;
        }

        @Override
        public void switchObject(final SchemaPath nextObject) {
            final Runnable r = new Runnable() {
                boolean ignoreThis = false;

                @Override
                public void run() {
                    if (ignoreThis) {
                        ignoreThis = false;
                        return;
                    }
                    ignoreThis = true;
                    UIElement uie = nextObject.editor.buildHoldingEditor(nextObject.targetElement, VirtualizedSchemaHost.this, nextObject);
                    SchemaElement se = new TempDialogSchemaChoice(uie, this, nextObject);
                    if (nextObject.findBack() != lastPathVirt)
                        lastPathReal = pathRootReal;
                    if (nextObject == pathRootVirt) {
                        // We're leaving
                        trueHost.switchObject(pathRootReal);
                        lastPathReal = pathRootReal;
                        lastPathVirt = pathRootVirt;
                        return;
                    }
                    lastPathVirt = nextObject;
                    // Using an ordinary switch-object loses the control this instance has.
                    trueHost.switchObject(lastPathReal = lastPathReal.newWindow(se, nextObject.targetElement));
                }
            };
            r.run();
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

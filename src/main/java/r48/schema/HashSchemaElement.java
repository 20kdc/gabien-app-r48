/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIHHalfsplit;
import r48.ui.UIScrollVertLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 12/29/16.
 */
public class HashSchemaElement extends SchemaElement {
    public SchemaElement keyElem, valElem;

    public HashSchemaElement(SchemaElement keySE, SchemaElement opaqueSE) {
        keyElem = keySE;
        valElem = opaqueSE;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIScrollVertLayout uiSV = new UIScrollVertLayout();
        int vertSz = keyElem.maxHoldingHeight();
        if (valElem.maxHoldingHeight() > keyElem.maxHoldingHeight())
            vertSz = valElem.maxHoldingHeight();
        final int vertSzF = vertSz;
        // similar to the array schema, this is a containing object with access to local information
        Runnable rebuildSection = new Runnable() {
            @Override
            public void run() {
                final Runnable me = this;
                uiSV.panels.clear();
                for (RubyIO key : UITest.sortedKeys(target.hashVal.keySet())) {
                    final RubyIO kss = key;
                    // keys are opaque - this prevents MANY issues
                    UIHHalfsplit hs = new UIHHalfsplit(1, 4, (new OpaqueSchemaElement() {
                        @Override
                        public String getMessage() {
                            return "Key ";
                        }
                    }).buildHoldingEditor(key, launcher, path), valElem.buildHoldingEditor(target.hashVal.get(key), launcher, path.arrayHashIndex(key, "{" + key.toString() + "}")));
                    hs.setBounds(new Rect(0, 0, 100, vertSzF));
                    uiSV.panels.add(new UIAppendButton("-", hs, new Runnable() {
                        @Override
                        public void run() {
                            // remove
                            target.hashVal.remove(kss);
                            path.changeOccurred(false);
                            me.run();
                        }
                    }, FontSizes.schemaButtonTextHeight));
                }
                // pre-emptively set up a default key workspace
                final RubyIO rio = SchemaPath.createDefaultValue(keyElem, null);
                UIElement workspace = keyElem.buildHoldingEditor(rio, launcher, new SchemaPath(keyElem, rio, launcher));
                UIHHalfsplit workspaceHS = new UIHHalfsplit(2, 3, workspace, new UITextButton(FontSizes.schemaButtonTextHeight, "Add This Key", new Runnable() {
                    @Override
                    public void run() {
                        if (target.getHashVal(rio) == null) {
                            RubyIO rio2 = new RubyIO();
                            valElem.modifyVal(rio2, path.arrayHashIndex(rio, "{" + rio.toString() + "}"), true);
                            target.hashVal.put(rio, rio2);
                            // and this prevents further modification of the key
                            path.changeOccurred(false);
                            me.run();
                        }
                    }
                }));
                workspaceHS.setBounds(new Rect(0, 0, 0, keyElem.maxHoldingHeight()));
                uiSV.panels.add(workspaceHS);
            }
        };
        rebuildSection.run();
        return uiSV;
    }

    @Override
    public int maxHoldingHeight() {
        throw new RuntimeException("HashSchemaElement definitely has to be in a subwindow, since it grows.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        setDefault = IntegerSchemaElement.ensureType(target, '{', setDefault);
        if (setDefault) {
            target.hashVal = new HashMap<RubyIO, RubyIO>();
            path.changeOccurred(true);
        } else {
            if (target.hashVal == null) {
                target.hashVal = new HashMap<RubyIO, RubyIO>();
                path.changeOccurred(true);
            }
            for (Map.Entry<RubyIO, RubyIO> e : target.hashVal.entrySet())
                valElem.modifyVal(e.getValue(), path.arrayHashIndex(e.getKey(), "{" + e.getKey().toString() + "}"), false);
        }
    }
}

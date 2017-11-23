/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.IProxySchemaElement;
import r48.dbs.TXDB;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 12/29/16.
 */
public class HashSchemaElement extends SchemaElement {
    public SchemaElement keyElem, valElem;

    // NOTE: This is cloned from.
    private RubyIO defKeyWorkspace;

    public HashSchemaElement(SchemaElement keySE, SchemaElement opaqueSE) {
        keyElem = keySE;
        valElem = opaqueSE;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIScrollLayout uiSV = AggregateSchemaElement.createScrollSavingSVL(path, launcher, this, target);
        // similar to the array schema, this is a containing object with access to local information
        Runnable rebuildSection = new Runnable() {
            @Override
            public void run() {
                uiSV.panels.clear();
                for (RubyIO key : UITest.sortedKeys(target.hashVal.keySet())) {
                    final RubyIO kss = key;
                    // keys are opaque - this prevents MANY issues
                    UISplitterLayout hs = new UISplitterLayout((new OpaqueSchemaElement() {
                        @Override
                        public String getMessage(RubyIO v) {
                            SchemaElement ke = keyElem;
                            while (ke instanceof IProxySchemaElement)
                                ke = ((IProxySchemaElement) ke).getEntry();
                            if (ke instanceof EnumSchemaElement)
                                return ((EnumSchemaElement) ke).viewValue((int) v.fixnumVal, true);
                            return TXDB.get("Key " + v);
                        }
                    }).buildHoldingEditor(key, launcher, path), valElem.buildHoldingEditor(target.hashVal.get(key), launcher, path.arrayHashIndex(key, "{" + key.toString() + "}")), false, 1, 4);
                    uiSV.panels.add(new UIAppendButton("-", hs, new Runnable() {
                        @Override
                        public void run() {
                            // remove
                            target.hashVal.remove(kss);
                            path.changeOccurred(false);
                            // auto-updates
                        }
                    }, FontSizes.schemaButtonTextHeight));
                }
                // Set up a key workspace.
                if (defKeyWorkspace == null)
                    defKeyWorkspace = SchemaPath.createDefaultValue(keyElem, null);
                UIElement workspace = keyElem.buildHoldingEditor(defKeyWorkspace, launcher, path.otherIndex("(tempWSKey)"));
                UISplitterLayout workspaceHS = new UISplitterLayout(workspace, new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Add Key"), new Runnable() {
                    @Override
                    public void run() {
                        if (target.getHashVal(defKeyWorkspace) == null) {
                            RubyIO rio2 = new RubyIO();
                            RubyIO finWorkspace = new RubyIO().setDeepClone(defKeyWorkspace);
                            valElem.modifyVal(rio2, path.arrayHashIndex(finWorkspace, "{" + finWorkspace.toString() + "}"), true);
                            target.hashVal.put(finWorkspace, rio2);
                            // the deep clone prevents further modification of the key
                            path.changeOccurred(false);
                            // auto-updates
                        }
                    }
                }), false, 2, 3);
                uiSV.panels.add(workspaceHS);
            }
        };
        rebuildSection.run();
        return uiSV;
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

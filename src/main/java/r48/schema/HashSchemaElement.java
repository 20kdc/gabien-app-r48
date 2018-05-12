/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.IProxySchemaElement;
import r48.dbs.TXDB;
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
    public boolean flexible;

    // NOTE: This is cloned from.
    private RubyIO defKeyWorkspace;

    public HashSchemaElement(SchemaElement keySE, SchemaElement opaqueSE, boolean flexible) {
        keyElem = keySE;
        valElem = opaqueSE;
        this.flexible = flexible;
    }

    private SchemaPath.EmbedDataKey getSearchTermCharKey(RubyIO target, int key) {
        return new SchemaPath.EmbedDataKey(this, target, HashSchemaElement.class, "searchTerm/" + key);
    }
    private String getSearchTerm(RubyIO target, ISchemaHost launcher, SchemaPath context) {
        int p = 0;
        String s = "";
        while (true) {
            double ch = context.getEmbedSP(launcher, getSearchTermCharKey(target, p++));
            if (ch <= 0)
                break;
            s += (char) ch;
        }
        return s;
    }
    private void setSearchTerm(RubyIO target, ISchemaHost launcher, SchemaPath context, String term) {
        for (int i = 0; i <= term.length(); i++) {
            int v = 0;
            if (i != term.length())
                v = term.charAt(i);
            context.getEmbedMap(launcher).put(getSearchTermCharKey(target, i), (double) v);
        }
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIScrollLayout uiSV = AggregateSchemaElement.createScrollSavingSVL(path, launcher, this, target);
        // similar to the array schema, this is a containing object with access to local information
        Runnable rebuildSection = new Runnable() {
            // "Here come the hax!"
            // Also does relayout
            public void trigger() {
                run();
            }
            @Override
            public void run() {
                uiSV.panelsClear();
                final UITextBox searchBox = new UITextBox(getSearchTerm(target, launcher, path), FontSizes.schemaFieldTextHeight);
                searchBox.onEdit = new Runnable() {
                    @Override
                    public void run() {
                        setSearchTerm(target, launcher, path, searchBox.text);
                        trigger();
                    }
                };
                uiSV.panelsAdd(new UISplitterLayout(new UILabel(TXDB.get("Search Keys:"), FontSizes.schemaFieldTextHeight), searchBox, false, 0d));
                for (RubyIO key : UITest.sortedKeys(target.hashVal.keySet(), new IFunction<RubyIO, String>() {
                    @Override
                    public String apply(RubyIO rubyIO) {
                        return getKeyText(rubyIO);
                    }
                })) {
                    if (!getKeyText(key).contains(searchBox.text))
                        continue;
                    final RubyIO kss = key;
                    // keys are opaque - this prevents MANY issues
                    UIElement hsA = (new OpaqueSchemaElement() {
                        @Override
                        public String getMessage(RubyIO v) {
                            return getKeyText(v);
                        }
                    }).buildHoldingEditor(key, launcher, path);
                    UIElement hsB = valElem.buildHoldingEditor(target.hashVal.get(key), launcher, path.arrayHashIndex(key, "{" + getKeyText(key) + "}"));
                    UISplitterLayout hs = null;
                    if (flexible) {
                        hs = new UISplitterLayout(hsA, hsB, true, 0.0d);
                    } else {
                        hs = new UISplitterLayout(hsA, hsB, false, 1, 4);
                    }
                    uiSV.panelsAdd(new UIAppendButton("-", hs, new Runnable() {
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
                UISplitterLayout workspaceHS = new UISplitterLayout(workspace, new UITextButton(TXDB.get("Add Key"), FontSizes.schemaButtonTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        if (target.getHashVal(defKeyWorkspace) == null) {
                            RubyIO rio2 = new RubyIO();
                            RubyIO finWorkspace = new RubyIO().setDeepClone(defKeyWorkspace);
                            valElem.modifyVal(rio2, path.arrayHashIndex(finWorkspace, "{" + getKeyText(finWorkspace) + "}"), true);
                            target.hashVal.put(finWorkspace, rio2);
                            // the deep clone prevents further modification of the key
                            path.changeOccurred(false);
                            // auto-updates
                        }
                    }
                }), false, 2, 3);
                uiSV.panelsAdd(workspaceHS);
            }
        };
        rebuildSection.run();
        return uiSV;
    }

    private String getKeyText(RubyIO v) {
        SchemaElement ke = keyElem;
        while (ke instanceof IProxySchemaElement)
            ke = ((IProxySchemaElement) ke).getEntry();
        if (ke instanceof EnumSchemaElement)
            return ((EnumSchemaElement) ke).viewValue(v, true);
        return TXDB.get("Key " + v);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        setDefault = SchemaElement.ensureType(target, '{', setDefault);
        if (setDefault) {
            target.hashVal = new HashMap<RubyIO, RubyIO>();
            path.changeOccurred(true);
        } else {
            if (target.hashVal == null) {
                target.hashVal = new HashMap<RubyIO, RubyIO>();
                path.changeOccurred(true);
            }
            for (Map.Entry<RubyIO, RubyIO> e : target.hashVal.entrySet())
                valElem.modifyVal(e.getValue(), path.arrayHashIndex(e.getKey(), "{" + getKeyText(e.getKey()) + "}"), false);
        }
    }
}

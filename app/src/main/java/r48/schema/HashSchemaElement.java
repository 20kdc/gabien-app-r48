/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.specialized.OSStrHashMapSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 12/29/16.
 */
public class HashSchemaElement extends SchemaElement {
    public SchemaElement keyElem, valElem;
    public boolean flexible;

    public HashSchemaElement(App app, SchemaElement keySE, SchemaElement opaqueSE, boolean flexible) {
        super(app);
        keyElem = keySE;
        valElem = opaqueSE;
        this.flexible = flexible;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIScrollLayout uiSV = AggregateSchemaElement.createScrollSavingSVL(launcher, this, target);
        RubyIO preWorkspace = (RubyIO) launcher.getEmbedObject(this, target, "keyWorkspace");
        if (preWorkspace == null) {
            preWorkspace = new RubyIO().setNull();
            SchemaPath.setDefaultValue(preWorkspace, keyElem, null);
        } else {
            preWorkspace = new RubyIO().setDeepClone(preWorkspace);
        }
        final RubyIO keyWorkspace = preWorkspace;

        final SchemaPath setLocalePath = launcher.getCurrentObject();

        final SchemaPath rioPath = new SchemaPath(keyElem, new IObjectBackend.MockLoadedObject(keyWorkspace), new Runnable() {
            @Override
            public void run() {
                // This may occur from a different page (say, an enum selector), so the more complicated form must be used.
                launcher.setEmbedObject(setLocalePath, HashSchemaElement.this, target, "keyWorkspace", new RubyIO().setDeepClone(keyWorkspace));
            }
        });

        if (keyWorkspace.type == 'i') {
            while (target.getHashVal(keyWorkspace) != null) {
                // Try adding 1
                long plannedVal = ++keyWorkspace.fixnumVal;
                keyElem.modifyVal(keyWorkspace, rioPath, false);
                if ((keyWorkspace.type != 'i') || (keyWorkspace.fixnumVal != plannedVal)) {
                    // Let's not try that again
                    break;
                }
            }
        }
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
                final UITextBox searchBox = new UITextBox("", FontSizes.schemaFieldTextHeight);
                String oldSearchTerm = (String) launcher.getEmbedObject(HashSchemaElement.this, target, "searchTerm");
                if (oldSearchTerm != null)
                    searchBox.text = oldSearchTerm;
                searchBox.onEdit = new Runnable() {
                    @Override
                    public void run() {
                        launcher.setEmbedObject(HashSchemaElement.this, target, "searchTerm", searchBox.text);
                        trigger();
                    }
                };
                uiSV.panelsAdd(new UISplitterLayout(new UILabel(TXDB.get("Search:"), FontSizes.schemaFieldTextHeight), searchBox, false, 0d));

                AtomicInteger fw = new AtomicInteger(0);

                for (IRIO key : UITest.sortedKeysArr(target.getHashKeys(), new IFunction<IRIO, String>() {
                    @Override
                    public String apply(IRIO rubyIO) {
                        return getKeyText(rubyIO);
                    }
                })) {

                    final String keyText = getKeyText(key);
                    IRIO value = target.getHashVal(key);

                    boolean relevantToSearch = false;
                    relevantToSearch |= keyText.contains(searchBox.text);
                    relevantToSearch |= app.fmt.interpretParameter(value, valElem, true).contains(searchBox.text);
                    if (!relevantToSearch)
                        continue;

                    final IRIO kss = key;
                    // keys are opaque - this prevents MANY issues
                    UIElement hsA = new UILabel(keyText, FontSizes.schemaFieldTextHeight);
                    UIElement hsB = valElem.buildHoldingEditor(value, launcher, path.arrayHashIndex(key, "{" + keyText + "}"));
                    UIElement hs = null;
                    if (flexible) {
                        hs = new UISplitterLayout(hsA, hsB, true, 0.0d);
                    } else {
                        fw.set(Math.max(fw.get(), hsA.getSize().width));
                        hs = new UIFieldLayout(hsA, hsB, fw, true);
                    }
                    uiSV.panelsAdd(new UIAppendButton("-", hs, new Runnable() {
                        @Override
                        public void run() {
                            // remove
                            target.removeHashVal(kss);
                            path.changeOccurred(false);
                            // auto-updates
                        }
                    }, FontSizes.schemaFieldTextHeight));
                }
                // Set up a key workspace.
                UIElement workspace = keyElem.buildHoldingEditor(keyWorkspace, launcher, rioPath);
                UISplitterLayout workspaceHS = new UISplitterLayout(workspace, new UITextButton(TXDB.get("Add Key"), FontSizes.schemaFieldTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        if (target.getHashVal(keyWorkspace) == null) {
                            RubyIO finWorkspace = new RubyIO().setDeepClone(keyWorkspace);
                            IRIO rio2 = target.addHashVal(finWorkspace);
                            // Don't YET link this value into the schema path stuff.
                            // If we do that stuff might crash.
                            SchemaPath.setDefaultValue(rio2, valElem, finWorkspace);
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

    private String getKeyText(IRIO v) {
        SchemaElement ke = AggregateSchemaElement.extractField(keyElem, v);
        if (ke instanceof EnumSchemaElement)
            return ((EnumSchemaElement) ke).viewValue(v, true);
        if (ke instanceof OSStrHashMapSchemaElement)
            return OSStrHashMapSchemaElement.decode(app, v);
        return TXDB.get("Key ") + v;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        setDefault = SchemaElement.checkType(target, '{', null, setDefault);
        if (setDefault) {
            target.setHash();
        } else {
            for (IRIO e : target.getHashKeys()) {
                IRIO ek = target.getHashVal(e);
                valElem.modifyVal(ek, path.arrayHashIndex(e, "{" + getKeyText(e) + "}"), false);
            }
        }
    }
}

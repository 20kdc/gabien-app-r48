/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.UITest;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.specialized.OSStrHashMapSchemaElement;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.EmbedDataSlot;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created on 12/29/16.
 */
public class HashSchemaElement extends SchemaElement {
    public SchemaElement keyElem, valElem;
    public boolean flexible;
    public final EmbedDataKey<Double> scrollPointKey = new EmbedDataKey<>();
    public final EmbedDataKey<IRIO> keyWorkspaceKey = new EmbedDataKey<>();
    public final EmbedDataKey<String> searchTermKey = new EmbedDataKey<>();

    public HashSchemaElement(App app, SchemaElement keySE, SchemaElement opaqueSE, boolean flexible) {
        super(app);
        keyElem = keySE;
        valElem = opaqueSE;
        this.flexible = flexible;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIScrollLayout uiSV = AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target);
        EmbedDataSlot<IRIO> keyWorkspaceSlot = launcher.embedSlot(target, keyWorkspaceKey, null);
        IRIO preWorkspace = keyWorkspaceSlot.value;
        if (preWorkspace == null) {
            preWorkspace = new IRIOGeneric(app.ctxWorkspaceAppEncoding);
            SchemaPath.setDefaultValue(preWorkspace, keyElem, null);
        } else {
            preWorkspace = new IRIOGeneric(app.ctxWorkspaceAppEncoding).setDeepClone(preWorkspace);
        }
        final IRIO keyWorkspace = preWorkspace;

        final SchemaPath rioPath = new SchemaPath(keyElem, new ObjectRootHandle.Isolated(keyElem, keyWorkspace, "KeyWorkspace") {
            @Override
            public void objectRootModifiedPass(SchemaPath path) {
                super.objectRootModifiedPass(path);
                // This may occur from a different page (say, an enum selector).
                keyWorkspaceSlot.value = new IRIOGeneric(app.ctxWorkspaceAppEncoding).setDeepClone(keyWorkspace);
            }
        });

        if (keyWorkspace.getType() == 'i') {
            while (target.getHashVal(keyWorkspace.asKey()) != null) {
                // Try adding 1
                long plannedVal = keyWorkspace.getFX() + 1;
                keyWorkspace.setFX(plannedVal);
                keyElem.modifyVal(keyWorkspace, rioPath, false);
                if ((keyWorkspace.getType() != 'i') || (keyWorkspace.getFX() != plannedVal)) {
                    // Let's not try that again
                    break;
                }
            }
        }
        EmbedDataSlot<String> searchTermSlot = launcher.embedSlot(target, searchTermKey, "");
        // similar to the array schema, this is a containing object with access to local information
        Runnable rebuildSection = new Runnable() {
            // "Here come the hax!"
            // Also does relayout
            public void trigger() {
                run();
            }
            @Override
            public void run() {
                LinkedList<UIElement> elms = new LinkedList<>();
                final UITextBox searchBox = new UITextBox(searchTermSlot.value, app.f.schemaFieldTH);
                searchBox.onEdit = () -> {
                    searchTermSlot.value = searchBox.getText();
                    trigger();
                };
                elms.add(new UISplitterLayout(new UILabel(T.s.searchbar, app.f.schemaFieldTH), searchBox, false, 0d));

                AtomicInteger fw = new AtomicInteger(0);

                for (DMKey key : UITest.sortedKeysArr(target.getHashKeys(), new Function<RORIO, String>() {
                    @Override
                    public String apply(RORIO rubyIO) {
                        return getKeyText(rubyIO);
                    }
                })) {

                    final String keyText = getKeyText(key);
                    IRIO value = target.getHashVal(key);

                    boolean relevantToSearch = false;
                    String searchBoxText = searchBox.getText();
                    relevantToSearch |= keyText.contains(searchBoxText);
                    relevantToSearch |= app.format(value, valElem, EnumSchemaElement.Prefix.Prefix).contains(searchBoxText);
                    if (!relevantToSearch)
                        continue;

                    final DMKey kss = key;
                    // keys are opaque - this prevents MANY issues
                    UIElement hsA = new UILabel(keyText, app.f.schemaFieldTH);
                    UIElement hsB = valElem.buildHoldingEditor(value, launcher, path.arrayHashIndex(key, "{" + keyText + "}"));
                    UIElement hs = null;
                    if (flexible) {
                        hs = new UISplitterLayout(hsA, hsB, true, 0.0d);
                    } else {
                        fw.set(Math.max(fw.get(), hsA.getSize().width));
                        hs = new UIFieldLayout(hsA, hsB, fw, true);
                    }
                    UIAppendButton append = new UIAppendButton("-", hs, null, app.f.schemaFieldTH);
                    append.button.onClick = () -> {
                        app.ui.confirmDeletion(false, value, valElem, append, () -> {
                            // remove
                            target.removeHashVal(kss);
                            path.changeOccurred(false);
                        });
                    };
                    elms.add(append);
                }
                // Set up a key workspace.
                UIElement workspace = keyElem.buildHoldingEditor(keyWorkspace, launcher, rioPath);
                UISplitterLayout workspaceHS = new UISplitterLayout(workspace, new UITextButton(T.s.bAddKey, app.f.schemaFieldTH, new Runnable() {
                    @Override
                    public void run() {
                        DMKey dmk = keyWorkspace.asKey();
                        if (target.getHashVal(dmk) == null) {
                            IRIO rio2 = target.addHashVal(dmk);
                            // Don't YET link this value into the schema path stuff.
                            // If we do that stuff might crash.
                            SchemaPath.setDefaultValue(rio2, valElem, dmk);
                            // the deep clone prevents further modification of the key
                            path.changeOccurred(false);
                            // auto-updates
                        }
                    }
                }), false, 2, 3);
                elms.add(workspaceHS);
                uiSV.panelsSet(elms);
            }
        };
        rebuildSection.run();
        return uiSV;
    }

    private String getKeyText(RORIO v) {
        SchemaElement ke = AggregateSchemaElement.extractField(keyElem, v);
        if (ke instanceof EnumSchemaElement)
            return ((EnumSchemaElement) ke).viewValue(v, EnumSchemaElement.Prefix.Prefix);
        if (ke instanceof OSStrHashMapSchemaElement)
            return OSStrHashMapSchemaElement.decode(app, v);
        return T.s.keyPfx + v;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        setDefault = SchemaElement.checkType(target, '{', null, setDefault);
        if (setDefault) {
            target.setHash();
        } else {
            for (DMKey e : target.getHashKeys()) {
                IRIO ek = target.getHashVal(e);
                valElem.modifyVal(ek, path.arrayHashIndex(e, "{" + getKeyText(e) + "}"), false);
            }
        }
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        for (DMKey e : target.getHashKeys()) {
            IRIO ek = target.getHashVal(e);
            valElem.visit(ek, path.arrayHashIndex(e, "{" + getKeyText(e) + "}"), v, detailedPaths);
        }
    }
}

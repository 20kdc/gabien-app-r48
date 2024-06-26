/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import gabien.ui.*;
import gabien.ui.dialogs.UIAutoclosingPopupMenu;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UILabel;
import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.UITest;
import r48.io.data.IRIO;
import r48.map.UIMapView;
import r48.toolsets.utils.UIIDChanger;
import r48.ui.Art;
import r48.ui.UIAppendButton;
import r48.wm.IDuplicatableWindow;

import java.util.Stack;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Created on 12/29/16.
 */
public class SchemaHostImpl extends SchemaHostBase implements ISchemaHost, IDuplicatableWindow {
    private UIElement innerElemEditor;

    private final Stack<SchemaPath> backStack = new Stack<SchemaPath>();

    private UILabel pathLabel = new UILabel("", app.f.schemaPathTH);
    private UIAppendButton toolbarP = new UIAppendButton(Art.Symbol.Back.i(app), pathLabel, () -> {
        popObject();
    }, app.f.schemaPathTH);
    private UIAppendButton toolbarCp = new UIAppendButton(T.g.bCopy, toolbarP, () -> {
        app.setClipboardFrom(innerElem.targetElement);
    }, app.f.schemaPathTH);
    private UIAppendButton toolbarPs = new UIAppendButton(T.g.bPaste, toolbarCp, () -> {
        if (app.theClipboard == null) {
            app.ui.launchDialog(T.u.shcEmpty);
        } else {
            if (IRIO.rubyTypeEquals(innerElem.targetElement, app.theClipboard)) {
                try {
                    innerElem.targetElement.setDeepClone(app.theClipboard);
                } catch (Exception e) {
                    app.ui.launchDialog(T.u.shcIncompatible, e);
                }
                innerElem.changeOccurred(false);
                switchObject(innerElem);
            } else {
                app.ui.launchDialog(T.u.shcIncompatible);
            }
        }
    }, app.f.schemaPathTH);
    private UIAppendButton toolbarSandwich = new UIAppendButton("...", toolbarPs, () -> {
        app.ui.wm.createMenu(this.toolbarSandwich, new UIAutoclosingPopupMenu(new UIPopupMenu.Entry[] {
            new UIPopupMenu.Entry(T.g.wordSave, () -> {
                SchemaPath root = innerElem.findRoot();
                // perform a final verification of the file, just in case? (NOPE: Causes long save times on, say, LDBs)
                // root.editor.modifyVal(root.targetElement, root, false);
                app.odb.ensureSaved(root.hrIndex, root.root);
            }),
            new UIPopupMenu.Entry(T.u.shInspect, () -> {
                app.ui.wm.createWindow(new UITest(app, innerElem.targetElement, innerElem.root));
            }),
            new UIPopupMenu.Entry(T.u.shLIDC, () -> {
                // innerElem.editor and innerElem.targetElement must exist because SchemaHostImpl uses them.
                app.ui.wm.createWindow(new UIIDChanger(app, innerElem));
            })
        }, app.f.menuTH, app.f.menuS, true));
    }, app.f.schemaPathTH);

    // Used so this doesn't require too much changes when moved about
    private UIElement toolbarRoot = toolbarSandwich;

    private Consumer<SchemaPath> nudgeRunnable = new Consumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            nudged = true;
        }
    };

    public boolean windowOpen = false;
    public boolean stayClosed = false;
    private boolean nudged = false;

    public SchemaHostImpl(App app, @Nullable UIMapView rendererSource) {
        super(app, rendererSource);
        layoutAddElement(toolbarRoot);
        // Why is this scaled by main window size? Answer: Because the alternative is occasional Android version glitches.
        Size rootSize = app.ui.wm.getRootSize();
        Rect r = new Rect(0, 0, rootSize.width / 2, (rootSize.height / 3) * 2);
        setForcedBounds(null, r);
        setWantedSize(r);
    }

    @Override
    public void pushObject(SchemaPath nextObject) {
        if (innerElem != null)
            backStack.push(innerElem);
        switchObject(nextObject);
    }

    @Override
    public void popObject() {
        if (backStack.size() > 0)
            switchObject(backStack.pop());
    }

    private void switchObject(SchemaPath nextObject) {
        if (innerElem != null)
            app.odb.deregisterModificationHandler(innerElem.root, nudgeRunnable);
        while (nextObject.editor == null)
            nextObject = nextObject.parent;
        boolean doLaunch = false;
        if (!(windowOpen || stayClosed))
            doLaunch = true;

        replaceValidity();

        innerElem = nextObject;
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        app.odb.registerModificationHandler(innerElem.root, nudgeRunnable);

        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        layoutAddElement(toolbarRoot);
        layoutAddElement(innerElemEditor);
        // Not actually correct, but serves to help assist the layout code
        innerElemEditor.setForcedBounds(this, new Rect(getSize()));

        pathLabel.setText(innerElem.toStringMissingRoot());

        layoutRecalculateMetrics();

        if (doLaunch) {
            windowOpen = true;
            app.ui.wm.createWindow(this);
            app.ui.schemaHostImplRegister(this);
        }
    }

    @Override
    public int layoutGetHForW(int width) {
        if (innerElemEditor == null)
            return toolbarRoot.layoutGetHForW(width);
        return toolbarRoot.layoutGetHForW(width) + innerElemEditor.layoutGetHForW(width);
    }

    @Override
    protected void layoutRunImpl() {
        Size r = getSize();
        int tbHeight = toolbarRoot.layoutGetHForW(r.width);
        toolbarRoot.setForcedBounds(this, new Rect(0, 0, r.width, tbHeight));
        if (innerElemEditor != null)
            innerElemEditor.setForcedBounds(this, new Rect(0, tbHeight, r.width, r.height - tbHeight));
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        Size tb = toolbarRoot.getWantedSize();
        Size iee = tb;
        if (innerElemEditor != null)
            iee = innerElemEditor.getWantedSize();
        return new Size(Math.max(tb.width, iee.width), tb.height + iee.height);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        if (nudged) {
            switchObject(innerElem);
            nudged = false;
        }
    }

    @Override
    public boolean isActive() {
        return windowOpen;
    }

    @Override
    public void duplicateThisWindow() {
        if (innerElem.hasTempDialog()) {
            app.ui.launchDialog(T.u.shNoCloneTmp);
            return;
        }
        // This serves to ensure that cloning a window causes it to retain scroll and such,
        // while still keeping it independent.
        SchemaHostImpl next = (SchemaHostImpl) newBlank();
        next.backStack.addAll(backStack);
        next.backStack.push(innerElem);
        next.embedData = new EmbedDataTracker(next.backStack, embedData);
        next.popObject();
    }

    @Override
    public void onWindowClose() {
        windowOpen = false;
        stayClosed = true;
        if (innerElem != null) {
            app.odb.deregisterModificationHandler(innerElem.findRoot().root, nudgeRunnable);
            replaceValidity(); // We're not seeing modifications, so don't check validity.
            innerElem = null;
            innerElemEditor = null;
        }
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
    }

    @Override
    public void shutdown() {
        stayClosed = true;
    }

    @Override
    public boolean requestsUnparenting() {
        return stayClosed;
    }

    @Override
    public App getApp() {
        return app;
    }
}

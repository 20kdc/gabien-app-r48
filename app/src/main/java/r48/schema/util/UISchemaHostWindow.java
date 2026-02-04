/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.uslx.append.*;
import r48.schema.SchemaElement;
import r48.schema.op.SchemaOp;
import r48.ui.AppUI;
import r48.ui.Art;
import r48.ui.UIAppendButton;
import r48.wm.IDuplicatableWindow;

import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Created on 12/29/16.
 */
public class UISchemaHostWindow extends SchemaHostBase implements IDuplicatableWindow {
    private UIElement innerElemEditor;

    private final Stack<SchemaPath.Page> backStack = new Stack<>();

    private UILabel pathLabel = new UILabel("", app.f.schemaPathTH);
    private UIAppendButton toolbarP = new UIAppendButton(Art.Symbol.Back.i(app), pathLabel, () -> {
        popObject(false);
    }, app.f.schemaPathTH);
    private UIAppendButton toolbarCp = new UIAppendButton(T.g.bCopy, toolbarP, () -> {
        app.opCopy.invokeUI(U, innerElem, operatorContext);
    }, app.f.schemaPathTH);
    private UIAppendButton toolbarPs = new UIAppendButton(T.g.bPaste, toolbarCp, () -> {
        app.opPaste.invokeUI(U, innerElem, operatorContext);
    }, app.f.schemaPathTH);
    private UIAppendButton toolbarSandwich;

    // Used so this doesn't require too much changes when moved about
    private UIElement toolbarRoot;

    public boolean windowOpen = false;
    private boolean stayClosed = false;

    public UISchemaHostWindow(AppUI aui, @Nullable SchemaDynamicContext rendererSource) {
        super(aui, rendererSource);
        toolbarSandwich = new UIAppendButton(U, Art.Symbol.Operator.i(app), toolbarPs, app.f.schemaPathTH, () -> {
            return (UIElement) SchemaOp.createOperatorMenu(aui, innerElem, app.opSites.SCHEMA_HEADER, getValidity(), operatorContext, dynContext);
        });
        toolbarRoot = toolbarSandwich;
        layoutAddElement(toolbarRoot);
        // Why is this scaled by main window size? Answer: Because the alternative is occasional Android version glitches.
        Size rootSize = U.wm.getRootSize();
        Rect r = new Rect(0, 0, rootSize.width / 2, (rootSize.height / 3) * 2);
        setForcedBounds(null, r);
        setWantedSize(r);
    }

    @Override
    public void pushObject(SchemaPath.Page nextObject) {
        if (innerElem != null)
            backStack.push(innerElem);
        switchObject(nextObject);
    }

    @Override
    public void popObject(boolean canClose) {
        // System.out.println("popObject " + backStack.size() + " " + canClose);
        if (backStack.size() > 0) {
            switchObject(backStack.pop());
        } else if (canClose) {
            shutdown();
        }
    }

    @Override
    protected void refreshDisplay() {
        boolean doLaunch = false;
        if (!(windowOpen || stayClosed))
            doLaunch = true;

        operatorContext.clear();
        innerElemEditor = SchemaElement.cast(innerElem.editor).buildHoldingEditor(innerElem.targetElement, this, innerElem);

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
            U.wm.createWindow(this);
            U.schemaHostImplRegister(this);
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
    public void duplicateThisWindow() {
        if (innerElem.hasTempDialog()) {
            U.launchDialog(T.u.shNoCloneTmp);
            return;
        }
        // This serves to ensure that cloning a window causes it to retain scroll and such,
        // while still keeping it independent.
        UISchemaHostWindow next = (UISchemaHostWindow) newBlank();
        next.backStack.addAll(backStack);
        next.backStack.push(innerElem);
        next.embedData = new EmbedDataTracker(next.backStack, embedData);
        next.popObject(false);
    }

    @Override
    public void onWindowClose() {
        windowOpen = false;
        stayClosed = true;
        if (innerElem != null) {
            innerElem.root.deregisterModificationHandler(nudgeRunnable);
            replaceValidity(); // We're not seeing modifications, so don't check validity.
            innerElem = null;
            innerElemEditor = null;
        }
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
    }

    /**
     * Used to shutdown all schema hosts during a revert.
     * No-op if the host isn't active.
     */
    public void shutdown() {
        stayClosed = true;
    }

    @Override
    public boolean requestsUnparenting() {
        return stayClosed;
    }

    @Override
    public AppUI getAppUI() {
        return U;
    }
}

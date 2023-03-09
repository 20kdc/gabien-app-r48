/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import gabien.IPeripherals;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;
import r48.RubyIO;
import r48.UITest;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.schema.SchemaElement;
import r48.ui.Art;
import r48.ui.UIAppendButton;

import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Created on 12/29/16.
 */
public class SchemaHostImpl extends App.Pan implements ISchemaHost {
    private SchemaPath innerElem;
    private UIElement innerElemEditor;

    // Can be null - if not, the renderer is accessible.
    // Note that even if the map view "dies", it's renderer will stay around.
    private final @Nullable UIMapView contextView;

    private final Stack<SchemaPath> backStack = new Stack<SchemaPath>();

    private EmbedDataTracker embedData = new EmbedDataTracker();

    private UILabel pathLabel = new UILabel("", app.f.schemaPathTextHeight);
    private UIAppendButton toolbarP = new UIAppendButton(Art.Symbol.Back, pathLabel, new Runnable() {
        @Override
        public void run() {
            popObject();
        }
    }, app.f.schemaPathTextHeight);
    private UIAppendButton toolbarCp = new UIAppendButton(T.g.bCopy, toolbarP, new Runnable() {
        @Override
        public void run() {
            app.theClipboard = new RubyIO().setDeepClone(innerElem.targetElement);
        }
    }, app.f.schemaPathTextHeight);
    private UIAppendButton toolbarPs = new UIAppendButton(T.g.bPaste, toolbarCp, new Runnable() {
        @Override
        public void run() {
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
        }
    }, app.f.schemaPathTextHeight);
    private UIAppendButton toolbarS = new UIAppendButton(T.g.wordSave, toolbarPs, new Runnable() {
        @Override
        public void run() {
            SchemaPath root = innerElem.findRoot();
            // perform a final verification of the file, just in case? (NOPE: Causes long save times on, say, LDBs)
            // root.editor.modifyVal(root.targetElement, root, false);
            app.odb.ensureSaved(root.hrIndex, root.root);
        }
    }, app.f.schemaPathTextHeight);
    private UIAppendButton toolbarI = new UIAppendButton(Art.Symbol.Inspect, toolbarS, new Runnable() {
        @Override
        public void run() {
            app.ui.wm.createWindow(new UITest(app, innerElem.targetElement));
        }
    }, app.f.schemaPathTextHeight);
    private UIAppendButton toolbarC = new UIAppendButton(Art.Symbol.CloneFrame, toolbarI, new Runnable() {
        @Override
        public void run() {
            if (innerElem.hasTempDialog()) {
                app.ui.launchDialog(T.u.shNoCloneTmp);
                return;
            }
            // This serves to ensure that cloning a window causes it to retain scroll and such,
            // while still keeping it independent.
            SchemaHostImpl next = new SchemaHostImpl(app, contextView);
            next.backStack.addAll(backStack);
            next.backStack.push(innerElem);
            next.embedData = new EmbedDataTracker(next.backStack, embedData);
            next.popObject();
        }
    }, app.f.schemaPathTextHeight);

    // Used so this doesn't require too much changes when moved about
    private UIElement toolbarRoot = toolbarC;

    private IConsumer<SchemaPath> nudgeRunnable = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            nudged = true;
        }
    };

    private ISupplier<Boolean> validitySupplier;

    public boolean windowOpen = false;
    public boolean stayClosed = false;
    private boolean nudged = false;

    public SchemaHostImpl(App app, @Nullable UIMapView rendererSource) {
        super(app);
        contextView = rendererSource;
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

        validitySupplier = new ISupplier<Boolean>() {
            @Override
            public Boolean get() {
                return validitySupplier == this;
            }
        };

        innerElem = nextObject;
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        app.odb.registerModificationHandler(innerElem.root, nudgeRunnable);

        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        layoutAddElement(toolbarRoot);
        layoutAddElement(innerElemEditor);
        // Not actually correct, but serves to help assist the layout code
        innerElemEditor.setForcedBounds(this, new Rect(getSize()));

        pathLabel.text = innerElem.toStringMissingRoot();

        runLayout();

        if (doLaunch) {
            windowOpen = true;
            app.ui.wm.createWindow(this);
            app.ui.schemaHostImplRegister(this);
        }
    }

    @Override
    public void runLayout() {
        Size tb = toolbarRoot.getWantedSize();
        Size r = getSize();
        toolbarRoot.setForcedBounds(this, new Rect(0, 0, r.width, tb.height));
        Size iee = tb;
        if (innerElemEditor != null) {
            iee = innerElemEditor.getWantedSize();
            innerElemEditor.setForcedBounds(this, new Rect(0, tb.height, r.width, r.height - tb.height));
        }
        setWantedSize(new Size(Math.max(tb.width, iee.width), tb.height + iee.height));
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
    public StuffRenderer getContextRenderer() {
        if (contextView != null)
            return contextView.mapTable.renderer;
        return app.stuffRendererIndependent;
    }

    @Override
    public String getContextGUM() {
        if (contextView != null)
            return contextView.mapGUM;
        return null;
    }

    @Override
    public double getEmbedDouble(SchemaElement source, IRIO target, String prop) {
        return (Double) embedData.getEmbed(innerElem, source, target, prop, 0.0d);
    }

    @Override
    public void setEmbedDouble(SchemaElement source, IRIO target, String prop, double dbl) {
        embedData.setEmbed(innerElem, source, target, prop, dbl);
    }

    @Override
    public Object getEmbedObject(SchemaElement source, IRIO target, String prop) {
        return embedData.getEmbed(innerElem, source, target, prop, null);
    }

    @Override
    public void setEmbedObject(SchemaElement source, IRIO target, String prop, Object dbl) {
        embedData.setEmbed(innerElem, source, target, prop, dbl);
    }

    @Override
    public Object getEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop) {
        return embedData.getEmbed(locale, source, target, prop, null);
    }

    @Override
    public void setEmbedObject(SchemaPath locale, SchemaElement source, IRIO target, String prop, Object dbl) {
        embedData.setEmbed(locale, source, target, prop, dbl);
    }

    @Override
    public ISupplier<Boolean> getValidity() {
        return validitySupplier;
    }

    @Override
    public ISchemaHost newBlank() {
        return new SchemaHostImpl(app, contextView);
    }

    @Override
    public boolean isActive() {
        return windowOpen;
    }

    @Override
    public SchemaPath getCurrentObject() {
        return innerElem;
    }

    @Override
    public void launchOther(UIElement uiTest) {
        app.ui.wm.createWindow(uiTest);
    }

    @Override
    public String toString() {
        if (innerElem == null)
            return "(how'd you manage this then?)";
        String rootName = app.odb.getIdByObject(innerElem.root);
        if (rootName == null)
            rootName = "AnonObject";
        String name = rootName;
        name += innerElem.windowTitleSuffix();
        if (app.odb.getObjectModified(rootName))
            name += "*";
        return name;
    }

    @Override
    public void onWindowClose() {
        windowOpen = false;
        stayClosed = true;
        if (innerElem != null) {
            app.odb.deregisterModificationHandler(innerElem.findRoot().root, nudgeRunnable);
            validitySupplier = null; // We're not seeing modifications, so don't check validity.
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

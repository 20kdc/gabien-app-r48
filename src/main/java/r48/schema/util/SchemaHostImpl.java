/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.util;

import gabien.IPeripherals;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.TXDB;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.schema.SchemaElement;
import r48.ui.Art;
import r48.ui.UIAppendButton;

import java.util.Stack;

/**
 * Created on 12/29/16.
 */
public class SchemaHostImpl extends UIElement.UIPanel implements ISchemaHost {
    private IConsumer<UIElement> hostWindows;
    private SchemaPath innerElem;
    private UIElement innerElemEditor;

    // Can be null - if not, the renderer is accessible.
    // Note that even if the map view "dies", it's renderer will stay around.
    private final UIMapView contextView;

    private final Stack<SchemaPath> backStack = new Stack<SchemaPath>();

    private EmbedDataTracker embedData = new EmbedDataTracker();

    private UILabel pathLabel = new UILabel("", FontSizes.schemaPathTextHeight);
    private UIAppendButton toolbarP = new UIAppendButton(Art.Symbol.Back, pathLabel, new Runnable() {
        @Override
        public void run() {
            popObject();
        }
    }, FontSizes.schemaPathTextHeight);
    private UIAppendButton toolbarCp = new UIAppendButton(TXDB.get("Copy"), toolbarP, new Runnable() {
        @Override
        public void run() {
            AppMain.theClipboard = new RubyIO().setDeepClone(innerElem.targetElement);
        }
    }, FontSizes.schemaPathTextHeight);
    private UIAppendButton toolbarPs = new UIAppendButton(TXDB.get("Paste"), toolbarCp, new Runnable() {
        @Override
        public void run() {
            if (AppMain.theClipboard == null) {
                AppMain.launchDialog(TXDB.get("There is nothing in the clipboard."));
            } else {
                if (RubyIO.rubyTypeEquals(innerElem.targetElement, AppMain.theClipboard)) {
                    innerElem.targetElement.setDeepClone(AppMain.theClipboard);
                    SchemaPath sp = innerElem.findHighestSubwatcher();
                    sp.editor.modifyVal(sp.targetElement, sp, false);
                    innerElem.changeOccurred(false);
                    switchObject(innerElem);
                } else {
                    AppMain.launchDialog(TXDB.get("Incompatible clipboard and target."));
                }
            }
        }
    }, FontSizes.schemaPathTextHeight);
    private UIAppendButton toolbarS = new UIAppendButton(TXDB.get("Save"), toolbarPs, new Runnable() {
        @Override
        public void run() {
            SchemaPath root = innerElem.findRoot();
            // perform a final verification of the file, just in case? (NOPE: Causes long save times on, say, LDBs)
            // root.editor.modifyVal(root.targetElement, root, false);
            AppMain.objectDB.ensureSaved(root.hrIndex, root.lastArrayIndex);
        }
    }, FontSizes.schemaPathTextHeight);
    private UIAppendButton toolbarI = new UIAppendButton(Art.Symbol.Inspect, toolbarS, new Runnable() {
        @Override
        public void run() {
            hostWindows.accept(new UITest(innerElem.targetElement));
        }
    }, FontSizes.schemaPathTextHeight);
    private UIAppendButton toolbarC = new UIAppendButton(Art.Symbol.CloneFrame, toolbarI, new Runnable() {
        @Override
        public void run() {
            if (innerElem.hasTempDialog()) {
                AppMain.launchDialog(TXDB.get("Cannot clone, this contains a temporary dialog."));
                return;
            }
            // This serves to ensure that cloning a window causes it to retain scroll and such,
            // while still keeping it independent.
            SchemaHostImpl next = new SchemaHostImpl(hostWindows, contextView);
            next.backStack.addAll(backStack);
            next.backStack.push(innerElem);
            next.embedData = new EmbedDataTracker(next.backStack, embedData);
            next.popObject();
        }
    }, FontSizes.schemaPathTextHeight);

    // Used so this doesn't require too much changes when moved about
    private UIElement toolbarRoot = toolbarC;

    private IConsumer<SchemaPath> nudgeRunnable = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            nudged = true;
        }
    };

    public boolean windowOpen = false;
    public boolean stayClosed = false;
    private boolean nudged = false;

    public SchemaHostImpl(IConsumer<UIElement> rootElem, UIMapView rendererSource) {
        hostWindows = rootElem;
        contextView = rendererSource;
        layoutAddElement(toolbarRoot);
        // Why is this scaled by main window size? Answer: Because the alternative is occasional Android version glitches.
        Rect r = new Rect(0, 0, AppMain.mainWindowWidth / 2, (AppMain.mainWindowHeight / 3) * 2);
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
            AppMain.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
        while (nextObject.editor == null)
            nextObject = nextObject.parent;
        boolean doLaunch = false;
        if (!(windowOpen || stayClosed))
            doLaunch = true;
        innerElem = nextObject;
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        AppMain.objectDB.registerModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);

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
            hostWindows.accept(this);
            AppMain.schemaHostImplRegister(this);
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
        return AppMain.stuffRendererIndependent;
    }

    @Override
    public String getContextGUM() {
        if (contextView != null)
            return contextView.mapGUM;
        return null;
    }

    @Override
    public double getEmbedDouble(SchemaElement source, RubyIO target, String prop) {
        return (Double) embedData.getEmbed(innerElem, source, target, prop, 0.0d);
    }

    @Override
    public void setEmbedDouble(SchemaElement source, RubyIO target, String prop, double dbl) {
        embedData.setEmbed(innerElem, source, target, prop, dbl);
    }

    @Override
    public Object getEmbedObject(SchemaElement source, RubyIO target, String prop) {
        return embedData.getEmbed(innerElem, source, target, prop, null);
    }

    @Override
    public void setEmbedObject(SchemaElement source, RubyIO target, String prop, Object dbl) {
        embedData.setEmbed(innerElem, source, target, prop, dbl);
    }

    @Override
    public ISchemaHost newBlank() {
        return new SchemaHostImpl(hostWindows, contextView);
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
        hostWindows.accept(uiTest);
    }

    @Override
    public String toString() {
        if (innerElem == null)
            return TXDB.get("Loading...");
        String name = innerElem.findRoot().toString();
        if (AppMain.objectDB.getObjectModified(name))
            return name + "*";
        return name;
    }

    @Override
    public void onWindowClose() {
        windowOpen = false;
        if (innerElem != null) {
            AppMain.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
            innerElem = null;
            innerElemEditor = null;
        }
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
    }
}

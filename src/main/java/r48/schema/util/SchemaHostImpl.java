/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.util;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.UITest;
import r48.dbs.TXDB;
import r48.map.StuffRenderer;
import r48.map.UIMapView;
import r48.ui.Art;
import r48.ui.UIAppendButton;

import java.util.HashMap;

/**
 * Created on 12/29/16.
 */
public class SchemaHostImpl extends UIPanel implements ISchemaHost, IWindowElement {
    public IConsumer<UIElement> hostWindows;
    public SchemaPath innerElem;
    public UIElement innerElemEditor;

    // Can be null - if not, the renderer is accessible.
    // Note that even if the map view "dies", it's renderer will stay around.
    private final UIMapView contextView;

    public UILabel pathLabel = new UILabel("", FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarP = new UIAppendButton(Art.Symbol.Back, pathLabel, new Runnable() {
        @Override
        public void run() {
            if (innerElem.parent != null)
                switchObject(innerElem.findBack());
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarCp = new UIAppendButton(TXDB.get("Copy"), toolbarP, new Runnable() {
        @Override
        public void run() {
            AppMain.theClipboard = new RubyIO().setDeepClone(innerElem.targetElement);
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarPs = new UIAppendButton(TXDB.get("Paste"), toolbarCp, new Runnable() {
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
    public UIAppendButton toolbarS = new UIAppendButton(TXDB.get("Save"), toolbarPs, new Runnable() {
        @Override
        public void run() {
            SchemaPath root = innerElem.findRoot();
            // perform a final verification of the file, just in case? (NOPE: Causes long save times on, say, LDBs)
            // root.editor.modifyVal(root.targetElement, root, false);
            AppMain.objectDB.ensureSaved(root.hrIndex, root.lastArrayIndex);
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarI = new UIAppendButton(Art.Symbol.Inspect, toolbarS, new Runnable() {
        @Override
        public void run() {
            hostWindows.accept(new UITest(innerElem.targetElement));
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarC = new UIAppendButton(Art.Symbol.CloneFrame, toolbarI, new Runnable() {
        @Override
        public void run() {
            if (innerElem.hasTempDialog()) {
                AppMain.launchDialog(TXDB.get("Cannot clone, this contains a temporary dialog."));
                return;
            }
            // This serves to ensure that cloning a window causes it to retain scroll and such,
            // while still keeping it independent.
            HashMap<SchemaPath.EmbedDataKey, Double> info = innerElem.embedData.get(SchemaHostImpl.this);
            SchemaHostImpl next = new SchemaHostImpl(hostWindows, contextView);
            if (info != null)
                innerElem.getEmbedMap(next).putAll(info);
            next.switchObject(innerElem);
        }
    }, FontSizes.schemaPathTextHeight);

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
        // Why is this scaled by main window size? Answer: Because the alternative is occasional Android version glitches.
        setBounds(new Rect(0, 0, AppMain.mainWindowWidth / 2, (AppMain.mainWindowHeight / 3) * 2));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int h = toolbarC.getBounds().height;
        toolbarC.setBounds(new Rect(0, 0, r.width, h));
        if (innerElem != null)
            innerElemEditor.setBounds(new Rect(0, h, r.width, r.height - h));
    }

    @Override
    public void switchObject(SchemaPath nextObject) {
        if (innerElem != null)
            AppMain.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
        while (nextObject.editor == null)
            nextObject = nextObject.parent;
        boolean doLaunch = false;
        if (!(windowOpen || stayClosed))
            doLaunch = true;
        innerElem = nextObject;
        // Do this first, so that findLast has a proper target and can be found during UI build (rather than the per-frame saving)
        innerElem.hasBeenUsed = true;
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        AppMain.objectDB.registerModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);

        allElements.clear();
        allElements.add(toolbarC);
        allElements.add(innerElemEditor);

        pathLabel.Text = innerElem.toStringMissingRoot();

        if (doLaunch) {
            windowOpen = true;
            hostWindows.accept(this);
            AppMain.schemaHostImplRegister(this);
        }
        setBounds(getBounds());
    }

    @Override
    public void updateAndRender(int ox, int oy, double DeltaTime, boolean select, IGrInDriver igd) {
        if (nudged) {
            switchObject(innerElem);
            nudged = false;
        }
        super.updateAndRender(ox, oy, DeltaTime, select, igd);
    }

    @Override
    public StuffRenderer getContextRenderer() {
        if (contextView != null)
            return contextView.mapTable.renderer;
        return AppMain.stuffRendererIndependent;
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
    public boolean wantsSelfClose() {
        return false;
    }

    @Override
    public void windowClosed() {
        windowOpen = false;
        if (innerElem != null) {
            AppMain.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
            innerElem = null;
            innerElemEditor = null;
        }
    }
}

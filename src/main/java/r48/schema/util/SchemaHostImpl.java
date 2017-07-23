/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
import r48.ui.UIAppendButton;
import gabien.ui.UIScrollLayout;

/**
 * Created on 12/29/16.
 */
public class SchemaHostImpl extends UIPanel implements ISchemaHost, IWindowElement {
    public IConsumer<UIElement> hostWindows;
    public SchemaPath innerElem;
    public UIElement innerElemEditor;

    // Can be null - if not, the renderer is accessible.
    private final UIMapView contextView;

    public UILabel pathLabel = new UILabel("", FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarP = new UIAppendButton(TXDB.get(".."), pathLabel, new Runnable() {
        @Override
        public void run() {
            if (innerElem.parent != null)
                switchObject(innerElem.findBack());
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarCp = new UIAppendButton(TXDB.get("Cp."), toolbarP, new Runnable() {
        @Override
        public void run() {
            AppMain.theClipboard = new RubyIO().setDeepClone(innerElem.targetElement);
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarPs = new UIAppendButton(TXDB.get("Ps."), toolbarCp, new Runnable() {
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
            // perform a final verification of the file, just in case.
            root.editor.modifyVal(root.targetElement, root, false);
            AppMain.objectDB.ensureSaved(root.hrIndex, root.lastArrayIndex);
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarI = new UIAppendButton(TXDB.get("I"), toolbarS, new Runnable() {
        @Override
        public void run() {
            hostWindows.accept(new UITest(innerElem.targetElement));
        }
    }, FontSizes.schemaPathTextHeight);
    public UIAppendButton toolbarC = new UIAppendButton(TXDB.get("C"), toolbarI, new Runnable() {
        @Override
        public void run() {
            SchemaHostImpl next = new SchemaHostImpl(hostWindows, contextView);
            next.switchObject(innerElem);
            hostWindows.accept(next);
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
        setBounds(new Rect(0, 0, 400, 300));
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
        if (innerElem != null) {
            if (innerElemEditor instanceof UIScrollLayout)
                innerElem.scrollValue = ((UIScrollLayout) innerElemEditor).scrollbar.scrollPoint;
            AppMain.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
        }
        while (nextObject.editor == null)
            nextObject = nextObject.parent;
        boolean doLaunch = false;
        if (!(windowOpen || stayClosed))
            doLaunch = true;
        innerElem = nextObject;
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        if (innerElemEditor instanceof UIScrollLayout)
            ((UIScrollLayout) innerElemEditor).scrollbar.scrollPoint = innerElem.scrollValue;
        innerElem.hasBeenUsed = true;
        AppMain.objectDB.registerModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);

        allElements.clear();
        allElements.add(toolbarC);
        allElements.add(innerElemEditor);

        pathLabel.Text = innerElem.toStringMissingRoot();

        if (doLaunch) {
            windowOpen = true;
            hostWindows.accept(this);
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
            return contextView.renderer;
        return AppMain.stuffRendererIndependent;
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
        if (innerElem != null)
            AppMain.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
    }
}

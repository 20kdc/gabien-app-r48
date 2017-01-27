/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema.util;

import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.*;
import gabienapp.map.StuffRenderer;
import gabienapp.ui.UIAppendButton;
import gabienapp.ui.UIScrollVertLayout;

/**
 * Created on 12/29/16.
 */
public class SchemaHostImpl extends UIPanel implements ISchemaHost, IWindowElement {
    public IConsumer<UIElement> hostWindows;
    public SchemaPath innerElem;
    public UIElement innerElemEditor;

    public StuffRenderer stuffRenderer = Application.stuffRenderer;

    public UILabel pathLabel = new UILabel("", false);
    public UIAppendButton toolbarP = new UIAppendButton("..", pathLabel, new Runnable() {
        @Override
        public void run() {
            if (innerElem.parent != null)
                switchObject(innerElem.findBack());
        }
    }, false);
    public UIAppendButton toolbarCp = new UIAppendButton("Cp.", toolbarP, new Runnable() {
        @Override
        public void run() {
            Application.theClipboard = new RubyIO().setDeepClone(innerElem.targetElement);
        }
    }, false);
    public UIAppendButton toolbarPs = new UIAppendButton("Ps.", toolbarCp, new Runnable() {
        @Override
        public void run() {
            if (Application.theClipboard == null) {
                hostWindows.accept(new UILabel("nothing in clipboard", true));
            } else {
                if (RubyIO.rubyTypeEquals(innerElem.targetElement, Application.theClipboard)) {
                    innerElem.targetElement.setDeepClone(Application.theClipboard);
                    SchemaPath sp = innerElem.lastArray;
                    if (sp == null)
                        sp = innerElem.findRoot();
                    sp.editor.modifyVal(sp.targetElement, sp, false);
                    innerElem.changeOccurred(false);
                    switchObject(innerElem);
                } else {
                    hostWindows.accept(new UILabel("incompatible", true));
                }
            }
        }
    }, false);
    public UIAppendButton toolbarS = new UIAppendButton("Save", toolbarPs, new Runnable() {
        @Override
        public void run() {
            SchemaPath root = innerElem.findRoot();
            // perform a final verification of the file, just in case.
            root.editor.modifyVal(root.targetElement, root, false);
            Application.objectDB.ensureSaved(root.hrIndex, root.lastArrayIndex);
        }
    }, false);
    public UIAppendButton toolbarI = new UIAppendButton("Insp", toolbarS, new Runnable() {
        @Override
        public void run() {
            hostWindows.accept(new UITest(innerElem.targetElement));
        }
    }, false);

    private Runnable nudgeRunnable = new Runnable() {
        @Override
        public void run() {
            nudged = true;
        }
    };

    public boolean windowOpen = false;
    public boolean stayClosed = false;
    private boolean nudged = false;

    public SchemaHostImpl(IConsumer<UIElement> rootElem) {
        hostWindows = rootElem;
        setBounds(new Rect(0, 0, 320, 200));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int h = toolbarI.getBounds().height;
        toolbarI.setBounds(new Rect(0, 0, r.width, h));
        if (innerElem != null)
            innerElemEditor.setBounds(new Rect(0, h, r.width, r.height - h));
    }

    @Override
    public void switchObject(SchemaPath nextObject) {
        if (innerElem != null) {
            if (innerElemEditor instanceof UIScrollVertLayout)
                innerElem.scrollValue = ((UIScrollVertLayout) innerElemEditor).scrollbar.scrollPoint;
            Application.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
        }
        while (nextObject.editor == null)
            nextObject = nextObject.parent;
        boolean doLaunch = false;
        if (!(windowOpen || stayClosed))
            doLaunch = true;
        innerElem = nextObject;
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        if (innerElemEditor instanceof UIScrollVertLayout)
            ((UIScrollVertLayout) innerElemEditor).scrollbar.scrollPoint = innerElem.scrollValue;
        innerElem.hasBeenUsed = true;
        Application.objectDB.registerModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);

        allElements.clear();
        allElements.add(toolbarI);
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
        return stuffRenderer;
    }

    @Override
    public void launchOther(UIElement uiTest) {
        hostWindows.accept(uiTest);
    }

    @Override
    public String toString() {
        if (innerElem == null)
            return "loading";
        String name = innerElem.findRoot().toString();
        if (Application.objectDB.getObjectModified(name))
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
            Application.objectDB.deregisterModificationHandler(innerElem.findRoot().targetElement, nudgeRunnable);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.*;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.schema.util.SchemaPath;
import r48.ui.Art;
import r48.ui.UIAppendButton;

/**
 * The system for editing a given cell.
 * Created on 2/17/17.
 */
public class UICellEditingPanel extends App.Pan {
    public UICellSelectionPanel cellSelectionPanel;
    public GenposFramePanelController root;
    public UISplitterLayout[] halfsplits;

    // This is used so UICellSelectionPanel can notify this panel of cell changes.
    // -1 is never used.
    public int lastCCN = -1;

    public UICellEditingPanel(UICellSelectionPanel csp, GenposFramePanelController rmAnimRootPanel) {
        super(csp.app);
        root = rmAnimRootPanel;
        cellSelectionPanel = csp;
        String[] properties = root.frame.getCellProps();
        // Filled in here
        halfsplits = new UISplitterLayout[properties.length];
        recreateHalfSplits();
        forceToRecommended();
    }

    private void recreateHalfSplits() {
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        String[] properties = root.frame.getCellProps();
        for (int i = 0; i < halfsplits.length; i++) {
            UIElement ed = createPropertyEditor(i);
            UIElement leftSide = new UILabel(properties[i], app.f.schemaFieldTH);
            halfsplits[i] = new UISplitterLayout(leftSide, ed, false, 1);
            layoutAddElement(halfsplits[i]);
        }
    }

    private UIElement createPropertyEditor(final int i) {
        if (cellSelectionPanel.cellNumber != -1) {
            SchemaPath sp = root.frame.getCellProp(cellSelectionPanel.cellNumber, i);
            // Used to have to 'correct host' here, but host's very existence was bad for window cloning and also totally unnecessary
            UIElement uie = sp.editor.buildHoldingEditor(sp.targetElement, root.hostLauncher, sp);
            if (root.tweening != null) {
                if (root.frame.getCellPropTweening(cellSelectionPanel.cellNumber, i) != null) {
                    final IGenposTweeningManagement.KeyTrack keytrack = root.tweening.propertyKeytrack(i);
                    final boolean keyed = root.tweening.propertyKeyed(i, keytrack);
                    // This is a 'delete keyframe' button
                    uie = new UIAppendButton((keyed ? Art.Symbol.Keyframe : Art.Symbol.Tween).i(app), uie, () -> {
                        if (keyed)
                            root.tweening.disablePropertyKey(i, keytrack);
                    }, app.f.schemaFieldTH);
                }
            }
            return uie;
        }
        return new UIEmpty();
    }

    public void somethingChanged() {
        recreateHalfSplits();
        layoutRecalculateMetrics();
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        int n = cellSelectionPanel.cellChangeNotificationNumber;
        if (lastCCN != n) {
            lastCCN = n;
            somethingChanged();
        }
        super.update(deltaTime, selected, peripherals);
    }

    @Override
    public int layoutGetHForW(int width) {
        int h = 0;
        for (int i = 0; i < halfsplits.length; i++)
            h += halfsplits[i].layoutGetHForW(width);
        return h;
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        int w = 0;
        int h = 0;
        for (int i = 0; i < halfsplits.length; i++) {
            Size ws = halfsplits[i].getWantedSize();
            w = Math.max(w, ws.width);
            h += ws.height;
        }
        return new Size(w, h);
    }

    @Override
    protected void layoutRunImpl() {
        int h = 0;
        Size r = getSize();
        for (int i = 0; i < halfsplits.length; i++) {
            Size ws = halfsplits[i].getWantedSize();
            halfsplits[i].setForcedBounds(this, new Rect(0, h, r.width, ws.height));
            h += ws.height;
        }
    }
}

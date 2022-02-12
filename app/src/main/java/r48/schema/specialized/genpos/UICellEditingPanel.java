/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.IPeripherals;
import gabien.ui.*;
import r48.FontSizes;
import r48.schema.HiddenSchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.Art;
import r48.ui.UIAppendButton;

/**
 * The system for editing a given cell.
 * Created on 2/17/17.
 */
public class UICellEditingPanel extends UIElement.UIPanel {
    public UICellSelectionPanel cellSelectionPanel;
    public GenposFramePanelController root;
    public UISplitterLayout[] halfsplits;

    // This is used so UICellSelectionPanel can notify this panel of cell changes.
    // -1 is never used.
    public int lastCCN = -1;

    public UICellEditingPanel(UICellSelectionPanel csp, GenposFramePanelController rmAnimRootPanel) {
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
            UIElement leftSide = new UILabel(properties[i], FontSizes.schemaFieldTextHeight);
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
                    uie = new UIAppendButton(keyed ? Art.Symbol.Keyframe : Art.Symbol.Tween, uie, new Runnable() {
                        @Override
                        public void run() {
                            if (keyed)
                                root.tweening.disablePropertyKey(i, keytrack);
                        }
                    }, FontSizes.schemaFieldTextHeight);
                }
            }
            return uie;
        }
        return HiddenSchemaElement.makeHiddenElement();
    }

    public void somethingChanged() {
        recreateHalfSplits();
        runLayout();
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
    public void runLayout() {
        int w = 0;
        int h = 0;
        Size r = getSize();
        for (int i = 0; i < halfsplits.length; i++) {
            Size ws = halfsplits[i].getWantedSize();
            halfsplits[i].setForcedBounds(this, new Rect(0, h, r.width, ws.height));
            w = Math.max(w, ws.width);
            h += ws.height;
        }
        setWantedSize(new Size(w, h));
    }
}

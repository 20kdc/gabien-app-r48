/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.schema.util.SchemaPath;

import java.util.Collections;

/**
 * The system for editing a given cell.
 * Created on 2/17/17.
 */
public class UICellEditingPanel extends UIPanel {
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
        setBounds(new Rect(0, 0, 32, recreateHalfSplits()));
    }

    private int recreateHalfSplits() {
        String[] properties = root.frame.getCellProps();
        int h = 0;
        for (int i = 0; i < halfsplits.length; i++) {
            UIElement ed = createPropertyEditor(i);
            halfsplits[i] = new UISplitterLayout(new UILabel(properties[i], FontSizes.schemaFieldTextHeight), ed, false, 3, 5);
            h += halfsplits[i].getBounds().height;
        }
        return h;
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int p = 0;
        for (int i = 0; i < halfsplits.length; i++) {
            int h = halfsplits[i].getBounds().height;
            halfsplits[i].setBounds(new Rect(0, p, r.width, h));
            p += h;
        }
    }

    private UIElement createPropertyEditor(final int i) {
        if (cellSelectionPanel.cellNumber != -1) {
            SchemaPath sp = root.frame.getCellProp(cellSelectionPanel.cellNumber, i);
            // Used to have to 'correct host' here, but host's very existence was bad for window cloning and also totally unnecessary
            return sp.editor.buildHoldingEditor(sp.targetElement, root.hostLauncher, sp);
        }
        UIPanel panel = new UIPanel();
        panel.setBounds(new Rect(0, 0, 0, 0));
        return panel;
    }

    public void somethingChanged() {
        allElements.clear();
        int cell = cellSelectionPanel.cellNumber;
        if (cell != -1) {
            recreateHalfSplits();
            setBounds(getBounds());
            Collections.addAll(allElements, halfsplits);
        }
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        int n = cellSelectionPanel.cellChangeNotificationNumber;
        if (lastCCN != n) {
            lastCCN = n;
            somethingChanged();
        }
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }
}

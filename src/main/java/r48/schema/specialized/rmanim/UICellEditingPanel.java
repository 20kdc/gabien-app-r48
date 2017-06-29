/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.RubyTable;
import r48.dbs.TXDB;

import java.util.Collections;

/**
 * The system for editing a given cell.
 * Created on 2/17/17.
 */
public class UICellEditingPanel extends UIPanel {
    public UICellSelectionPanel cellSelectionPanel;
    public RMAnimRootPanel root;
    public UISplitterLayout[] halfsplits;
    public Runnable[] cellChangeNotificationHandlers;

    // This is used so UICellSelectionPanel can notify this panel of cell changes.
    // -1 is never used.
    public int lastCCN = -1;

    public UICellEditingPanel(UICellSelectionPanel csp, RMAnimRootPanel rmAnimRootPanel) {
        root = rmAnimRootPanel;
        cellSelectionPanel = csp;
        String[] properties = new String[] {
                TXDB.get("cellID"),
                TXDB.get("xPos"),
                TXDB.get("yPos"),
                TXDB.get("scale"),
                TXDB.get("angle"),
                TXDB.get("mirror"),
                TXDB.get("opacity"),
                TXDB.get("blendType")
        };
        // Filled in here
        halfsplits = new UISplitterLayout[properties.length];
        // Filled in by createPropertyEditor
        cellChangeNotificationHandlers = new Runnable[properties.length];
        int h = 0;
        for (int i = 0; i < halfsplits.length; i++) {
            UIElement ed = createPropertyEditor(i);
            halfsplits[i] = new UISplitterLayout(new UILabel(properties[i], FontSizes.rmaPropertyFontSize), ed, false, 3, 5);
            h += halfsplits[i].getBounds().height;
        }
        setBounds(new Rect(0, 0, 32, h));
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

    private RubyTable getCellTable() {
        return new RubyTable(root.getFrame().getInstVarBySymbol("@cell_data").userVal);
    }

    private UIElement createPropertyEditor(final int i) {
        // general case
        final UINumberBox unb = new UINumberBox(FontSizes.rmaPropertyFontSize);

        cellChangeNotificationHandlers[i] = new Runnable() {
            @Override
            public void run() {
                final RubyTable rt = getCellTable();
                final int ct = cellSelectionPanel.cellNumber;
                unb.number = rt.getTiletype(ct, i, 0);
                unb.onEdit = new Runnable() {
                    @Override
                    public void run() {
                        rt.setTiletype(ct, i, 0, (short) unb.number);
                        root.updateNotify.run();
                    }
                };
            }
        };

        return unb;
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        int cell = cellSelectionPanel.cellNumber;
        int n = cellSelectionPanel.cellChangeNotificationNumber;
        if (lastCCN != n) {
            lastCCN = n;
            allElements.clear();
            if (cell != -1) {
                Collections.addAll(allElements, halfsplits);
                for (Runnable r : cellChangeNotificationHandlers)
                    r.run();
            }
        }
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }
}

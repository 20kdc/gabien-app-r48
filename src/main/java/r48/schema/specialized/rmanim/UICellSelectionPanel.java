/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.ui.UIScrollVertLayout;

/**
 * Created on 2/17/17.
 */
public class UICellSelectionPanel extends UIPanel {
    // Instead, using getCell will ensure it gets corrected.
    public int cellNumber = -1;
    public int cellChangeNotificationNumber = 0;

    public RMAnimRootPanel root;

    public UIScrollVertLayout selectionPanel = new UIScrollVertLayout();

    public UICellSelectionPanel(RMAnimRootPanel rmAnimRootPanel) {
        root = rmAnimRootPanel;
        allElements.add(selectionPanel);
    }

    private void rebuildSelectionPanel(RubyTable table) {
        selectionPanel.panels.clear();
        for (int i = 0; i < table.width; i++) {
            final int i2 = i;
            UIElement button = new UITextButton(FontSizes.rmaPropertyFontSize, "Cell " + i2, new Runnable() {
                @Override
                public void run() {
                    cellNumber = i2;
                    cellChangeNotificationNumber++;
                }
            });
            selectionPanel.panels.add(button);
        }
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        selectionPanel.setBounds(new Rect(0, 0, r.width, r.height));
    }

    public void frameChanged() {
        RubyTable rt = new RubyTable(root.getFrame().getInstVarBySymbol("@cell_data").userVal);
        if (cellNumber >= rt.width) {
            cellNumber = -1;
            cellChangeNotificationNumber++;
        }
        if (cellNumber < -1) {
            cellNumber = -1;
            cellChangeNotificationNumber++;
        }
        rebuildSelectionPanel(rt);
    }
}

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
import r48.ui.UIAppendButton;
import gabien.ui.UIScrollLayout;

import java.util.LinkedList;

/**
 * Created on 2/17/17.
 */
public class UICellSelectionPanel extends UIPanel {
    // Instead, using getCell will ensure it gets corrected.
    public int cellNumber = -1;
    public int cellChangeNotificationNumber = 0;

    public RMAnimRootPanel root;

    public UIScrollLayout selectionPanel = new UIScrollLayout(true);

    public UICellSelectionPanel(RMAnimRootPanel rmAnimRootPanel) {
        root = rmAnimRootPanel;
        allElements.add(selectionPanel);
    }

    private void rebuildSelectionPanel(final RubyTable table) {
        selectionPanel.panels.clear();
        for (int i = 0; i < table.width; i++) {
            final int i2 = i;
            addAdditionButton(table, selectionPanel.panels, i2);
            String prefix = cellNumber == i2 ? ">" : " ";
            UIElement button = new UITextButton(FontSizes.rmaPropertyFontSize, prefix + "Cell " + i2, new Runnable() {
                @Override
                public void run() {
                    cellNumber = i2;
                    cellChangeNotificationNumber++;
                    rebuildSelectionPanel(table);
                }
            });
            selectionPanel.panels.add(new UIAppendButton("-", button, new Runnable() {
                @Override
                public void run() {
                    // delete cell
                    RubyIO frame = root.getFrame();
                    RubyIO frameData = frame.getInstVarBySymbol("@cell_data");
                    frame.getInstVarBySymbol("@cell_max").fixnumVal = table.width - 1;
                    RubyTable newTable = new RubyTable(table.width - 1, 8, 1, new int[1]);
                    for (int p = 0; p < 8; p++) {
                        for (int j = 0; j < i2; j++)
                            newTable.setTiletype(j, p, 0, table.getTiletype(j, p, 0));
                        for (int j = i2 + 1; j < table.width; j++)
                            newTable.setTiletype(j - 1, p, 0, table.getTiletype(j, p, 0));
                    }
                    frameData.userVal = newTable.innerBytes;
                    root.updateNotify.run();
                    cellNumber = -1;
                    cellChangeNotificationNumber++;
                    rebuildSelectionPanel(newTable);
                }
            }, FontSizes.rmaPropertyFontSize));
        }
        addAdditionButton(table, selectionPanel.panels, table.width);
    }

    private void addAdditionButton(final RubyTable table, final LinkedList<UIElement> panels, final int i2) {
        panels.add(new UITextButton(FontSizes.rmaPropertyFontSize, "<add cell here>", new Runnable() {
            @Override
            public void run() {
                // delete cell
                RubyIO frame = root.getFrame();
                RubyIO frameData = frame.getInstVarBySymbol("@cell_data");
                frame.getInstVarBySymbol("@cell_max").fixnumVal = table.width + 1;
                RubyTable newTable = new RubyTable(table.width + 1, 8, 1, new int[1]);
                short[] initValues = new short[] {
                        1, 0, 0, 100, 0, 0, 255, 1
                };
                for (int p = 0; p < 8; p++) {
                    for (int j = 0; j < i2; j++)
                        newTable.setTiletype(j, p, 0, table.getTiletype(j, p, 0));
                    for (int j = i2; j < table.width; j++)
                        newTable.setTiletype(j + 1, p, 0, table.getTiletype(j, p, 0));
                    newTable.setTiletype(i2, p, 0, initValues[p]);
                }
                frameData.userVal = newTable.innerBytes;
                root.updateNotify.run();
                cellNumber = i2;
                cellChangeNotificationNumber++;
                rebuildSelectionPanel(newTable);
            }
        }));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        selectionPanel.setBounds(new Rect(0, 0, r.width, r.height));
    }

    public void frameChanged() {
        RubyTable rt = new RubyTable(root.getFrame().getInstVarBySymbol("@cell_data").userVal);
        if (cellNumber >= rt.width)
            cellNumber = -1;
        if (cellNumber < -1)
            cellNumber = -1;
        // The actual cell changed, even if the number didn't
        cellChangeNotificationNumber++;
        rebuildSelectionPanel(rt);
    }
}

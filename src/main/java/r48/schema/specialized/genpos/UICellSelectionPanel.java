/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
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

    public IGenposFrame root;

    public UIScrollLayout selectionPanel = new UIScrollLayout(true);

    public UICellSelectionPanel(IGenposFrame rmAnimRootPanel) {
        selectionPanel.scrollbar.setBounds(new Rect(0, 0, 8, 8));
        root = rmAnimRootPanel;
        allElements.add(selectionPanel);
    }

    private void rebuildSelectionPanel() {
        selectionPanel.panels.clear();
        final int cellCount = root.getCellCount();
        for (int i = 0; i < cellCount; i++) {
            final int i2 = i;
            addAdditionButton(selectionPanel.panels, i2);
            String prefix = cellNumber == i2 ? ">" : " ";
            UIElement button = new UITextButton(FontSizes.rmaCellFontSize, prefix + FormatSyntax.formatExtended(TXDB.get("Cell #A"), new RubyIO().setFX(i)), new Runnable() {
                @Override
                public void run() {
                    cellNumber = i2;
                    frameChanged();
                }
            });
            if (root.canAddRemoveCells()) {
                selectionPanel.panels.add(new UIAppendButton("-", button, new Runnable() {
                    @Override
                    public void run() {
                        if (i2 < root.getCellCount()) {
                            // delete cell
                            root.deleteCell(i2);
                            cellNumber = -1;
                            cellChangeNotificationNumber++;
                        }
                        frameChanged();
                    }
                }, FontSizes.rmaCellFontSize));
            } else {
                selectionPanel.panels.add(button);
            }
        }
        addAdditionButton(selectionPanel.panels, cellCount);
        // cause the (scrollable) selection panel to update stuff
        selectionPanel.setBounds(selectionPanel.getBounds());
    }

    private void addAdditionButton(final LinkedList<UIElement> panels, final int i2) {
        if (!root.canAddRemoveCells())
            return;
        panels.add(new UITextButton(FontSizes.rmaCellFontSize, TXDB.get("<add cell here>"), new Runnable() {
            @Override
            public void run() {
                if (i2 <= root.getCellCount()) {
                    root.addCell(i2);
                    cellNumber = i2;
                    cellChangeNotificationNumber++;
                }
                frameChanged();
            }
        }));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        selectionPanel.setBounds(new Rect(0, 0, r.width, r.height));
    }

    public void frameChanged() {
        if (cellNumber >= root.getCellCount())
            cellNumber = -1;
        if (cellNumber < -1)
            cellNumber = -1;
        // The actual cell changed, even if the number didn't
        cellChangeNotificationNumber++;
        rebuildSelectionPanel();
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;

import java.util.LinkedList;

/**
 * Created on 2/17/17.
 */
public class UICellSelectionPanel extends UIPanel {
    // Instead, using getCell will ensure it gets corrected.
    public int cellNumber = -1;
    public int cellChangeNotificationNumber = 0;

    public IGenposFrame root;

    public UIScrollLayout selectionPanel = new UIScrollLayout(true, FontSizes.cellSelectScrollersize);

    public UICellSelectionPanel(IGenposFrame rmAnimRootPanel) {
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
            UIElement button = new UITextButton(FontSizes.rmaCellTextHeight, prefix + FormatSyntax.formatExtended(TXDB.get("Cell #A"), new RubyIO().setFX(i)), new Runnable() {
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
                }, FontSizes.rmaCellTextHeight));
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
        panels.add(new UITextButton(FontSizes.rmaCellTextHeight, TXDB.get("<add cell here>"), new Runnable() {
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

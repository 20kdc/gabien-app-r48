/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

import java.util.LinkedList;

import gabien.ui.*;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import r48.App;
import r48.ui.UIAppendButton;

/**
 * Created on 2/17/17.
 */
public class UICellSelectionPanel extends App.Prx {
    // Instead, using getCell will ensure it gets corrected.
    public int cellNumber = -1;
    public int cellChangeNotificationNumber = 0;

    public IGenposFrame root;

    public UIScrollLayout selectionPanel = new UIScrollLayout(true, app.f.cellSelectS);

    public UICellSelectionPanel(App app, IGenposFrame rmAnimRootPanel) {
        super(app);
        root = rmAnimRootPanel;
        proxySetElement(selectionPanel, true);
    }

    private void rebuildSelectionPanel() {
        LinkedList<UIElement> elms = new LinkedList<>();
        final int cellCount = root.getCellCount();
        for (int i = 0; i < cellCount; i++) {
            final int i2 = i;
            addAdditionButton(elms, i2);
            String prefix = cellNumber == i2 ? ">" : " ";
            UIElement button = new UITextButton(prefix + T.s.cellTitle.r(i), app.f.rmaCellTH, () -> {
                cellNumber = i2;
                frameChanged();
            });
            if (root.canAddRemoveCells()) {
                elms.add(new UIAppendButton("-", button, () -> {
                    if (i2 < root.getCellCount()) {
                        // delete cell
                        root.deleteCell(i2);
                        cellNumber = -1;
                        cellChangeNotificationNumber++;
                    }
                    frameChanged();
                }, app.f.rmaCellTH));
            } else {
                elms.add(button);
            }
        }
        addAdditionButton(elms, cellCount);
        selectionPanel.panelsSet(elms);
    }

    private void addAdditionButton(LinkedList<UIElement> elms, final int i2) {
        if (!root.canAddRemoveCells())
            return;
        elms.add(new UITextButton(T.s.cellAdd, app.f.rmaCellTH, () -> {
            if (i2 <= root.getCellCount()) {
                root.addCell(i2);
                cellNumber = i2;
                cellChangeNotificationNumber++;
            }
            frameChanged();
        }));
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

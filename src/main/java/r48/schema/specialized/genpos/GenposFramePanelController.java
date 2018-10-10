/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.IImage;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;
import r48.ui.UINSVertLayout;

/**
 * Part of genpos.
 * This takes some of RMAnim's responsibilities.
 * Created on 28/07/17.
 */
public class GenposFramePanelController {
    public UICellSelectionPanel cellSelection;
    public UICellEditingPanel editingPanel;
    public UISingleFrameView editor;
    public UINSVertLayout editingSidebar;

    // For use by the parent.
    public UISplitterLayout rootLayout;
    public IGenposFrame frame;
    public IGenposTweeningManagement tweening;

    // for schema purposes
    public ISchemaHost hostLauncher;

    public UITextButton gridToggleButton;

    public GenposFramePanelController(IGenposFrame rootForNow, IGenposTweeningManagement gtm, ISchemaHost launcher) {
        tweening = gtm;
        hostLauncher = launcher;
        frame = rootForNow;
        editor = new UISingleFrameView(this);
        IImage bkg = rootForNow.getBackground();
        if (bkg != null) {
            editor.camX = bkg.getWidth() / 2;
            editor.camY = bkg.getHeight() / 2;
        }
        cellSelection = new UICellSelectionPanel(rootForNow);

        editingPanel = new UICellEditingPanel(cellSelection, this);
        gridToggleButton = new UITextButton(TXDB.get("8px Grid"), FontSizes.rmaCellTextHeight, new Runnable() {
            @Override
            public void run() {
                // Do nothing.
            }
        }).togglable(false);
        editingSidebar = new UINSVertLayout(gridToggleButton, new UINSVertLayout(editingPanel, cellSelection));
        rootLayout = new UISplitterLayout(editor, editingSidebar, false, 1);
    }

    // Frame changed events <Incoming>. Run before displaying on-screen
    public void frameChanged() {
        // This implicitly changes an incrementing number which causes the cell editor to update, but, that happens at next frame.
        // Instead, for a case like this, call editingPanel directly
        cellSelection.frameChanged();
        editingPanel.somethingChanged();
    }

}

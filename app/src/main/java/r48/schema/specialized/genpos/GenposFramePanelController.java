/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

import org.eclipse.jdt.annotation.NonNull;

import gabien.render.IImage;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.schema.util.ISchemaHost;
import r48.tr.pages.TrRoot;

/**
 * Part of genpos.
 * This takes some of RMAnim's responsibilities.
 * Created on 28/07/17.
 */
public class GenposFramePanelController {
    public UICellSelectionPanel cellSelection;
    public UICellEditingPanel editingPanel;
    public UISingleFrameView editor;
    public UISplitterLayout editingSidebar;

    // For use by the parent.
    public UISplitterLayout rootLayout;
    public IGenposFrame frame;
    public IGenposTweeningManagement tweening;

    // for schema purposes
    public ISchemaHost hostLauncher;

    public UITextButton gridToggleButton;

    public GenposFramePanelController(IGenposFrame rootForNow, IGenposTweeningManagement gtm, @NonNull ISchemaHost launcher) {
        App app = launcher.getApp();
        final TrRoot T = app.t;
        tweening = gtm;
        hostLauncher = launcher;
        frame = rootForNow;
        editor = new UISingleFrameView(app, this);
        IImage bkg = rootForNow.getBackground();
        if (bkg != null) {
            editor.camX = bkg.getWidth() / 2;
            editor.camY = bkg.getHeight() / 2;
        }
        cellSelection = new UICellSelectionPanel(launcher.getApp(), rootForNow);

        editingPanel = new UICellEditingPanel(cellSelection, this);
        gridToggleButton = new UITextButton(T.s.bGrid8px, app.f.rmaCellTH, () -> {
            // Do nothing.
        }).togglable(false);
        editingSidebar = new UISplitterLayout(gridToggleButton, new UISplitterLayout(editingPanel, cellSelection, true, 0), true, 0);
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

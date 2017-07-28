/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UISplitterLayout;
import r48.RubyIO;
import r48.ui.UINSVertLayout;

/**
 * Part of genpos.
 * This takes some of RMAnim's responsibilities.
 * Created on 28/07/17.
 */
public class GenposFramePanelController {
    public UICellSelectionPanel cellSelection;
    public UISingleFrameView editor;
    public UINSVertLayout editingSidebar;

    // For use by the parent.
    public UISplitterLayout rootLayout;
    public IGenposFrame frame;

    public GenposFramePanelController(IGenposFrame rootForNow) {
        frame = rootForNow;
        editor = new UISingleFrameView(this);
        IGrInDriver.IImage bkg = rootForNow.getBackground();
        if (bkg != null) {
            editor.camX = bkg.getWidth() / 2;
            editor.camY = bkg.getHeight() / 2;
        }
        cellSelection = new UICellSelectionPanel(rootForNow);

        // The UICellEditingPanel is informed about frame changes via UICellSelectionPanel
        editingSidebar = new UINSVertLayout(new UICellEditingPanel(cellSelection, rootForNow), cellSelection);
        // Set an absolute width for the editing sidebar
        editingSidebar.setBounds(new Rect(0, 0, 128, 32));
        rootLayout = new UISplitterLayout(editor, editingSidebar, false, 1);
    }

    // Frame changed events <Incoming>. Run before displaying on-screen
    public void frameChanged() {
        // This implicitly changes an incrementing number which causes the cell editor to update.
        cellSelection.frameChanged();
    }


}

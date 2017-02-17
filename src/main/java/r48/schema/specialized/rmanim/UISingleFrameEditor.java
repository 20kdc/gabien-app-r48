/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.RubyTable;

/**
 * Created on 2/17/17.
 */
public class UISingleFrameEditor extends UIPanel {
    public UIPanel editingSidebar = new UIPanel();
    public RMAnimRootPanel basePanelAccess;

    public UISingleFrameEditor(RMAnimRootPanel rmAnimRootPanel) {
        editingSidebar.setBounds(new Rect(0, 0, 32, 32));
        basePanelAccess = rmAnimRootPanel;
        allElements.add(editingSidebar);
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        Rect esb = editingSidebar.getBounds();
        editingSidebar.setBounds(new Rect(r.width - esb.width, 0, esb.width, r.height));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        Rect b = getBounds();
        igd.clearRect(255, 0, 255, ox, oy, b.width, b.height);
        IGrInDriver.IImage sectionA = null;
        String saf1 = basePanelAccess.target.getInstVarBySymbol(basePanelAccess.framesetALoc).decString();
        IGrInDriver.IImage sectionB = null;
        String saf2 = basePanelAccess.target.getInstVarBySymbol(basePanelAccess.framesetBLoc).decString();
        if (saf1.length() != 0)
            sectionA = GaBIEn.getImage(AppMain.rootPath + "Graphics/Animations/" + saf1 + ".png", 0, 0, 0);
        if (saf2.length() != 0)
            sectionB = GaBIEn.getImage(AppMain.rootPath + "Graphics/Animations/" + saf2 + ".png", 0, 0, 0);
        RubyIO f = basePanelAccess.getFrame();
        RubyTable rt = new RubyTable(f.getInstVarBySymbol("@cell_data").userVal);

        int opx = ox + (b.width / 2);
        int opy = oy + (b.height / 2);

        for (int i = 0; i < rt.width; i++) {
            // VERY UNFINISHED.
            // Also critical to the whole point of this.
            // Hm.
            int cell = rt.getTiletype(i, 0, 0);
            int ofx = rt.getTiletype(i, 1, 0);
            int ofy = rt.getTiletype(i, 2, 0);
            int cellX = (cell % 5) * 192;
            int cellY = (cell / 5) * 192;
            if (sectionA != null)
                igd.blitImage(cellX, cellY, 192, 192, (opx - 96) + ofx, (opy - 96) + ofy, sectionA);
        }
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }
}

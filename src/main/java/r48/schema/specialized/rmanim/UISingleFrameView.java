/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ScissorGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.RubyTable;
import r48.ui.UINSVertLayout;

/**
 * Handles drawing for a single-frame editor.
 * Created on 2/17/17.
 */
public class UISingleFrameView extends UIElement {
    public RMAnimRootPanel basePanelAccess;

    public UISingleFrameView(RMAnimRootPanel rmAnimRootPanel) {
        basePanelAccess = rmAnimRootPanel;
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igdo) {
        ScissorGrInDriver igd = new ScissorGrInDriver();

        Rect b = getBounds();

        igd.inner = igdo;
        igd.workLeft = ox;
        igd.workTop = oy;
        igd.workRight = ox + b.width;
        igd.workBottom = oy + b.height;

        igd.clearAll(255, 0, 255);
        RubyIO f = basePanelAccess.getFrame();
        RubyTable rt = new RubyTable(f.getInstVarBySymbol("@cell_data").userVal);

        int opx = ox + (b.width / 2);
        int opy = oy + (b.height / 2);

        basePanelAccess.prepareFramesetCache();

        igd.clearRect(192, 0, 192, opx - 8, opy - 1, 16, 2);
        igd.clearRect(192, 0, 192, opx - 1, opy - 8, 2, 16);

        for (int i = 0; i < rt.width; i++) {
            // VERY UNFINISHED.
            // Also critical to the whole point of this.
            // Hm.

            // In this version, 7 is blend_type, 6 is opacity (0-255), 5 is mirror (int_boolean),
            // 4 is angle, 3 is scale (0-100), x is modified by 1, y is modified by 2.
            // 0 is presumably cell-id.

            int cell = rt.getTiletype(i, 0, 0);
            int scale = rt.getTiletype(i, 3, 0);
            int ts = basePanelAccess.getScaledImageIconSize(scale);
            int ofx = rt.getTiletype(i, 1, 0) - (ts / 2);
            int ofy = rt.getTiletype(i, 2, 0) - (ts / 2);
            int cellX = (cell % 5) * 192;
            int cellY = (cell / 5) * 192;
            // this is a guess!
            IGrInDriver.IImage scaleImage = basePanelAccess.framesetCacheA;
            if (cellY >= (6 * 192)) {
                cellY -= (6 * 192);
                scaleImage = basePanelAccess.framesetCacheB;
            }
            if (scaleImage != null)
                igd.blitScaledImage(cellX, cellY, 192, 192, opx + ofx, opy + ofy, ts, ts, scaleImage);
        }
    }

    @Override
    public void handleClick(int x, int y, int button) {

    }
}

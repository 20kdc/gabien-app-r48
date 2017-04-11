/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

import gabien.IGrInDriver;
import gabien.ScissorGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.RubyIO;
import r48.RubyTable;

/**
 * Handles drawing for a single-frame editor.
 * Created on 2/17/17.
 */
public class UISingleFrameView extends UIElement {
    public RMAnimRootPanel basePanelAccess;

    private int lastMX, lastMY, camX, camY;
    private int dragging;

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

        int opx = ox + (b.width / 2) - camX;
        int opy = oy + (b.height / 2) - camY;

        igd.clearRect(192, 0, 192, opx - 8, opy - 1, 16, 2);
        igd.clearRect(192, 0, 192, opx - 1, opy - 8, 2, 16);

        for (int i = 0; i < rt.width; i++) {
            // Slightly less unfinished.

            // In the target versions, 7 is blend_type, 6 is opacity (0-255), 5 is mirror (int_boolean),
            // 4 is angle, 3 is scale (0-100), x is modified by 1, y is modified by 2.
            // 0 is presumably cell-id.

            int cell = rt.getTiletype(i, 0, 0);
            boolean mirror = rt.getTiletype(i, 5, 0) != 0;
            int opacity = Math.min(Math.max(rt.getTiletype(i, 6, 0), 0), 255);
            if (opacity == 0)
                continue;
            IGrInDriver.IImage scaleImage = null;
            if (cell >= 100) {
                cell -= 100;
                scaleImage = basePanelAccess.getFramesetCache(true, mirror, opacity);
            } else {
                scaleImage = basePanelAccess.getFramesetCache(false, mirror, opacity);
            }
            int angle = rt.getTiletype(i, 4, 0);
            int scale = rt.getTiletype(i, 3, 0);
            int ts = basePanelAccess.getScaledImageIconSize(scale);
            int ofx = rt.getTiletype(i, 1, 0) - (ts / 2);
            int ofy = rt.getTiletype(i, 2, 0) - (ts / 2);
            int cellX = (cell % 5) * 192;
            int cellY = (cell / 5) * 192;
            // try to avoid using rotated images
            if (scaleImage != null) {
                if ((angle % 360) == 0) {
                    igd.blitScaledImage(cellX, cellY, 192, 192, opx + ofx, opy + ofy, ts, ts, scaleImage);
                } else {
                    igd.blitRotatedScaledImage(cellX, cellY, 192, 192, opx + ofx, opy + ofy, ts, ts, angle, scaleImage);
                }
            }
        }
    }

    @Override
    public void handleClick(int x, int y, int button) {
        dragging = button;
        lastMX = x;
        lastMY = y;
    }

    @Override
    public void handleDrag(int x, int y) {
        if (dragging == 1) {
            if (basePanelAccess.cellSelection.cellNumber != -1) {
                RubyIO target = basePanelAccess.getFrame();
                RubyTable rt = new RubyTable(target.getInstVarBySymbol("@cell_data").userVal);
                int ofsX = x - lastMX;
                int ofsY = y - lastMY;
                rt.setTiletype(basePanelAccess.cellSelection.cellNumber, 1, 0, (short) (rt.getTiletype(basePanelAccess.cellSelection.cellNumber, 1, 0) + ofsX));
                rt.setTiletype(basePanelAccess.cellSelection.cellNumber, 2, 0, (short) (rt.getTiletype(basePanelAccess.cellSelection.cellNumber, 2, 0) + ofsY));
                // Need to whack the other components into accepting this
                basePanelAccess.updateNotify.run();
                basePanelAccess.frameChanged();
            }
        } else if (dragging == 3) {
            camX -= x - lastMX;
            camY -= y - lastMY;
        }
        lastMX = x;
        lastMY = y;
    }
}

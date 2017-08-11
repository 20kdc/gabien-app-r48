/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ScissorGrInDriver;
import gabien.ui.IFunction;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.ui.UIGrid;

/**
 * Handles drawing for a single-frame editor.
 * Created on 2/17/17.
 */
public class UISingleFrameView extends UIElement {
    public GenposFramePanelController basePanelAccess;

    private int lastMX, lastMY, lossX, lossY;
    public int camX, camY;
    private int dragging;

    public UISingleFrameView(GenposFramePanelController rmAnimRootPanel) {
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

        int opx = ox + (b.width / 2) - camX;
        int opy = oy + (b.height / 2) - camY;

        IImage bkg = basePanelAccess.frame.getBackground();
        if (bkg != null)
            igd.blitImage(0, 0, bkg.getWidth(), bkg.getHeight(), opx, opy, bkg);
        int[] d = basePanelAccess.frame.getIndicators();
        for (int i = 0; i < d.length; i += 2) {
            int x = d[i];
            int y = d[i + 1];
            igd.clearRect(192, 0, 192, (opx + x) - 8, (opy + y) - 1, 16, 2);
            igd.clearRect(192, 0, 192, (opx + x) - 1, (opy + y) - 8, 2, 16);
        }
        if (basePanelAccess.cellSelection.cellNumber != -1)
            if (basePanelAccess.frame.getCellCount() > basePanelAccess.cellSelection.cellNumber) {
                Rect r = basePanelAccess.frame.getCellSelectionIndicator(basePanelAccess.cellSelection.cellNumber);
                UIGrid.drawSelectionBox(opx + (r.x - 1), opy + (r.y - 1), r.width + 2, r.height + 2, igd);
            }
        int cellCount = basePanelAccess.frame.getCellCount();
        for (int i = 0; i < cellCount; i++)
            basePanelAccess.frame.drawCell(i, opx, opy, igd);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        dragging = button;
        lastMX = x;
        lastMY = y;
        lossX = 0;
        lossY = 0;
    }

    @Override
    public void handleDrag(int x, int y) {
        if (dragging == 1) {
            if (basePanelAccess.cellSelection.cellNumber != -1) {
                if (basePanelAccess.frame.getCellCount() > basePanelAccess.cellSelection.cellNumber) {
                    // RubyIO target = basePanelAccess.frame.getFrame();
                    // RubyTable rt = new RubyTable(target.getInstVarBySymbol("@cell_data").userVal);
                    final int ofsX = (x - lastMX) + lossX;
                    final int ofsY = (y - lastMY) + lossY;
                    lastMX = x;
                    lastMY = y;
                    basePanelAccess.frame.moveCell(basePanelAccess.cellSelection.cellNumber, new IFunction<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer integer) {
                            int r = offset(integer, ofsX);
                            int ao = r - integer;
                            lossX = ofsX - ao;
                            return r;
                        }
                    }, new IFunction<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer integer) {
                            int r = offset(integer, ofsY);
                            int ao = r - integer;
                            lossY = ofsY - ao;
                            return r;
                        }
                    });
                    return;
                }
            }
        } else if (dragging == 3) {
            camX -= x - lastMX;
            camY -= y - lastMY;
        }
        lastMX = x;
        lastMY = y;
    }

    private int offset(int integer, int ofs) {
        integer += ofs;
        if (basePanelAccess.gridToggleButton.state)
            integer &= ~7;
        return integer;
    }
}

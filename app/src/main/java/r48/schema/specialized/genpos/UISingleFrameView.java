/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos;

import java.util.function.Function;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.*;
import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;
import gabien.wsi.IPointer;
import r48.App;

/**
 * Handles drawing for a single-frame editor.
 * Created on 2/17/17.
 */
public class UISingleFrameView extends App.Elm implements OldMouseEmulator.IOldMouseReceiver {
    public GenposFramePanelController basePanelAccess;

    private int lastMX, lastMY, lossX, lossY;
    public int camX, camY;
    private int dragging;

    public OldMouseEmulator mouseEmulator = new OldMouseEmulator(this);

    public UISingleFrameView(App app, GenposFramePanelController rmAnimRootPanel) {
        super(app);
        basePanelAccess = rmAnimRootPanel;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void renderLayer(IGrDriver igd, UILayer layer) {
        if (layer != UILayer.Content)
            return;
        Size b = getSize();

        igd.clearAll(255, 0, 255);

        int opx = (b.width / 2) - camX;
        int opy = (b.height / 2) - camY;

        IImage bkg = basePanelAccess.frame.getBackground();
        if (bkg != null)
            igd.blitImage(opx, opy, bkg);
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
                app.a.drawSelectionBox(opx + (r.x - 1), opy + (r.y - 1), r.width + 2, r.height + 2, 1, igd);
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
                    final int ofsX = (x - lastMX) + lossX;
                    final int ofsY = (y - lastMY) + lossY;
                    lastMX = x;
                    lastMY = y;
                    basePanelAccess.frame.moveCell(basePanelAccess.cellSelection.cellNumber, new Function<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer integer) {
                            int r = offset(integer, ofsX);
                            int ao = r - integer;
                            lossX = ofsX - ao;
                            return r;
                        }
                    }, new Function<Integer, Integer>() {
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

    @Override
    public void handleRelease(int x, int y) {

    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        return mouseEmulator;
    }

    private int offset(int integer, int ofs) {
        integer += ofs;
        if (basePanelAccess.gridToggleButton.state)
            integer &= ~7;
        return integer;
    }
}

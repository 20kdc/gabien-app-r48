/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;

/**
 * Created on 12/28/16.
 */
public class UIVScrollbar extends UIElement {
    public double scrollPoint = 0.0;
    public int lMX, lMY;

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        IGrInDriver.IImage lt = GaBIEn.getImage("layertab.png", 0, 0, 0);
        Rect bounds = getBounds();
        for (int i = 0; i < bounds.height; i += 4) {
            int seg = 2;
            if (i == 0)
                seg = 0;
            if (i == bounds.height - 4)
                seg = 3;
            drawSegment(ox, oy + i, seg, igd, lt);
        }
        drawSegment(ox, oy + 2 + ((int) ((bounds.height - 8) * scrollPoint)), 1, igd, lt);
    }

    public void drawSegment(int ox, int opy, int sid, IGrInDriver igd, IGrInDriver.IImage div) {
        igd.blitImage(36, 32 + (sid * 4), 32, 4, ox, opy, div);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        lMX = x;
        lMY = y;
    }

    @Override
    public void handleDrag(int x, int y) {
        double scalingFactor = 1.0 / (getBounds().height - 8);
        scrollPoint += (y - lMY) * scalingFactor;
        if (scrollPoint < 0)
            scrollPoint = 0;
        if (scrollPoint > 1)
            scrollPoint = 1;
        lMX = x;
        lMY = y;
    }
}

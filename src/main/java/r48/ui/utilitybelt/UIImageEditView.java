/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.ui.Art;
import r48.ui.UIGrid;

/**
 * Thanks to Tomeno for the general design of this UI in a tablet-friendly pixel-arty way.
 * Though I kind of modified those plans a bit.
 */
public class UIImageEditView extends UIElement {
    public int[] image = new int[1024];
    public int imageW = 32, imageH = 32, cursorX = 16, cursorY = 16, zoom = FontSizes.getSpriteScale() * 16;
    public boolean camMode = true, dragging;
    public int dragLastX, dragLastY;
    public double camX, camY;
    public int gridW = 16, gridH = 16, gridOX = 0, gridOY = 0;
    public Runnable colour;

    public boolean showTarget;
    public int targetX, targetY;
    public int gridColour = 0x200020;

    public UIImageEditView(Runnable c) {
        colour = c;
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        Rect bounds = getBounds();
        Rect viewRct = getViewRect();
        // Maybe cache this for perf. Acts like a more precise scissor for now.
        IGrDriver osb = GaBIEn.makeOffscreenBuffer(bounds.width, bounds.height, false);
        osb.clearRect(32, 32, 32, 0, 0, bounds.width, bounds.height);
        int gcR = (gridColour >> 16) & 0xFF;
        int gcG = (gridColour >> 8) & 0xFF;
        int gcB = gridColour & 0xFF;
        osb.clearRect(gcR / 3, gcG / 3, gcB / 3, viewRct.x, viewRct.y, viewRct.width, viewRct.height);
        // viewRct is in zoomed osb-local coordinates.
        // It's the rectangle of the image.
        // localGrid is in zoomed osb-local coordinates.
        // It's X/Y is the viewrect x/y + the grid offset - 1 grid cell (so the offset stuff is correct) scaled,
        //  and it's W/H is the grid size scaled.
        Rect localGrid = getLocalGridRect(viewRct);
        boolean outerFlip = false;
        for (int ofx = (localGrid.x - (gridW * zoom)); ofx < (viewRct.x + viewRct.width); ofx += localGrid.width) {
            Rect testGrid = viewRct.getIntersection(new Rect(ofx, viewRct.y, localGrid.width, viewRct.height));
            if (testGrid == null)
                continue;
            int i = outerFlip ? 1 : 0;
            outerFlip = !outerFlip;
            for (int ofy = (localGrid.y - (gridH * zoom)); ofy < (viewRct.y + viewRct.height); ofy += localGrid.height) {
                int o = 0x80;
                boolean light = ((i & 1) != 0);
                if (light)
                    o = 0xC0;
                Rect subLocalGrid = viewRct.getIntersection(new Rect(ofx, ofy, localGrid.width, localGrid.height));
                if (subLocalGrid != null)
                    osb.clearRect((gcR * o) / 255, (gcG * o) / 255, (gcB * o) / 255, subLocalGrid.x, subLocalGrid.y, subLocalGrid.width, subLocalGrid.height);
                i++;
            }
        }
        IImage tempImg = createImg();
        osb.blitScaledImage(0, 0, imageW, imageH, viewRct.x, viewRct.y, viewRct.width, viewRct.height, tempImg);
        Art.drawSelectionBox(viewRct.x + (cursorX * zoom), viewRct.y + (cursorY * zoom), zoom, zoom, FontSizes.getSpriteScale(), osb);
        if (showTarget)
            Art.drawTarget(viewRct.x + (targetX * zoom), viewRct.y + (targetY * zoom), zoom, osb);
        igd.blitImage(0, 0, bounds.width, bounds.height, ox, oy, osb);
        osb.shutdown();

        Rect zPlus = Art.getZIconRect(false, 0);
        Rect zPlusFull = Art.getZIconRect(true, 0);
        Rect zMinus = Art.getZIconRect(false, 1);
        Rect zDrag = Art.getZIconRect(false, 2);
        int textX = zPlusFull.x + zPlusFull.width;
        String text = cursorX + ", " + cursorY;
        UILabel.drawLabel(igd, bounds.width - (textX + zPlus.x), ox + textX, oy + zPlus.y, text, 0, FontSizes.mapPositionTextHeight);
        Art.drawZoom(igd, true, zPlus.x + ox, zPlus.y + oy, zPlus.height);
        Art.drawZoom(igd, false, zMinus.x + ox, zMinus.y + oy, zMinus.height);
        Art.drawDragControl(igd, camMode, zDrag.x + ox, zDrag.y + oy, zDrag.height);
    }

    private Rect getLocalGridRect(Rect viewRct) {
        int localAnchorX = gridOX % gridW;
        if (localAnchorX != 0)
            localAnchorX -= gridW * zoom;
        int localAnchorY = gridOY % gridH;
        if (localAnchorY != 0)
            localAnchorY -= gridH * zoom;
        return new Rect(viewRct.x + localAnchorX, viewRct.y + localAnchorY, gridW * zoom, gridH * zoom);
    }

    private Rect getViewRect() {
        Rect bounds = getBounds();
        return new Rect((int) (camX * zoom) + (bounds.width / 2), (int) (camY * zoom) + (bounds.height / 2), imageW * zoom, imageH * zoom);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        dragging = false;
        if (button != 1)
            return;
        if (Art.getZIconRect(true, 0).contains(x, y)) {
            handleMousewheel(x, y, true);
            return;
        }
        if (Art.getZIconRect(true, 1).contains(x, y)) {
            handleMousewheel(x, y, false);
            return;
        }
        if (Art.getZIconRect(true, 2).contains(x, y)) {
            camMode = !camMode;
            return;
        }
        dragging = true;
        dragLastX = x;
        dragLastY = y;
        handleAct(x, y, true);
    }

    @Override
    public void handleDrag(int x, int y) {
        handleAct(x, y, false);
        dragLastX = x;
        dragLastY = y;
    }

    public void handleAct(int x, int y, boolean first) {
        if (!dragging)
            return;
        if (camMode) {
            camX += (x - dragLastX) / (double) zoom;
            camY += (y - dragLastY) / (double) zoom;
        } else {
            Rect bounds = getViewRect();
            int nx = UIGrid.sensibleCellDiv(x - bounds.x, zoom);
            int ny = UIGrid.sensibleCellDiv(y - bounds.y, zoom);
            nx -= UIGrid.sensibleCellDiv(nx, imageW) * imageW;
            ny -= UIGrid.sensibleCellDiv(ny, imageH) * imageH;
            boolean perform = (first || (nx != cursorX) || (ny != cursorY)) && !camMode;
            cursorX = nx;
            cursorY = ny;
            if (perform)
                colour.run();
        }
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        if (north) {
            zoom *= 2;
        } else {
            zoom /= 2;
            if (zoom < 1)
                zoom = 1;
        }
    }

    public IImage createImg() {
        return GaBIEn.createImage(image, imageW, imageH);
    }
}

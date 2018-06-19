/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.ui.Art;
import r48.ui.UIGrid;

/**
 * Thanks to Tomeno for the general design of this UI in a tablet-friendly pixel-arty way.
 * Though I kind of modified those plans a bit.
 */
public class UIImageEditView extends UIElement implements OldMouseEmulator.IOldMouseReceiver {
    // Do not set outside of setImage
    public ImageEditorImage image = new ImageEditorImage(32, 32);
    public int cursorX = 16, cursorY = 16, zoom = FontSizes.getSpriteScale() * 16;
    public boolean camMode = true, tempCamMode = false, dragging;
    public int dragLastX, dragLastY;
    public double camX, camY;
    public int gridW = 16, gridH = 16, gridOX = 0, gridOY = 0;
    public Runnable colour;

    public boolean showTarget;
    public Rect tiling;
    public int targetX, targetY;
    public int gridColour = 0x200020;
    public boolean gridST = false;

    public OldMouseEmulator mouseEmulator = new OldMouseEmulator(this);
    public UILabel.StatusLine statusLine = new UILabel.StatusLine();

    public UIImageEditView(Runnable c) {
        colour = c;
    }

    // Only write to image from here!
    public void setImage(ImageEditorImage n) {
        image = n;
        cursorX = n.width / 2;
        cursorY = n.height / 2;
        camX = 0;
        camY = 0;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
    }

    @Override
    public void render(IGrDriver igd) {
        Size bounds = getSize();
        Rect viewRct = getViewRect();

        igd.clearAll(32, 32, 32);

        drawGrid(igd, viewRct, false);

        IImage tempImg = createImg();
        int min = 0;
        int max = 0;
        if (tiling != null) {
            min = -1;
            max = 1;
        }
        int ofsX = 0;
        int ofsY = 0;
        int ofsW = image.width;
        int ofsH = image.height;
        if (tiling != null) {
            ofsX = tiling.x;
            ofsY = tiling.y;
            ofsW = tiling.width;
            ofsH = tiling.height;
        }
        for (int i = min; i <= max; i++)
            for (int j = min; j <= max; j++)
                igd.blitScaledImage(ofsX, ofsY, ofsW, ofsH, viewRct.x + (ofsW * zoom * i) + (ofsX * zoom), viewRct.y + (ofsH * zoom * j) + (ofsY * zoom), ofsW * zoom, ofsH * zoom, tempImg);

        if (gridST)
            drawGrid(igd, viewRct, true);

        if (tiling != null)
            Art.drawSelectionBox(viewRct.x + (ofsX * zoom), viewRct.y + (ofsY * zoom), ofsW * zoom, ofsH * zoom, FontSizes.getSpriteScale(), igd);

        Art.drawSelectionBox(viewRct.x + (cursorX * zoom), viewRct.y + (cursorY * zoom), zoom, zoom, FontSizes.getSpriteScale(), igd);

        if (showTarget)
            Art.drawTarget(viewRct.x + (targetX * zoom), viewRct.y + (targetY * zoom), zoom, igd);

        Rect zPlus = Art.getZIconRect(false, 0);
        Rect zPlusFull = Art.getZIconRect(true, 0);
        Rect zMinus = Art.getZIconRect(false, 1);
        Rect zDrag = Art.getZIconRect(false, 2);
        String info = TXDB.get("LMB: Draw/place, others: scroll, camera button: scroll mode");
        if (GaBIEn.singleWindowApp())
            info = TXDB.get("Tap/Drag: Draw, camera button: Switch to scrolling");
        if (camMode) {
            info = TXDB.get("All mouse buttons position cursor & scroll, camera button goes back to drawing");
            if (GaBIEn.singleWindowApp())
                info = TXDB.get("Tap: Position cursor, Drag: Scroll, camera button : go back to drawing");
        }
        String text = cursorX + ", " + cursorY + " " + info;

        int textX = zPlusFull.x + zPlusFull.width;
        int textW = bounds.width - (textX + ((zPlusFull.width - zPlus.width) / 2));
        statusLine.draw(text, FontSizes.mapPositionTextHeight, igd, textX, zPlus.y, textW);

        Art.drawZoom(igd, true, zPlus.x, zPlus.y, zPlus.height);
        Art.drawZoom(igd, false, zMinus.x, zMinus.y, zMinus.height);
        Art.drawDragControl(igd, camMode, zDrag.x, zDrag.y, zDrag.height);
    }

    private void drawGrid(IGrDriver osb, Rect viewRct, boolean cut) {
        int gcR = (gridColour >> 16) & 0xFF;
        int gcG = (gridColour >> 8) & 0xFF;
        int gcB = gridColour & 0xFF;
        if (!cut)
            osb.clearRect(gcR / 3, gcG / 3, gcB / 3, viewRct.x, viewRct.y, viewRct.width, viewRct.height);
        int lineThickness = FontSizes.getSpriteScale() * 2;
        // viewRct is in zoomed osb-local coordinates.
        // It's the rectangle of the image.
        // localGrid is in zoomed osb-local coordinates.
        // It's X/Y is the viewrect x/y + the grid offset - 1 grid cell (so the offset stuff is correct) scaled,
        //  and it's W/H is the grid size scaled.
        Rect localGrid = getLocalGridRect(viewRct);
        boolean outerFlip = false;
        Intersector intersect = MTIntersector.singleton.get();
        for (int ofx = (localGrid.x - (gridW * zoom)); ofx < (viewRct.x + viewRct.width); ofx += localGrid.width) {
            if (!viewRct.intersects(new Rect(ofx, viewRct.y, localGrid.width, viewRct.height)))
                continue;
            int i = outerFlip ? 1 : 0;
            outerFlip = !outerFlip;
            for (int ofy = (localGrid.y - (gridH * zoom)); ofy < (viewRct.y + viewRct.height); ofy += localGrid.height) {
                int o = 0xA0;
                boolean light = ((i & 1) != 0);
                if (light)
                    o = 0xFF;
                // The osb.clearRect call alters the Intersect, but that's fine since it gets reset.
                intersect.set(viewRct);
                if (intersect.intersect(ofx, ofy, localGrid.width, localGrid.height)) {
                    int rR = (gcR * o) / 255;
                    int rG = (gcG * o) / 255;
                    int rB = (gcB * o) / 255;
                    if (cut) {
                        osb.clearRect(rR, rG, rB, intersect.x, intersect.y, lineThickness, intersect.height);
                        osb.clearRect(rR, rG, rB, intersect.x + intersect.width - lineThickness, intersect.y, lineThickness, intersect.height);
                        osb.clearRect(rR, rG, rB, intersect.x + lineThickness, intersect.y, intersect.width - (lineThickness * 2), lineThickness);
                        osb.clearRect(rR, rG, rB, intersect.x + lineThickness, intersect.y + intersect.height - lineThickness, intersect.width - (lineThickness * 2), lineThickness);
                    } else {
                        osb.clearRect(rR, rG, rB, intersect.x, intersect.y, intersect.width, intersect.height);
                    }
                }
                i++;
            }
        }
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
        Size bounds = getSize();
        return new Rect((int) (camX * zoom) + (bounds.width / 2), (int) (camY * zoom) + (bounds.height / 2), image.width * zoom, image.height * zoom);
    }

    @Override
    public void handlePointerBegin(IPointer state) {
        mouseEmulator.handlePointerBegin(state);
    }

    @Override
    public void handlePointerUpdate(IPointer state) {
        mouseEmulator.handlePointerUpdate(state);
    }

    @Override
    public void handlePointerEnd(IPointer state) {
        mouseEmulator.handlePointerEnd(state);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        dragging = false;
        if (button == 1) {
            tempCamMode = false;
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
        } else {
            tempCamMode = true;
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

    @Override
    public void handleRelease(int x, int y) {

    }

    public void handleAct(int x, int y, boolean first) {
        if (!dragging)
            return;

        int ofsX = 0;
        int ofsY = 0;
        int ofsW = image.width;
        int ofsH = image.height;
        if (tiling != null) {
            ofsX = tiling.x;
            ofsY = tiling.y;
            ofsW = tiling.width;
            ofsH = tiling.height;
        }

        Rect bounds = getViewRect();
        int nx = UIGrid.sensibleCellDiv(x - bounds.x, zoom);
        int ny = UIGrid.sensibleCellDiv(y - bounds.y, zoom);
        nx -= ofsX;
        ny -= ofsY;
        nx -= UIGrid.sensibleCellDiv(nx, ofsW) * ofsW;
        ny -= UIGrid.sensibleCellDiv(ny, ofsH) * ofsH;
        nx += ofsX;
        ny += ofsY;

        if (camMode || tempCamMode) {
            if (first) {
                cursorX = nx;
                cursorY = ny;
            }
            camX += (x - dragLastX) / (double) zoom;
            camY += (y - dragLastY) / (double) zoom;
        } else {
            boolean perform = first || (nx != cursorX) || (ny != cursorY);
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
        return image.rasterize();
    }
}

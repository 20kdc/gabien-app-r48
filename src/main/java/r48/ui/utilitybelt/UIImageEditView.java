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
    public int targetX, targetY;
    public int gridColour = 0x200020;

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
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
    }

    @Override
    public void render(IGrDriver igd) {
        Size bounds = getSize();
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
                if (intersect.intersect(ofx, ofy, localGrid.width, localGrid.height))
                    osb.clearRect((gcR * o) / 255, (gcG * o) / 255, (gcB * o) / 255, intersect.x, intersect.y, intersect.width, intersect.height);
                i++;
            }
        }
        IImage tempImg = createImg();
        osb.blitScaledImage(0, 0, image.width, image.height, viewRct.x, viewRct.y, viewRct.width, viewRct.height, tempImg);
        Art.drawSelectionBox(viewRct.x + (cursorX * zoom), viewRct.y + (cursorY * zoom), zoom, zoom, FontSizes.getSpriteScale(), osb);
        if (showTarget)
            Art.drawTarget(viewRct.x + (targetX * zoom), viewRct.y + (targetY * zoom), zoom, osb);
        igd.blitImage(0, 0, bounds.width, bounds.height, 0, 0, osb);
        osb.shutdown();

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

        Rect bounds = getViewRect();
        int nx = UIGrid.sensibleCellDiv(x - bounds.x, zoom);
        int ny = UIGrid.sensibleCellDiv(y - bounds.y, zoom);
        nx -= UIGrid.sensibleCellDiv(nx, image.width) * image.width;
        ny -= UIGrid.sensibleCellDiv(ny, image.height) * image.height;

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

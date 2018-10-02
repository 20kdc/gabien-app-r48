/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.*;
import gabien.ui.*;
import r48.FontSizes;
import r48.map.UIMapView;
import r48.ui.Art;
import r48.ui.UIGrid;

/**
 * Thanks to Tomeno for the general design of this UI in a tablet-friendly pixel-arty way.
 * Though I kind of modified those plans a bit.
 */
public class UIImageEditView extends UIElement implements OldMouseEmulator.IOldMouseReceiver {
    // Do NOT set outside of setImage
    public ImageEditorImage image = new ImageEditorImage(32, 32);
    public ImageEditorEDS eds = new ImageEditorEDS();
    public int zoom = FontSizes.getSpriteScale() * 16;
    private boolean tempCamMode = false, dragging;
    private boolean shift;
    private int dragLastX, dragLastY;
    public double camX, camY;
    public Rect grid = new Rect(0, 0, 16, 16);

    public IImageEditorTool currentTool;
    public int selPaletteIndex;

    public Rect tiling;
    public int gridColour = 0x800080;
    public boolean gridST = false;

    private OldMouseEmulator mouseEmulator = new OldMouseEmulator(this);
    private UILabel.StatusLine statusLine = new UILabel.StatusLine();

    public Runnable newToolCallback;

    public UIImageEditView(IImageEditorTool rootTool, Runnable updatePal) {
        eds.currentImage = image;
        eds.newFile();
        newToolCallback = updatePal;
        currentTool = rootTool;
        if (UIMapView.useDragControl(false))
            currentTool = new CamImageEditorTool(currentTool);
    }

    // Only write to image from here!
    public void setImage(ImageEditorImage n) {
        selPaletteIndex = 0;
        if (n.paletteSize() > 1)
            selPaletteIndex = 1;
        image = n;
        eds.currentImage = n;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        shift = false;
        if (peripherals instanceof IDesktopPeripherals)
            shift = ((IDesktopPeripherals) peripherals).isKeyDown(IGrInDriver.VK_SHIFT);
    }

    @Override
    public void render(IGrDriver igd) {
        Size bounds = getSize();
        Rect viewRct = getViewRect();

        igd.clearAll(32, 32, 32);

        drawGrid(igd, viewRct, false);

        // This allows for tiling
        IImage tempImg = image.rasterizeDouble();
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
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        int soX = viewRct.x + (ofsX * zoom);
        int soY = viewRct.y + (ofsY * zoom);
        int soIX = bounds.width - soX;
        int soIY = bounds.height - soY;
        int soW = ofsW * zoom;
        int soH = ofsH * zoom;
        if (tiling != null) {
            minX = -((soX + (soW - 1)) / soW);
            maxX = ((soIX + (soW - 1)) / soW);
            minY = -((soY + (soH - 1)) / soH);
            maxY = ((soIY + (soW - 1)) / soH);
        }
        for (int i = minX; i <= maxX; i++)
            for (int j = minY; j <= maxY; j++)
                igd.blitScaledImage(ofsX, ofsY, ofsW, ofsH, soX + (soW * i), soY + (soH * j), soW, soH, tempImg);

        if (gridST)
            drawGrid(igd, viewRct, true);

        if (tiling != null)
            Art.drawSelectionBox(viewRct.x + (ofsX * zoom), viewRct.y + (ofsY * zoom), ofsW * zoom, ofsH * zoom, FontSizes.getSpriteScale(), igd);


        Rect theSelection = currentTool.getSelection();
        if (theSelection != null) {
            if (theSelection.width == 0 && theSelection.height == 0) {
                Art.drawTarget(viewRct.x + (theSelection.x * zoom), viewRct.y + (theSelection.y * zoom), zoom, igd);
            } else {
                Art.drawSelectionBox(viewRct.x + (theSelection.x * zoom), viewRct.y + (theSelection.y * zoom), theSelection.width * zoom, theSelection.height * zoom, FontSizes.getSpriteScale(), igd);
            }
        }

        boolean dedicatedDragControl = UIMapView.useDragControl(currentTool.getCamModeLT() != null);

        String status = currentTool.getLocalizedText(dedicatedDragControl);

        // shared with UIMapView

        Rect plusRect = Art.getZIconRect(false, 0);
        Rect plusRectFull = Art.getZIconRect(true, 0); // used for X calc on the label
        Rect minusRect = Art.getZIconRect(false, 1);
        Rect dragRect = Art.getZIconRect(false, 2);

        int textX = plusRectFull.x + plusRectFull.width;
        int textW = getSize().width - (textX + ((plusRectFull.width - plusRect.width) / 2));
        statusLine.draw(status, FontSizes.mapPositionTextHeight, igd, textX, plusRect.y, textW);

        Art.drawZoom(igd, true, plusRect.x, plusRect.y, plusRect.height);
        Art.drawZoom(igd, false, minusRect.x, minusRect.y, minusRect.height);
        if (dedicatedDragControl)
            Art.drawDragControl(igd, currentTool.getCamModeLT() != null, dragRect.x, dragRect.y, minusRect.height);
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
        for (int ofx = (localGrid.x - (grid.width * zoom)); ofx < (viewRct.x + viewRct.width); ofx += localGrid.width) {
            if (!viewRct.intersects(new Rect(ofx, viewRct.y, localGrid.width, viewRct.height)))
                continue;
            int i = outerFlip ? 1 : 0;
            outerFlip = !outerFlip;
            for (int ofy = (localGrid.y - (grid.height * zoom)); ofy < (viewRct.y + viewRct.height); ofy += localGrid.height) {
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
        int localAnchorX = grid.x % grid.width;
        if (localAnchorX != 0)
            localAnchorX -= grid.width;
        int localAnchorY = grid.y % grid.height;
        if (localAnchorY != 0)
            localAnchorY -= grid.height;
        localAnchorX *= zoom;
        localAnchorY *= zoom;
        return new Rect(viewRct.x + localAnchorX, viewRct.y + localAnchorY, grid.width * zoom, grid.height * zoom);
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
        handleRelease(x, y);
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
            boolean dragControl = UIMapView.useDragControl(currentTool.getCamModeLT() != null);
            if (dragControl) {
                if (Art.getZIconRect(true, 2).contains(x, y)) {
                    IImageEditorTool lt = currentTool.getCamModeLT();
                    if (lt != null) {
                        currentTool = lt;
                    } else {
                        currentTool = new CamImageEditorTool(currentTool);
                    }
                    newToolCallback.run();
                    return;
                }
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
        if (dragging) {
            currentTool.endApply(this);
            dragging = false;
        }
    }

    public void handleAct(int x, int y, boolean first) {
        if (!dragging)
            return;

        Rect bounds = getViewRect();
        int ax = UIGrid.sensibleCellDiv(dragLastX - bounds.x, zoom);
        int ay = UIGrid.sensibleCellDiv(dragLastY - bounds.y, zoom);
        int nx = UIGrid.sensibleCellDiv(x - bounds.x, zoom);
        int ny = UIGrid.sensibleCellDiv(y - bounds.y, zoom);

        if ((currentTool.getCamModeLT() != null) || tempCamMode) {
            camX += (x - dragLastX) / (double) zoom;
            camY += (y - dragLastY) / (double) zoom;
        } else {
            IImageEditorTool oldTool = currentTool;
            ImPoint imp = new ImPoint(nx, ny);
            if (first) {
                imp.updateCorrected(this);
                if (shift) {
                    new EDImageEditorTool().applyCore(imp, this);
                    dragging = false;
                    return;
                }
                currentTool.apply(imp, this, true, false);
                if (oldTool != currentTool) {
                    dragging = false;
                    oldTool.endApply(this);
                }
            } else {
                if (shift)
                    return;
                int absX = Math.abs(ax - nx);
                int absY = Math.abs(ay - ny);
                /*
                 * Consider:
                 * A+X
                 *    +B
                 * With a pure integer method, point X would be down by one, we'd go past B
                 * So instead use this sort of fixed point method.
                 */
                int sub = absX;

                int subV = sub;
                int subS = absY;

                while ((absX > 0) || (absY > 0)) {
                    imp.x = ax;
                    imp.y = ay;
                    imp.updateCorrected(this);
                    currentTool.apply(imp, this, false, !first);
                    if (oldTool != currentTool) {
                        dragging = false;
                        oldTool.endApply(this);
                        return;
                    }

                    subV -= subS;
                    boolean firstApp = true;
                    while ((subV <= 0) && (absY > 0)) {
                        if (!firstApp) {
                            imp.x = ax;
                            imp.y = ay;
                            imp.updateCorrected(this);
                            currentTool.apply(imp, this, false, !first);
                            if (oldTool != currentTool) {
                                dragging = false;
                                oldTool.endApply(this);
                                return;
                            }
                        }
                        firstApp = false;
                        // Move perpendicular
                        if (ay < ny) {
                            ay++;
                            absY--;
                        } else if (ay > ny) {
                            ay--;
                            absY--;
                        }
                        subV += sub;
                    }
                    // Move
                    if (ax < nx) {
                        ax++;
                        absX--;
                    } else if (ax > nx) {
                        ax--;
                        absX--;
                    }
                }

                if ((ax != nx) || (ay != ny))
                    System.out.println("Warning " + ax + "," + ay + ":" + nx + "," + ny);
                imp.x = nx;
                imp.y = ny;
                imp.updateCorrected(this);
                currentTool.apply(imp, this, true, !first);
                if (oldTool != currentTool) {
                    dragging = false;
                    oldTool.endApply(this);
                }
            }
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

    public static class ImPoint {
        public int x, y;
        public int correctedX, correctedY;

        public ImPoint(int px, int py) {
            x = px;
            y = py;
        }

        public void updateCorrected(UIImageEditView iev) {
            int ofsX = 0;
            int ofsY = 0;
            int ofsW = iev.image.width;
            int ofsH = iev.image.height;
            if (iev.tiling != null) {
                ofsX = iev.tiling.x;
                ofsY = iev.tiling.y;
                ofsW = iev.tiling.width;
                ofsH = iev.tiling.height;
            }
            correctedX = x - ofsX;
            correctedY = y - ofsY;
            correctedX = UIElement.sensibleCellMod(correctedX, ofsW);
            correctedY = UIElement.sensibleCellMod(correctedY, ofsH);
            correctedX += ofsX;
            correctedY += ofsY;
        }
    }
}

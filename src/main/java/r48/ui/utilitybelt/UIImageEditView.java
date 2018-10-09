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

        IImage tempImg = image.rasterize();
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
                blitTiledScaledImage(igd, ofsX, ofsY, ofsW, ofsH, soX + (soW * i), soY + (soH * j), soW, soH, tempImg);

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

    private void blitTiledScaledImage(IGrDriver igd, int ofsX, int ofsY, int ofsW, int ofsH, int x, int y, int soW, int soH, IImage tempImg) {
        if (ofsW <= 0 || ofsH <= 0)
            return;
        int tiW = tempImg.getWidth();
        int tiH = tempImg.getHeight();
        if ((ofsX + ofsW > tiW) || (ofsY + ofsH > tiH)) {
            // It needs horizontal tiling.
            int part1W = tiW - ofsX;
            int part1SW = (soW * part1W) / ofsW;
            int part1H = tiH - ofsY;
            int part1SH = (soH * part1H) / ofsH;
            igd.blitScaledImage(ofsX, ofsY, part1W, part1H, x, y, part1SW, part1SH, tempImg);
            // -+
            blitTiledScaledImage(igd, ofsX, (ofsY + part1H) % tiH, part1W, ofsH - part1H, x, y + part1SH, part1SW, soH - part1SH, tempImg);
            // +-
            blitTiledScaledImage(igd, (ofsX + part1W) % tiW, ofsY, ofsW - part1W, part1H, x + part1SW, y, soW - part1SW, part1SH, tempImg);
            // ++
            blitTiledScaledImage(igd, (ofsX + part1W) % tiW, (ofsY + part1H) % tiH, ofsW - part1W, ofsH - part1H, x + part1SW, y + part1SH, soW - part1SW, soH - part1SH, tempImg);
        } else {
            igd.blitScaledImage(ofsX, ofsY, ofsW, ofsH, x, y, soW, soH, tempImg);
        }
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

    public void handleAct(int x, int y, final boolean first) {
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
            final IImageEditorTool oldTool = currentTool;
            final ImPoint imp = new ImPoint(nx, ny);
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
                final LineAlgorithm lineDraw = new LineAlgorithm();
                lineDraw.ax = ax;
                lineDraw.ay = ay;
                IFunction<Boolean, Boolean> plotPoint = new IFunction<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean major) {
                        imp.x = lineDraw.ax;
                        imp.y = lineDraw.ay;
                        imp.updateCorrected(UIImageEditView.this);
                        currentTool.apply(imp, UIImageEditView.this, major, true);
                        if (oldTool != currentTool) {
                            dragging = false;
                            oldTool.endApply(UIImageEditView.this);
                            return false;
                        }
                        return true;
                    }
                };
                // Ignore the return value, since returning anyway.
                lineDraw.run(nx, ny, plotPoint);
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
            if (iev.tiling != null) {
                int ofsX = iev.tiling.x;
                int ofsY = iev.tiling.y;
                int ofsW = iev.tiling.width;
                int ofsH = iev.tiling.height;
                correctedX = x - ofsX;
                correctedY = y - ofsY;
                correctedX = UIElement.sensibleCellMod(correctedX, ofsW);
                correctedY = UIElement.sensibleCellMod(correctedY, ofsH);
                correctedX += ofsX;
                correctedY += ofsY;
            }
            correctedX = UIElement.sensibleCellMod(correctedX, iev.image.width);
            correctedY = UIElement.sensibleCellMod(correctedY, iev.image.height);
        }
    }
}

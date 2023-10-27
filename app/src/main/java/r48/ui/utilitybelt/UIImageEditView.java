/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import java.util.function.Function;

import gabien.render.*;
import gabien.ui.*;
import gabien.uslx.append.*;
import gabien.wsi.*;
import r48.App;
import r48.ui.Art;
import r48.ui.UIPlaneView;

/**
 * Thanks to Tomeno for the general design of this UI in a tablet-friendly pixel-arty way.
 * Though I kind of modified those plans a bit.
 */
public class UIImageEditView extends UIPlaneView {
    // Do NOT set outside of setImage
    public ImageEditorImage image = new ImageEditorImage(32, 32);
    public ImageEditorEDS eds = new ImageEditorEDS();
    private boolean shift;
    public Rect grid = new Rect(0, 0, 16, 16);

    public ImageEditorTool currentTool;
    public int selPaletteIndex;

    public Rect tiling;
    public int gridColour = 0x800080;
    public boolean gridST = false;

    public Runnable newToolCallback;

    public UIImageEditView(App app, ImageEditorTool rootTool, Runnable updatePal) {
        super(app);
        planeZoomMul = app.f.getSpriteScale() * 16;
        eds.currentImage = image;
        eds.newFile();
        newToolCallback = updatePal;
        currentTool = new CamImageEditorTool(rootTool);
    }

    // Only write to image from here!
    public void setImage(ImageEditorImage n) {
        selPaletteIndex = 0;
        if (n.paletteSize() > 1)
            selPaletteIndex = 1;
        image = n;
        eds.currentImage = n;
        if (tiling != null)
            tiling = new Rect(UIElement.sensibleCellMod(tiling.x, image.width), UIElement.sensibleCellMod(tiling.y, image.height), tiling.width, tiling.height);
    }

    public FillAlgorithm.Point correctPoint(int x, int y) {
        if (tiling != null) {
            int ofsX = tiling.x;
            int ofsY = tiling.y;
            int ofsW = tiling.width;
            int ofsH = tiling.height;
            x -= ofsX;
            y -= ofsY;
            x = UIElement.sensibleCellMod(x, ofsW);
            y = UIElement.sensibleCellMod(y, ofsH);
            x += ofsX;
            y += ofsY;
            x = UIElement.sensibleCellMod(x, image.width);
            y = UIElement.sensibleCellMod(y, image.height);
        }
        if (x < 0)
            return null;
        if (y < 0)
            return null;
        if (x >= image.width)
            return null;
        if (y >= image.height)
            return null;
        return new FillAlgorithm.Point(x, y);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        shift = false;
        if (peripherals instanceof IDesktopPeripherals)
            shift = ((IDesktopPeripherals) peripherals).isKeyDown(IGrInDriver.VK_SHIFT);
    }

    @Override
    public String planeGetStatus() {
        return currentTool.getLocalizedText(true);
    }

    @Override
    public boolean planeGetDragLock() {
        return currentTool.getCamModeLT() != null;
    }

    @Override
    public void planeToggleDragLock() {
        ImageEditorTool lt = currentTool.getCamModeLT();
        if (lt != null) {
            currentTool = lt;
        } else {
            currentTool = new CamImageEditorTool(currentTool);
        }
        newToolCallback.run();
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
        int soX = viewRct.x + (int) planeMulZoom(ofsX);
        int soY = viewRct.y + (int) planeMulZoom(ofsY);
        int soIX = bounds.width - soX;
        int soIY = bounds.height - soY;
        int soW = (int) planeMulZoom(ofsW);
        int soH = (int) planeMulZoom(ofsH);
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
            Art.drawSelectionBox(viewRct.x + (int) planeMulZoom(ofsX), viewRct.y + (int) planeMulZoom(ofsY), (int) planeMulZoom(ofsW), (int) planeMulZoom(ofsH), app.f.getSpriteScale(), igd);


        Rect theSelection = currentTool.getSelection();
        if (theSelection != null) {
            if (theSelection.width == 0 && theSelection.height == 0) {
                Art.drawTarget(viewRct.x + (int) planeMulZoom(theSelection.x), viewRct.y + (int) planeMulZoom(theSelection.y), (int) planeMulZoom(1), igd);
            } else {
                Art.drawSelectionBox(viewRct.x + (int) planeMulZoom(theSelection.x), viewRct.y + (int) planeMulZoom(theSelection.y), (int) planeMulZoom(theSelection.width), (int) planeMulZoom(theSelection.height), app.f.getSpriteScale(), igd);
            }
        }

        super.render(igd);
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
        int lineThickness = app.f.getSpriteScale() * 2;
        // viewRct is in zoomed osb-local coordinates.
        // It's the rectangle of the image.
        // localGrid is in zoomed osb-local coordinates.
        // It's X/Y is the viewrect x/y + the grid offset - 1 grid cell (so the offset stuff is correct) scaled,
        //  and it's W/H is the grid size scaled.
        Rect localGrid = getLocalGridRect(viewRct);
        boolean outerFlip = false;
        RectIntersector intersect = RectIntersector.THREAD_LOCAL.get();
        for (int ofx = (localGrid.x - (int) planeMulZoom(grid.width)); ofx < (viewRct.x + viewRct.width); ofx += localGrid.width) {
            if (!viewRct.intersects(new Rect(ofx, viewRct.y, localGrid.width, viewRct.height)))
                continue;
            int i = outerFlip ? 1 : 0;
            outerFlip = !outerFlip;
            for (int ofy = (localGrid.y - (int) planeMulZoom(grid.height)); ofy < (viewRct.y + viewRct.height); ofy += localGrid.height) {
                int o = 0xA0;
                boolean light = ((i & 1) != 0);
                if (light)
                    o = 0xFF;
                // The osb.clearRect call alters the Intersect, but that's fine since it gets reset.
                intersect.set(viewRct);
                if (intersect.intersect(ofx, ofy, localGrid.width, localGrid.height)) {
                    int ix = intersect.x;
                    int iy = intersect.y;
                    int iw = intersect.width;
                    int ih = intersect.height;

                    int rR = (gcR * o) / 255;
                    int rG = (gcG * o) / 255;
                    int rB = (gcB * o) / 255;
                    if (cut) {
                        osb.clearRect(rR, rG, rB, ix, iy, lineThickness, ih);
                        osb.clearRect(rR, rG, rB, ix + iw - lineThickness, iy, lineThickness, ih);
                        osb.clearRect(rR, rG, rB, ix + lineThickness, iy, iw - (lineThickness * 2), lineThickness);
                        osb.clearRect(rR, rG, rB, ix + lineThickness, iy + ih - lineThickness, iw - (lineThickness * 2), lineThickness);
                    } else {
                        osb.clearRect(rR, rG, rB, ix, iy, iw, ih);
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
        localAnchorX = (int) planeMulZoom(localAnchorX);
        localAnchorY = (int) planeMulZoom(localAnchorY);
        return new Rect(viewRct.x + localAnchorX, viewRct.y + localAnchorY, (int) planeMulZoom(grid.width), (int) planeMulZoom(grid.height));
    }

    // Returns a rectangle whose coordinates are the coordinates of the image on-screen.
    // (The whole image, ignoring tiling; tiling is primarily a scissor with added special effects.)
    // Everything is derived from this.
    private Rect getViewRect() {
        Size bounds = getSize();
        return new Rect((bounds.width / 2) - (int) planeMulZoom(camX), (bounds.height / 2) - (int) planeMulZoom(camY), (int) planeMulZoom(image.width), (int) planeMulZoom(image.height));
    }

    @Override
    public IPointerReceiver planeHandleDrawPointer(IPointer state) {
        return new IPointerReceiver() {
            int dragLastX, dragLastY;
            // Setting this to null is used for forceful deactivation.
            ImageEditorTool lockedTool = currentTool;

            @Override
            public void handlePointerBegin(IPointer state) {
                dragLastX = state.getX();
                dragLastY = state.getY();
                handleAct(state.getX(), state.getY(), true);
            }

            @Override
            public void handlePointerUpdate(IPointer state) {
                handleAct(state.getX(), state.getY(), false);
                dragLastX = state.getX();
                dragLastY = state.getY();
            }

            @Override
            public void handlePointerEnd(IPointer state) {
                if (currentTool == lockedTool)
                    currentTool.endApply(UIImageEditView.this);
            }

            public void handleAct(int x, int y, final boolean first) {
                if (currentTool != lockedTool)
                    return;

                Rect bounds = getViewRect();
                int ax = planeDivZoomFloor(dragLastX - bounds.x);
                int ay = planeDivZoomFloor(dragLastY - bounds.y);
                int nx = planeDivZoomFloor(x - bounds.x);
                int ny = planeDivZoomFloor(y - bounds.y);

                if (first) {
                    if (shift) {
                        FillAlgorithm.Point p = correctPoint(nx, ny);
                        if (p != null)
                            new EDImageEditorTool(app).applyCore(p, UIImageEditView.this);
                        lockedTool = null;
                    } else {
                        lockedTool.apply(nx, ny, UIImageEditView.this, true, false);
                        if (currentTool != lockedTool)
                            lockedTool.endApply(UIImageEditView.this);
                    }
                } else {
                    if (shift)
                        return;
                    final LineAlgorithm lineDraw = new LineAlgorithm();
                    lineDraw.ax = ax;
                    lineDraw.ay = ay;
                    Function<Boolean, Boolean> plotPoint = new Function<Boolean, Boolean>() {
                        @Override
                        public Boolean apply(Boolean major) {
                            FillAlgorithm.Point p = correctPoint(lineDraw.ax, lineDraw.ay);
                            if (p != null)
                                lockedTool.apply(p.x, p.y, UIImageEditView.this, major, true);
                            if (currentTool != lockedTool) {
                                lockedTool.endApply(UIImageEditView.this);
                                return false;
                            }
                            return true;
                        }
                    };
                    // Ignore the return value, since returning anyway.
                    lineDraw.run(nx, ny, plotPoint);
                }
            }
        };
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrDriver;
import gabien.ui.*;
import r48.FontSizes;

import java.util.HashMap;

/**
 * A new base class for all of the UI code that I keep replicating between UIMapView & UIImageEditView.
 * Created on November 18, 2018.
 */
public abstract class UIPlaneView extends UIElement {

    private final UILabel.StatusLine planeStatusLine = new UILabel.StatusLine();

    public int planeZoomMul = 1;
    public int planeZoomDiv = 1;
    // The centre of the view in planespace.
    public double camX, camY;

    private HashMap<IPointer, Size> dragPointers = new HashMap<IPointer, Size>();
    private double dragNexusX, dragNexusY, firstDragDist, firstDragZoom, dragAvgDist;

    protected abstract String planeGetStatus();

    protected abstract boolean planeGetDragLock();

    protected abstract void planeToggleDragLock();

    protected abstract IPointerReceiver planeHandleDrawPointer(IPointer state);

    protected boolean planeCanZoom(boolean north) {
        if (!north)
            if (planeZoomMul == 1)
                return false;
        return true;
    }

    protected void planeZoomLogic(boolean north) {
        if (north) {
            if (planeZoomDiv > 1) {
                planeZoomDiv /= 2;
            } else {
                planeZoomMul *= 2;
            }
        } else {
            planeZoomMul /= 2;
            if (planeZoomMul < 1)
                planeZoomMul = 1;
        }
    }

    protected double planeMulZoom(double n) {
        return ((n * planeZoomMul) / planeZoomDiv);
    }

    protected double planeDivZoom(double i) {
        return (i * planeZoomDiv) / planeZoomMul;
    }

    @Override
    public void render(IGrDriver igd) {
        Rect plusRect = Art.getZIconRect(false, 0);
        Rect plusRectFull = Art.getZIconRect(true, 0); // used for X calc on the label
        Rect minusRect = Art.getZIconRect(false, 1);
        Rect dragRect = Art.getZIconRect(false, 2);

        int textX = plusRectFull.x + plusRectFull.width;
        int textW = getSize().width - (textX + ((plusRectFull.width - plusRect.width) / 2));
        String statusText = planeGetStatus();
        if (statusText != null)
            planeStatusLine.draw(statusText, FontSizes.mapPositionTextHeight, igd, textX, plusRect.y, textW);

        if (planeCanZoom(true))
            Art.drawZoom(igd, true, plusRect.x, plusRect.y, plusRect.height);
        if (planeCanZoom(false))
            Art.drawZoom(igd, false, minusRect.x, minusRect.y, minusRect.height);
        Art.drawDragControl(igd, planeGetDragLock(), dragRect.x, dragRect.y, minusRect.height);
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        if (state.getType() == IPointer.PointerType.Right) {
            return handleDragPointer();
        } else if (state.getType() != IPointer.PointerType.Generic)
            return super.handleNewPointer(state);
        int x = state.getX();
        int y = state.getY();
        Size bSize = getSize();
        if (planeCanZoom(true) && Art.getZIconRect(true, 0).contains(x, y)) {
            handleMousewheel(bSize.width / 2, bSize.height / 2, true);
        } else if (planeCanZoom(false) && Art.getZIconRect(true, 1).contains(x, y)) {
            handleMousewheel(bSize.width / 2, bSize.height / 2, false);
        } else if (Art.getZIconRect(true, 2).contains(x, y)) {
            planeToggleDragLock();
        } else if (planeGetDragLock()) {
            return handleDragPointer();
        } else {
            // Ordinary pointer
            return planeHandleDrawPointer(state);
        }
        return super.handleNewPointer(state);
    }

    private IPointerReceiver handleDragPointer() {
        return new IPointerReceiver() {
            @Override
            public void handlePointerBegin(IPointer state) {
                pokePointer(state);
                recalcNexus();
                lockZoom();
            }

            private void pokePointer(IPointer state) {
                dragPointers.put(state, new Size(state.getX(), state.getY()));
            }

            private void recalcNexus() {
                dragNexusX = 0;
                dragNexusY = 0;
                dragAvgDist = 0;
                for (Size ip : dragPointers.values()) {
                    dragNexusX += ip.width / dragPointers.size();
                    dragNexusY += ip.height / dragPointers.size();
                }
                for (Size ip : dragPointers.values()) {
                    double x = ip.width - dragNexusX;
                    double y = ip.height - dragNexusY;
                    dragAvgDist += Math.sqrt((x * x) + (y * y)) / dragPointers.size();
                }
            }

            @Override
            public void handlePointerUpdate(IPointer state) {
                pokePointer(state);
                double oldDNX = dragNexusX;
                double oldDNY = dragNexusY;
                recalcNexus();
                // Move camera in opposite direction of drag, thus emulating moving object in drag direction.
                camX += planeDivZoom(oldDNX - dragNexusX);
                camY += planeDivZoom(oldDNY - dragNexusY);
                // And now for the finale; pinch/n/zoom!
                if (dragPointers.size() > 1) {
                    // coreRatio is the actual distance ratio from the last time lockZoom was called.
                    // Squaring it seems to actually do exactly what is wanted, oddly enough
                    double coreRatio = (dragAvgDist / firstDragDist);
                    double ratioW = firstDragZoom * coreRatio * coreRatio;
                    double ratioF = tryZoom(false);
                    double ratioT = tryZoom(true);
                    if (ratioW <= ratioF) {
                        handleMousewheel((int) dragNexusX, (int) dragNexusY, false);
                    } else if (ratioW >= ratioT) {
                        handleMousewheel((int) dragNexusX, (int) dragNexusY, true);
                    }
                }
            }

            private double tryZoom(boolean b) {
                int oldM = planeZoomMul;
                int oldD = planeZoomDiv;
                planeZoomLogic(b);
                double r2 = planeMulZoom(1);
                planeZoomMul = oldM;
                planeZoomDiv = oldD;
                return r2;
            }

            @Override
            public void handlePointerEnd(IPointer state) {
                pokePointer(state);
                dragPointers.remove(state);
                // Used to prevent any 'rebound'
                recalcNexus();
                lockZoom();
            }

            private void lockZoom() {
                firstDragDist = dragAvgDist;
                firstDragZoom = planeMulZoom(1);
            }
        };
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        if (!planeCanZoom(north))
            return;
        // Transform target point into plane coordinates
        Size bSize = getSize();
        double zx = planeDivZoom(x - (bSize.width / 2.0d));
        double zy = planeDivZoom(y - (bSize.height / 2.0d));
        planeZoomLogic(north);
        // Re-transform and apply difference
        double z2x = planeDivZoom(x - (bSize.width / 2.0d));
        double z2y = planeDivZoom(y - (bSize.height / 2.0d));
        camX += zx - z2x;
        camY += zy - z2y;
    }
}

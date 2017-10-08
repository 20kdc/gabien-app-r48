/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui.utilitybelt;

import gabien.*;
import gabien.ui.ISupplier;
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
        // Maybe cache this for perf.
        IGrDriver osb = GaBIEn.makeOffscreenBuffer(bounds.width, bounds.height, false);
        osb.clearRect(32, 32, 32, 0, 0, bounds.width, bounds.height);
        int gcR = (gridColour >> 16) & 0xFF;
        int gcG = (gridColour >> 8) & 0xFF;
        int gcB = gridColour & 0xFF;
        osb.clearRect(gcR / 3, gcG / 3, gcB / 3, viewRct.x, viewRct.y, viewRct.width, viewRct.height);
        Rect localGrid = getLocalGridRect(viewRct);
        boolean primaryX = (UIGrid.sensibleCellDiv(cursorX - gridOX, gridW) & 1) != 0;
        boolean primaryY = (UIGrid.sensibleCellDiv(cursorY - gridOY, gridH) & 1) != 0;
        int gridReach = 9;
        for (int i = 0; i < (gridReach * gridReach); i++) {
            int ofx = ((i % gridReach) - (gridReach / 2)) * localGrid.width;
            int ofy = ((i / gridReach) - (gridReach / 2)) * localGrid.height;
            int o = 0x80;
            boolean light = ((i & 1) != 0) ^ primaryX ^ primaryY;
            if (light)
                o = 0xC0;
            Rect subLocalGrid = viewRct.getIntersection(new Rect(localGrid.x + ofx, localGrid.y + ofy, localGrid.width, localGrid.height));
            if (subLocalGrid != null)
                osb.clearRect((gcR * o) / 255, (gcG * o) / 255, (gcB * o) / 255, subLocalGrid.x, subLocalGrid.y, subLocalGrid.width, subLocalGrid.height);
        }
        IImage tempImg = createImg();
        osb.blitScaledImage(0, 0, imageW, imageH, viewRct.x, viewRct.y, viewRct.width, viewRct.height, tempImg);
        Art.drawSelectionBox(viewRct.x + (cursorX * zoom), viewRct.y + (cursorY * zoom), zoom, zoom, osb);
        if (showTarget)
            Art.drawTarget(viewRct.x + (targetX * zoom), viewRct.y + (targetY * zoom), zoom, osb);
        igd.blitImage(0, 0, bounds.width, bounds.height, ox, oy, osb);
        osb.shutdown();

        Rect zPlus = getZPlusRect();
        Rect zMinus = getZMinusRect();
        int textX = zPlus.x + zPlus.width + getZoomButtonMargin();
        String text = cursorX + ", " + cursorY;
        UILabel.drawLabel(igd, bounds.width - (textX + getZoomButtonMargin()), ox + textX, oy + getZoomButtonMargin(), text,  0, FontSizes.mapPositionTextHeight);
        Art.drawZoom(igd, true, zPlus.x + ox, zPlus.y + oy, zPlus.height);
        Art.drawZoom(igd, false, zMinus.x + ox, zMinus.y + oy, zMinus.height);
    }

    private Rect getLocalGridRect(Rect viewRct) {
        int localAnchorX = (UIGrid.sensibleCellDiv(cursorX - gridOX, gridW) * gridW) + gridOX;
        int localAnchorY = (UIGrid.sensibleCellDiv(cursorY - gridOY, gridH) * gridH) + gridOY;
        return new Rect(viewRct.x + (localAnchorX * zoom), viewRct.y + (localAnchorY * zoom), gridW * zoom, gridH * zoom);
    }

    private Rect getViewRect() {
        Rect bounds = getBounds();
        int camOfsX = -((cursorX * zoom) + (zoom / 2));
        int camOfsY = -((cursorY * zoom) + (zoom / 2));
        return new Rect(camOfsX + (bounds.width / 2), camOfsY + (bounds.height / 2), imageW * zoom, imageH * zoom);
    }

    public int getZoomButtonSize() {
        return UILabel.getRecommendedSize("", FontSizes.mapPositionTextHeight).height;
    }

    public int getZoomButtonMargin() {
        return FontSizes.scaleGuess(4);
    }

    private Rect getZPlusRect() {
        int zbs = getZoomButtonSize();
        int zbm = getZoomButtonMargin();
        return new Rect(zbm, zbm, zbs, zbs);
    }

    private Rect getZMinusRect() {
        int zbs = getZoomButtonSize();
        int zbm = getZoomButtonMargin();
        return new Rect(zbm, (zbm * 2) + zbs, zbs, zbs);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (button != 1)
            return;
        if (getZPlusRect().contains(x, y)) {
            handleMousewheel(x, y, true);
            return;
        }
        if (getZMinusRect().contains(x, y)) {
            handleMousewheel(x, y, false);
            return;
        }
        Rect bounds = getViewRect();
        int nx = UIGrid.sensibleCellDiv(x - bounds.x, zoom);
        int ny =  UIGrid.sensibleCellDiv(y - bounds.y, zoom);
        nx -= UIGrid.sensibleCellDiv(nx, imageW) * imageW;
        ny -= UIGrid.sensibleCellDiv(ny, imageH) * imageH;
        if (nx == cursorX)
            if (ny == cursorY)
                colour.run();
        cursorX = nx;
        cursorY = ny;
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        if (north) {
            zoom++;
        } else {
            zoom--;
            if (zoom < 3)
                zoom = 3;
        }
    }

    public IImage createImg() {
        return GaBIEn.createImage(image, imageW, imageH);
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui.utilitybelt;

import gabien.*;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.FontSizes;
import r48.ui.Art;

/**
 * Thanks to Tomeno for the general design of this UI in a tablet-friendly pixel-arty way.
 */
public class UIImageEditView extends UIElement {
    public int[] image = new int[256 * 256];
    public int imageW = 256, imageH = 256, cursorX = 128, cursorY = 128, zoom = FontSizes.getSpriteScale() * 16;

    public UIImageEditView() {

    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        Rect bounds = getBounds();
        Rect viewRct = getViewRect();
        IGrDriver osb = GaBIEn.makeOffscreenBuffer(bounds.width, bounds.height, false);
        osb.clearRect(16, 16, 16, 0, 0, bounds.width, bounds.height);
        IImage tempImg = GaBIEn.createImage(image, imageW, imageH);
        osb.blitScaledImage(0, 0, imageW, imageH, viewRct.x, viewRct.y, viewRct.width, viewRct.height, tempImg);
        Art.drawSelectionBox(viewRct.x + (cursorX * zoom), viewRct.y + (cursorY * zoom), zoom, zoom, osb);
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
            zoom++;
            return;
        }
        if (getZMinusRect().contains(x, y)) {
            zoom--;
            if (zoom < 3)
                zoom = 3;
            return;
        }
        Rect bounds = getViewRect();
        cursorX = (x - bounds.x) / zoom;
        cursorY = (y - bounds.y) / zoom;
        cursorX %= imageW;
        cursorY %= imageH;
    }
}

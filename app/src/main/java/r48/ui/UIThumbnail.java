/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui;

import gabien.render.IGrDriver;
import gabien.render.ITexRegion;
import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import gabien.wsi.IPeripherals;

/**
 * Created on September 03, 2018.
 */
public class UIThumbnail extends UIElement {
    public ITexRegion viewedImage;
    private Rect drawRect;
    private int wantedW;

    public UIThumbnail(ITexRegion im) {
        this(im, (int) im.getRegionWidth());
    }

    public UIThumbnail(ITexRegion im, int wanted) {
        super(wanted, (int) ((im.getRegionHeight() * wanted) / im.getRegionWidth()));
        wantedW = wanted;
        viewedImage = im;
        drawRect = new Rect(getWantedSize());
    }

    public static Rect getDrawRect(Size bounds, float contentsW, float contentsH) {
        double scale = Math.min((double) bounds.width / contentsW, (double) bounds.height / contentsH);

        int bw = (int) (contentsW * scale);
        int bh = (int) (contentsH * scale);

        int bx = (bounds.width - bw) / 2;
        int by = (bounds.height - bh) / 2;

        return new Rect(bx, by, bw, bh);
    }

    @Override
    public void runLayout() {
        float irW = viewedImage.getRegionWidth(), irH = viewedImage.getRegionHeight();
        drawRect = getDrawRect(getSize(), irW, irH);
        int efW = Math.min(getSize().width, wantedW);
        setWantedSize(new Size(wantedW, (int) ((irH * efW) / irW)));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void render(IGrDriver igd) {
        igd.blitScaledImage(drawRect.x, drawRect.y, drawRect.width, drawRect.height, viewedImage);
    }
}

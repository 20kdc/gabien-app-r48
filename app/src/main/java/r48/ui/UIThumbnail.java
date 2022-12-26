/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui;

import gabien.IGrDriver;
import gabien.IImage;
import gabien.IPeripherals;
import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement;

/**
 * Created on September 03, 2018.
 */
public class UIThumbnail extends UIElement {
    public IImage viewedImage;
    private Rect drawRect;
    private Rect imgRegion;
    private int wantedW;

    public UIThumbnail(IImage im) {
        this(im, im.getWidth(), new Rect(0, 0, im.getWidth(), im.getHeight()));
    }

    public UIThumbnail(IImage im, int wanted, Rect imgReg) {
        super(wanted, (imgReg.height * wanted) / imgReg.width);
        wantedW = wanted;
        imgRegion = imgReg;
        viewedImage = im;
        drawRect = new Rect(getWantedSize());
    }

    public static Rect getDrawRect(Size bounds, int contentsW, int contentsH) {
        double scale = Math.min((double) bounds.width / contentsW, (double) bounds.height / contentsH);

        int bw = (int) (contentsW * scale);
        int bh = (int) (contentsH * scale);

        int bx = (bounds.width - bw) / 2;
        int by = (bounds.height - bh) / 2;

        return new Rect(bx, by, bw, bh);
    }

    @Override
    public void runLayout() {
        drawRect = getDrawRect(getSize(), imgRegion.width, imgRegion.height);
        int efW = Math.min(getSize().width, wantedW);
        setWantedSize(new Size(wantedW, (imgRegion.height * efW) / imgRegion.width));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void render(IGrDriver igd) {
        igd.blitScaledImage(imgRegion.x, imgRegion.y, imgRegion.width, imgRegion.height, drawRect.x, drawRect.y, drawRect.width, drawRect.height, viewedImage);
    }
}

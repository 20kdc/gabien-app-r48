/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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

    public UIThumbnail(IImage im) {
        super(im.getWidth(), im.getHeight());
        viewedImage = im;
        drawRect = new Rect(0, 0, im.getWidth(), im.getHeight());
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
        drawRect = getDrawRect(getSize(), viewedImage.getWidth(), viewedImage.getHeight());
        // This treats horizontal size as the 'limiter'
        Rect tmp = getDrawRect(new Size(getSize().width, viewedImage.getHeight()), viewedImage.getWidth(), viewedImage.getHeight());
        setWantedSize(new Size(viewedImage.getWidth(), tmp.height));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void render(IGrDriver igd) {
        igd.blitScaledImage(0, 0, viewedImage.getWidth(), viewedImage.getHeight(), drawRect.x, drawRect.y, drawRect.width, drawRect.height, viewedImage);
    }
}

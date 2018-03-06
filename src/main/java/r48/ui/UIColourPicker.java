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
import r48.dbs.TXDB;

public class UIColourPicker extends UIElement.UIPanel {
    public UIPublicPanel colourPanel;
    public IConsumer<Integer> result;
    public int currentMainSpriteScale = 1;
    public int x, y;
    public UIScrollbar hueScroll, alphaScroll;
    public UISplitterLayout alphaContainer;
    public UIColourSwatch swatch;
    public UITextButton confirmer;

    public UIColourPicker(IConsumer<Integer> iConsumer, boolean alpha) {
        super();
        result = iConsumer;
        colourPanel = new UIPublicPanel(256, 256) {
            int lastHue = 0;

            @Override
            public void render(IGrDriver igd) {
                int newHue = (int) Math.floor((hueScroll.scrollPoint * 359) + 0.5d);
                if (lastHue != newHue) {
                    baseImage = Art.getColourPal(newHue);
                    lastHue = newHue;
                }
                super.render(igd);
                int margin = currentMainSpriteScale * 4;
                Art.drawSelectionBox(((x * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - (margin / 2),  ((y * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - (margin / 2), margin + 1, margin + 1, 1, igd);
            }

            @Override
            public void handlePointerBegin(IPointer state) {
                if (state.getType() != IPointer.PointerType.Generic)
                    return;
                x = state.getX() / currentMainSpriteScale;
                y = state.getY() / currentMainSpriteScale;
                restrictXY();
            }

            @Override
            public void handlePointerUpdate(IPointer state) {
                if (state.getType() != IPointer.PointerType.Generic)
                    return;
                x = state.getX() / currentMainSpriteScale;
                y = state.getY() / currentMainSpriteScale;
                restrictXY();
            }

            private void restrictXY() {
                if (x < 0)
                    x = 0;
                if (y < 0)
                    y = 0;
                if (x > 255)
                    x = 255;
                if (y > 255)
                    y = 255;
            }
        };
        hueScroll = new UIScrollbar(true, FontSizes.generalScrollersize);
        int alphaScrollH = UILabel.getRecommendedTextSize("", FontSizes.schemaFieldTextHeight).height;
        if (alpha) {
            alphaScroll = new UIScrollbar(false, alphaScrollH);
            alphaScroll.scrollPoint = 1.0d;
            alphaContainer = new UISplitterLayout(new UILabel(TXDB.get("Alpha"), FontSizes.schemaFieldTextHeight), alphaScroll, false, 0d);
        }
        swatch = new UIColourSwatch(0);
        colourPanel.baseImage = Art.getColourPal(0);
        colourPanel.imageScale = true;
        colourPanel.imageSW = 256;
        colourPanel.imageSH = 256;
        currentMainSpriteScale = Math.max(1, FontSizes.getSpriteScale() / 2);
        confirmer = new UITextButton(TXDB.get("Set Colour"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                if (result != null) {
                    result.accept(swatch.col);
                    result = null;
                }
            }
        });
        layoutAddElement(colourPanel);
        layoutAddElement(hueScroll);
        if (alpha)
            layoutAddElement(alphaContainer);
        layoutAddElement(swatch);
        layoutAddElement(confirmer);

        runLayout();
        setForcedBounds(null, new Rect(getWantedSize()));
    }

    @Override
    public void render(IGrDriver igd) {
        int col = colourPanel.baseImage.getPixels()[x + (y * 256)];
        col &= 0xFFFFFF;
        if (alphaScroll != null) {
            int al = Math.min(255, Math.max(0, (int) (alphaScroll.scrollPoint * 255d)));
            col |= al << 24;
        } else {
            col |= 0xFF000000;
        }
        swatch.col = col;
        super.render(igd);
    }

    @Override
    public void runLayout() {
        Size r = getSize();
        int y = 0;

        int hsw = hueScroll.getWantedSize().width;
        currentMainSpriteScale = Math.max(1, (r.width - hsw) / 256);
        int remainder = r.width - ((currentMainSpriteScale * 256) + hsw);
        colourPanel.setForcedBounds(this, new Rect(remainder / 2, y, currentMainSpriteScale * 256, currentMainSpriteScale * 256));
        hueScroll.setForcedBounds(this, new Rect(r.width - hsw, y, hsw, currentMainSpriteScale * 256));
        y += currentMainSpriteScale * 256;

        int abh = FontSizes.generalScrollersize;
        if (alphaScroll != null) {
            abh = alphaContainer.getWantedSize().height;
            alphaContainer.setForcedBounds(this, new Rect(0, y, r.width, abh));
            y += abh;
        }

        swatch.setForcedBounds(this, new Rect(0, y, r.width, abh));
        y += abh;

        confirmer.setForcedBounds(this, new Rect(0, y, r.width, confirmer.getWantedSize().height));
        y += confirmer.getParentRelativeBounds().height;

        setWantedSize(new Size((currentMainSpriteScale * 256) + hueScroll.getWantedSize().width, y));
    }

    @Override
    public boolean requestsUnparenting() {
        return result == null;
    }

    @Override
    public void handleRootDisconnect() {
        super.handleRootDisconnect();
        if (result != null)
            result.accept(null);
    }

    @Override
    public String toString() {
        String text = Integer.toHexString(swatch.col);
        for (int i = text.length(); i < 8; i++)
            text = "0" + text;
        return "#" + text;
    }
}

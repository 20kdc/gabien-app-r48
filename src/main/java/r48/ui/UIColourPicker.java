/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.imagefx.HueShiftImageEffect;

public class UIColourPicker extends UIElement.UIPanel {
    public UIPublicPanel colourPanel;
    public UIElement hueScroll;
    public int hueDegree = 0;
    public IConsumer<Integer> result;
    public int currentMainSpriteScale = 1;
    public int x, y;
    public UIScrollbar alphaScroll;
    public UISplitterLayout alphaContainer;
    public UIColourSwatch swatch;
    public UITextButton confirmer;

    public UIColourPicker(IConsumer<Integer> iConsumer, boolean alpha) {
        super();
        currentMainSpriteScale = Math.max(1, FontSizes.getSpriteScale());
        result = iConsumer;
        colourPanel = new UIPublicPanel(256 * currentMainSpriteScale, 256 * currentMainSpriteScale) {
            int lastHue = 0;

            @Override
            public void render(IGrDriver igd) {
                int newHue = hueDegree;
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
                handlePointerBegin(state);
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
        hueScroll = new UIElement(16 * currentMainSpriteScale, 360 * currentMainSpriteScale) {
            private IImage renderMe;

            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

            }

            @Override
            public void render(IGrDriver igd) {
                if (renderMe == null) {
                    int[] cols = new int[360];
                    for (int i = 0; i < cols.length; i++) {
                        HueShiftImageEffect hsie = new HueShiftImageEffect(i - hueDegree);
                        cols[i] = hsie.processCol(0xFFFF0000);
                    }
                    renderMe = GaBIEn.createImage(cols, 1, 360);
                }
                igd.blitScaledImage(0, 0, 1, 360, 0, 0, 16 * currentMainSpriteScale, 360 * currentMainSpriteScale, renderMe);
                int margin = currentMainSpriteScale * 4;
                Art.drawSelectionBox(0, ((hueDegree * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - (margin / 2), 16 * currentMainSpriteScale, margin + 1, 1, igd);
            }

            @Override
            public void handlePointerBegin(IPointer state) {
                if (state.getType() != IPointer.PointerType.Generic)
                    return;
                hueDegree = state.getY() / currentMainSpriteScale;
                if (hueDegree < 0)
                    hueDegree = 0;
                if (hueDegree > 359)
                    hueDegree = 359;
            }

            @Override
            public void handlePointerUpdate(IPointer state) {
                handlePointerBegin(state);
            }
        };
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

        forceToRecommended();
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
        // (360 - 256) / 2 = 52
        colourPanel.setForcedBounds(this, new Rect(remainder / 2, y + (currentMainSpriteScale * 52), currentMainSpriteScale * 256, currentMainSpriteScale * 256));
        hueScroll.setForcedBounds(this, new Rect(r.width - hsw, y, hsw, currentMainSpriteScale * 360));
        y += currentMainSpriteScale * 360;

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
    public void onWindowClose() {
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

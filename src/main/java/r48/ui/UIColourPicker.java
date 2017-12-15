/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

public class UIColourPicker extends UIPanel implements IWindowElement {

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
        colourPanel = new UIPublicPanel() {
            boolean dragging = false;
            int lastHue = 0;

            @Override
            public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
                int newHue = (int) Math.floor((hueScroll.scrollPoint * 359) + 0.5d);
                if (lastHue != newHue) {
                    baseImage = Art.getColourPal(newHue);
                    lastHue = newHue;
                }
                super.updateAndRender(ox, oy, deltaTime, select, igd);
                int margin = currentMainSpriteScale * 4;
                Art.drawSelectionBox(ox + ((x * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - (margin / 2), oy + ((y * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - (margin / 2), margin + 1, margin + 1, 1, igd);
            }

            @Override
            public void handleClick(int xi, int yi, int button) {
                dragging = false;
                if (button != 1)
                    return;
                dragging = true;
                x = xi / currentMainSpriteScale;
                y = yi / currentMainSpriteScale;
                restrictXY();
            }

            @Override
            public void handleDrag(int xi, int yi) {
                if (dragging) {
                    x = xi / currentMainSpriteScale;
                    y = yi / currentMainSpriteScale;
                    restrictXY();
                }
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
        int alphaScrollH = UILabel.getRecommendedSize("", FontSizes.schemaFieldTextHeight).height;
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
        confirmer = new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Set Colour"), new Runnable() {
            @Override
            public void run() {
                if (result != null) {
                    result.accept(swatch.col);
                    result = null;
                }
            }
        });
        allElements.add(colourPanel);
        allElements.add(hueScroll);
        if (alpha)
            allElements.add(alphaContainer);
        allElements.add(swatch);
        allElements.add(confirmer);
        setBounds(new Rect(0, 0, (currentMainSpriteScale * 256) + hueScroll.getBounds().width, (currentMainSpriteScale * 256) + (alphaScrollH * (alpha ? 1 : 0)) + FontSizes.generalScrollersize + confirmer.getBounds().height));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        int col = colourPanel.baseImage.getPixels()[x + (y * 256)];
        col &= 0xFFFFFF;
        if (alphaScroll != null) {
            int al = Math.min(255, Math.max(0, (int) (alphaScroll.scrollPoint * 255d)));
            col |= al << 24;
        } else {
            col |= 0xFF000000;
        }
        swatch.col = col;
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }

    @Override
    public void setBounds(Rect r) {
        int hsw = hueScroll.getBounds().width;
        currentMainSpriteScale = Math.max(1, (r.width - hsw) / 256);
        int remainder = r.width - ((currentMainSpriteScale * 256) + hsw);
        colourPanel.setBounds(new Rect(remainder / 2, 0, currentMainSpriteScale * 256, currentMainSpriteScale * 256));
        hueScroll.setBounds(new Rect(r.width - hsw, 0, hsw, currentMainSpriteScale * 256));
        int abo = 0;
        int abh = FontSizes.generalScrollersize;
        if (alphaScroll != null) {
            abo = abh = alphaContainer.getBounds().height;
            alphaContainer.setBounds(new Rect(0, currentMainSpriteScale * 256, r.width, abh));
        }
        swatch.setBounds(new Rect(0, (currentMainSpriteScale * 256) + abo, r.width, abh));
        confirmer.setBounds(new Rect(0, (currentMainSpriteScale * 256) + abo + abh, r.width, confirmer.getBounds().height));
        super.setBounds(r);
    }

    @Override
    public boolean wantsSelfClose() {
        return result == null;
    }

    @Override
    public void windowClosed() {
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

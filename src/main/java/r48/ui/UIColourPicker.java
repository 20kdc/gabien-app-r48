/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

public class UIColourPicker extends UIPanel implements IWindowElement {

    public UIPanel colourPanel;
    public IConsumer<Integer> result;
    public int currentMainSpriteScale = 1;
    public int x, y;
    public UIScrollbar hueScroll, alphaScroll;
    public UISplitterLayout alphaContainer;

    public UIColourPicker(IConsumer<Integer> iConsumer) {
        super();
        result = iConsumer;
        colourPanel = new UIPanel() {
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
                Art.drawSelectionBox(ox + ((x * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - margin, oy + ((y * currentMainSpriteScale) + (currentMainSpriteScale / 2)) - margin, margin + 1, margin + 1, igd);
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
        alphaScroll = new UIScrollbar(false, UILabel.getRecommendedSize("", FontSizes.schemaFieldTextHeight).height);
        colourPanel.baseImage = Art.getColourPal(0);
        colourPanel.imageScale = true;
        colourPanel.imageSW = 256;
        colourPanel.imageSH = 256;
        alphaContainer = new UISplitterLayout(new UILabel(TXDB.get("Alpha"), FontSizes.schemaFieldTextHeight), alphaScroll, false, 0d);
        currentMainSpriteScale = Math.max(1, FontSizes.getSpriteScale() / 2);
        allElements.add(colourPanel);
        allElements.add(hueScroll);
        allElements.add(alphaContainer);
        setBounds(new Rect(0, 0, (currentMainSpriteScale * 256) + hueScroll.getBounds().width, currentMainSpriteScale * 256));
    }

    @Override
    public void setBounds(Rect r) {
        int hsw = hueScroll.getBounds().width;
        currentMainSpriteScale = Math.max(1, (r.width - hsw) / 256);
        int remainder = r.width - ((currentMainSpriteScale * 256) + hsw);
        colourPanel.setBounds(new Rect(remainder / 2, 0, currentMainSpriteScale * 256, currentMainSpriteScale * 256));
        hueScroll.setBounds(new Rect(r.width - hsw, 0, hsw, currentMainSpriteScale * 256));
        super.setBounds(r);
    }

    @Override
    public boolean wantsSelfClose() {
        return false;
    }

    @Override
    public void windowClosed() {
        int col = colourPanel.baseImage.getPixels()[x + (y * 256)];
        col &= 0xFFFFFF;
        int al = Math.min(255, Math.max(0, (int) (alphaScroll.scrollPoint * 255d)));
        col |= al << 24;
        result.accept(col);
    }
}

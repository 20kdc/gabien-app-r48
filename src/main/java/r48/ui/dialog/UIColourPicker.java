/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.dialog;

import gabien.FontManager;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.ui.Art;
import r48.ui.UIColourSwatch;
import r48.ui.UIThumbnail;

/**
 * RGBHSV
 * +--+--+
 * |S2|CV|
 * +--+  |
 * |a0|  |
 * +--+--+
 * |Ok|No|
 * +--+--+
 * Remade on August 12th, 2018
 */
public class UIColourPicker extends UIElement.UIProxy {
    private final String wTitle;

    // This is the colour that we agree every ColourPickerPanel has.
    private UIColourSwatch currentColour;
    private final UITabPane tabPane;
    private final IConsumer<Integer>[] colourListeners;
    private final UINumberBox alphaBox;
    private final Size numberBoxMinimumSize = UILabel.getRecommendedTextSize("_255_", FontSizes.imageEditorTextHeight);
    private boolean shuttingDown = false;

    public UIColourPicker(String purpose, int baseCol, final IConsumer<Integer> iConsumer, boolean alpha) {
        super();
        currentColour = new UIColourSwatch(baseCol);
        wTitle = purpose;
        tabPane = new UITabPane(FontSizes.imageEditorTextHeight, false, true);
        // IGNORE THE WARNING. There is no way to fix this.
        colourListeners = new IConsumer[] {
                new UIHSVColourView(FontSizes.getSpriteScale()),
                new UIRGBColourView()
        };
        UITabBar.TabIcon[] noIcons = new UITabBar.TabIcon[0];
        for (IConsumer<Integer> ici : colourListeners) {
            ici.accept(baseCol & 0xFFFFFF);
            // This makes sense. Yup.
            tabPane.addTab(new UITabBar.Tab((UIElement) ici, noIcons));
        }
        tabPane.handleIncoming();

        // Part marked 'S2'
        // This part is made up of colour swatches which don't care about size,
        // so they need to be given a size - imageEditorTextHeight * 2 should do it
        UISplitterLayout s2Layout = new UISplitterLayout(new UIColourSwatch(baseCol), currentColour, true, 0.5d) {
            @Override
            public void setWantedSize(Size size) {
                int v = FontSizes.imageEditorTextHeight * 2;
                super.setWantedSize(new Size(v, v));
            }
        };

        // Part marked 'a0'
        alphaBox = new UIChannelBox((baseCol >> 24) & 0xFF);
        alphaBox.onEdit = new Runnable() {
            @Override
            public void run() {
                setColour(null, currentColour.col);
            }
        };

        UISplitterLayout leftCoreLayout;
        if (!alpha) {
            leftCoreLayout = s2Layout;
        } else {
            // alphaBox is referred to elsewhere so it's kept around, but this isn't
            UISplitterLayout a0Layout = new UISplitterLayout(new UILabel("Alpha", FontSizes.imageEditorTextHeight), alphaBox, false, 0);
            leftCoreLayout = new UISplitterLayout(s2Layout, a0Layout, true, 1d);
        }

        // left/right layouts are stuff above ok/cancel buttons
        UISplitterLayout leftLayout = new UISplitterLayout(leftCoreLayout, new UITextButton(TXDB.get("Ok"), FontSizes.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                if (!shuttingDown) {
                    shuttingDown = true;
                    iConsumer.accept(currentColour.col);
                }
            }
        }), true, 1);
        UISplitterLayout rightLayout = new UISplitterLayout(tabPane, new UITextButton(TXDB.get("Cancel"), FontSizes.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                if (!shuttingDown) {
                    shuttingDown = true;
                    iConsumer.accept(null);
                }
            }
        }), true, 1);
        UISplitterLayout mainLayout = new UISplitterLayout(leftLayout, rightLayout, false, 0d);
        proxySetElement(mainLayout, true);
    }

    @Override
    public boolean requestsUnparenting() {
        return shuttingDown;
    }

    @Override
    public String toString() {
        return wTitle;
    }

    private void setColour(IConsumer<Integer> onBehalfOf, int argb) {
        argb &= 0xFFFFFF;
        currentColour.col = argb | (((int) alphaBox.number & 0xFF) << 24);
        for (IConsumer<Integer> cl : colourListeners)
            if (cl != onBehalfOf)
                cl.accept(argb & 0xFFFFFF);
    }

    // -- Channel Utility Class --

    private class UIChannelBox extends UINumberBox {
        public UIChannelBox(long number) {
            super(number, FontSizes.imageEditorTextHeight);
        }

        @Override
        public void setWantedSize(Size size) {
            if ((size.width < numberBoxMinimumSize.width) || (size.height < numberBoxMinimumSize.height)) {
                super.setWantedSize(new Size(Math.max(size.width, numberBoxMinimumSize.width), Math.max(size.height, numberBoxMinimumSize.height)));
                return;
            }
            super.setWantedSize(size);
        }
    }

    private static class UIChannelLabel extends UILabel {
        public UIChannelLabel(String txt) {
            super(txt, FontSizes.imageEditorTextHeight);
        }

        @Override
        public void setWantedSize(Size size) {
            super.setWantedSize(new Size(size.height, size.height));
        }
    }

    private static class UIPickCoordinator extends UIElement {
        public IImage baseImage;
        private final int baseW, baseH, targetScale;
        public Size targetSize;
        private final IConsumer<Size> resultConsumer;

        public UIPickCoordinator(int bw, int bh, int sc, IConsumer<Size> setter) {
            super(bw * sc, bh * sc);
            targetScale = sc;
            baseW = bw;
            baseH = bh;
            resultConsumer = setter;
        }

        public Rect determineInteriorPosition() {
            return UIThumbnail.getDrawRect(getSize(), baseW, baseH);
        }

        @Override
        public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

        }

        @Override
        public void render(IGrDriver igd) {
            Rect intPos = determineInteriorPosition();
            int bw = baseImage.getWidth();
            int bh = baseImage.getHeight();
            igd.blitScaledImage(0, 0, bw, bh, intPos.x, intPos.y, intPos.width, intPos.height, baseImage);
            int tsx = intPos.x + ((targetSize.width * intPos.width) / baseW);
            int tsy = intPos.y + ((targetSize.height * intPos.height) / baseH);
            // *cough* ignore this please
            if (bh == 1)
                tsy = intPos.y + (intPos.height / 2);

            igd.clearRect(0, 0, 0, intPos.x, tsy - targetScale, intPos.width, targetScale);
            igd.clearRect(0, 0, 0, intPos.x, tsy + targetScale, intPos.width, targetScale);

            igd.clearRect(0, 0, 0, tsx - targetScale, intPos.y, targetScale, intPos.height);
            igd.clearRect(0, 0, 0, tsx + targetScale, intPos.y, targetScale, intPos.height);

            if (bh == 1) {
                FontManager.drawString(igd, 0, 0, Integer.toString(targetSize.width), false, false, FontSizes.tonePickerTextHeight);
            } else {
                FontManager.drawString(igd, 0, 0, targetSize.width + "," + targetSize.height, false, false, FontSizes.tonePickerTextHeight);
            }
        }

        @Override
        public IPointerReceiver handleNewPointer(IPointer state) {
            return new IPointerReceiver() {
                @Override
                public void handlePointerBegin(IPointer state) {
                    handlePointerUpdate(state);
                }

                @Override
                public void handlePointerEnd(IPointer state) {
                    handlePointerUpdate(state);
                }

                @Override
                public void handlePointerUpdate(IPointer state) {
                    Rect intPos = determineInteriorPosition();
                    if (intPos.width == 0)
                        return;
                    if (intPos.height == 0)
                        return;
                    int rx = ((state.getX() - intPos.x) * baseW) / intPos.width;
                    int ry = ((state.getY() - intPos.y) * baseH) / intPos.height;
                    if (rx < 0)
                        rx = 0;
                    if (ry < 0)
                        ry = 0;
                    if (rx >= baseImage.getWidth())
                        rx = baseImage.getWidth() - 1;
                    if (ry >= baseImage.getHeight())
                        ry = baseImage.getHeight() - 1;
                    targetSize = new Size(rx, ry);
                    resultConsumer.accept(targetSize);
                }

            };
        }
    }

    // -- ColourViews --

    private class UIRGBColourView extends UIProxy implements IConsumer<Integer> {
        private UIChannelBox rA, gA, bA;
        private UIScrollbar rB, gB, bB;
        private int lastKnownBVal = 0;

        private UIRGBColourView() {
            // R[0]<->
            // G[0]<->
            // B[0]<->

            Runnable onAEdit = new Runnable() {
                @Override
                public void run() {
                    // This also implicitly clamps the numbers
                    updateBFromA();
                    lastKnownBVal = getBColour();
                    setColour(UIRGBColourView.this, lastKnownBVal);
                }
            };

            rA = new UIChannelBox(0);
            rA.onEdit = onAEdit;
            gA = new UIChannelBox(0);
            gA.onEdit = onAEdit;
            bA = new UIChannelBox(0);
            bA.onEdit = onAEdit;

            rB = new UIScrollbar(false, FontSizes.generalScrollersize);
            gB = new UIScrollbar(false, FontSizes.generalScrollersize);
            bB = new UIScrollbar(false, FontSizes.generalScrollersize);

            UISplitterLayout rC = new UISplitterLayout(rA, rB, false, 0);
            UISplitterLayout gC = new UISplitterLayout(gA, gB, false, 0);
            UISplitterLayout bC = new UISplitterLayout(bA, bB, false, 0);

            UISplitterLayout r = new UISplitterLayout(new UIChannelLabel("R"), rC, false, 0);
            UISplitterLayout g = new UISplitterLayout(new UIChannelLabel("G"), gC, false, 0);
            UISplitterLayout b = new UISplitterLayout(new UIChannelLabel("B"), bC, false, 0);

            UISplitterLayout rgSplit = new UISplitterLayout(r, g, true, 0);
            UISplitterLayout b0Split = new UISplitterLayout(b, new UILabel(TXDB.get("(NOTE: HSV may be better if precision isn't an issue.)"), FontSizes.imageEditorTextHeight), true, 0);
            UISplitterLayout rbSplit = new UISplitterLayout(rgSplit, b0Split, true, 0);
            proxySetElement(rbSplit, true);
        }

        // NOTE: Must work properly, be reversible, etc. for this whole thing to NOT explode!
        private int getBColour() {
            int r = crChannel(rB.scrollPoint);
            int g = crChannel(gB.scrollPoint);
            int b = crChannel(bB.scrollPoint);
            return (r << 16) | (g << 8) | b;
        }

        private int crChannel(double r) {
            int round = (int) Math.round(r * 255);
            if (round < 0)
                return 0;
            if (round > 255)
                return 255;
            return round;
        }

        @Override
        public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
            super.update(deltaTime, selected, peripherals);
            int bCol = getBColour();
            if (bCol != lastKnownBVal) {
                lastKnownBVal = bCol;
                // Scrollbars adjusted!
                updateA(bCol);
                setColour(this, bCol);
            }
        }

        @Override
        public void accept(Integer integer) {
            updateA(integer);
            lastKnownBVal = integer;
            updateBFromA();
        }

        private void updateA(int x) {
            rA.number = (x & 0xFF0000) >> 16;
            gA.number = (x & 0xFF00) >> 8;
            bA.number = (x & 0xFF);
        }

        private void updateBFromA() {
            rB.scrollPoint = Math.min(255, Math.max(0, rA.number / 255.0d));
            gB.scrollPoint = Math.min(255, Math.max(0, gA.number / 255.0d));
            bB.scrollPoint = Math.min(255, Math.max(0, bA.number / 255.0d));
        }

        @Override
        public String toString() {
            return "RGB";
        }
    }

    private class UIHSVColourView extends UIProxy implements IConsumer<Integer> {
        public final UIPickCoordinator svCoordinator, hCoordinator;

        public UIHSVColourView(int sc) {
            svCoordinator = new UIPickCoordinator(256, 256, sc, new IConsumer<Size>() {
                @Override
                public void accept(Size size) {
                    performSendStuff();
                }
            });
            hCoordinator = new UIPickCoordinator(256, 16, sc, new IConsumer<Size>() {
                @Override
                public void accept(Size size) {
                    svCoordinator.baseImage = Art.getColourPal(Art.getRainbowHue(size.width));
                    performSendStuff();
                }
            });
            hCoordinator.baseImage = Art.getRainbow();
            proxySetElement(new UISplitterLayout(svCoordinator, hCoordinator, true, 1), true);
        }

        private void performSendStuff() {
            int[] array = svCoordinator.baseImage.getPixels();
            int col = array[svCoordinator.targetSize.width + (svCoordinator.targetSize.height * 256)];
            col &= 0xFFFFFF;
            setColour(this, col);
            // Use to determine distortions
            // accept(col);
        }

        @Override
        public void accept(Integer i) {
            // Attempt to figure out HSV values...
            // NOTE: It seems this calculates SV incorrectly... *sigh*
            // you'll have to deal with it for now, colourspaces are weird
            int r = (i & 0xFF0000) >> 16;
            int g = (i & 0xFF00) >> 8;
            int b = i & 0xFF;

            int v = Math.max(Math.max(r, g), b);
            int mn = Math.min(Math.min(r, g), b);
            int s = v - mn;
            // During testing of this alg it seemed that S was somehow implicitly multiplied by V
            if (v != 0) {
                s *= 255;
                s /= v;
            }
            // -

            // To work out hue, start by eliminating a lack of saturation/value as a factor
            r -= mn;
            g -= mn;
            b -= mn;

            int h = 0;
            if (s != 0) {
                // In 1.0space, R /= S
                r *= (255 * 255) / s;
                g *= (255 * 255) / s;
                b *= (255 * 255) / s;
                r /= 255;
                g /= 255;
                b /= 255;
                int hScore = 0x7FFFFFFF;
                // Closest match wins.
                int[] comparisons = Art.getRainbow().getPixels();
                for (int j = 0; j < comparisons.length; j++) {
                    int score = 0;
                    int t = comparisons[j];
                    int tr = (t & 0xFF0000) >> 16;
                    int tg = (t & 0xFF00) >> 8;
                    int tb = t & 0xFF;
                    score += Math.abs(r - tr);
                    score += Math.abs(g - tg);
                    score += Math.abs(b - tb);
                    if (hScore > score) {
                        hScore = score;
                        h = j;
                    }
                }
            }

            svCoordinator.baseImage = Art.getColourPal(Art.getRainbowHue(h));
            svCoordinator.targetSize = new Size(s, 255 - v);
            hCoordinator.targetSize = new Size(h, 0);
        }

        @Override
        public String toString() {
            return "HSV";
        }
    }
}

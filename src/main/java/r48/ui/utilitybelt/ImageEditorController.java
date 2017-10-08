/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui.utilitybelt;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.FontSizes;
import r48.ui.UIAppendButton;

import java.awt.*;
import java.util.LinkedList;

/**
 * Oh, this can't be good news.
 * - 7th October, 2017
 */
public class ImageEditorController {
    public UISplitterLayout rootView;
    private UIImageEditView imageEditView;
    private UIScrollLayout paletteView;
    public LinkedList<Integer> palette = new LinkedList<Integer>();
    public int selPaletteIndex = 0;
    public ImageEditorController() {
        palette.add(0xFF000000);
        palette.add(0xFF0000FF);
        palette.add(0xFF00FF00);
        palette.add(0xFF00FFFF);
        palette.add(0xFFFF0000);
        palette.add(0xFFFF00FF);
        palette.add(0xFFFFFF00);
        palette.add(0xFFFFFFFF);
        imageEditView = new UIImageEditView();
        paletteView = new UIScrollLayout(true, FontSizes.generalScrollersize);
        paletteView.setBounds(new Rect(0, 0, initPalette(), 1));
        rootView = new UISplitterLayout(imageEditView, paletteView, false, 1.0d);
    }

    private int initPalette() {
        paletteView.panels.clear();
        // NEEDS A/S/L PANEL
        int idx = 0;
        for (final Integer col : palette) {
            final int fidx = idx;
            final int a = ((col & 0xFF000000) >> 24) & 0xFF;
            final int r = (col & 0xFF0000) >> 16;
            final int g = (col & 0xFF00) >> 8;
            final int b = (col & 0xFF);
            UIElement cPanel = new UIElement() {
                @Override
                public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
                    Rect bounds = getBounds();
                    igd.clearRect(r, g, b, ox, oy, bounds.width / 2, bounds.height);
                    igd.clearRect(a, a, a, ox + (bounds.width / 2), oy, bounds.width - (bounds.width / 2), bounds.height);
                }
            };
            if (selPaletteIndex == fidx) {
                cPanel = new UIAppendButton("X", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        if (palette.size() > 1) {
                            selPaletteIndex--;
                            palette.remove(fidx);
                            initPalette();
                        }
                    }
                }, FontSizes.schemaButtonTextHeight);
            } else {
                cPanel = new UIAppendButton("~", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        int thi = palette.get(fidx);
                        int tha = palette.get(selPaletteIndex);
                        palette.set(selPaletteIndex, thi);
                        palette.set(fidx, tha);
                        initPalette();
                    }
                }, FontSizes.schemaButtonTextHeight);
            }
            cPanel = new UISplitterLayout(new UITextButton(FontSizes.schemaButtonTextHeight, "<", new Runnable() {
                @Override
                public void run() {
                    imageEditView.image[imageEditView.cursorX + (imageEditView.cursorY * imageEditView.imageW)] = col;
                }
            }), cPanel, false, 0.0d);
            paletteView.panels.add(new UIAppendButton("O", cPanel, new Runnable() {
                @Override
                public void run() {
                    selPaletteIndex = fidx;
                    initPalette();
                }
            }, FontSizes.schemaButtonTextHeight));
            idx++;
        }
        paletteView.setBounds(paletteView.getBounds());
        return FontSizes.scaleGuess(80);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.dmicg;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.IPeripherals;
import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.imagefx.MultiplyImageEffect;
import r48.io.BMPConnection;

import java.io.IOException;

/**
 * Created on December 16, 2018.
 */
public class UICharGenView extends UIElement.UIPanel {
    public String mode, text;
    private final CharacterGeneratorController ctrl;
    private UITextButton exportButton;
    private int w, h;

    public UICharGenView(String t, final int w, final int h, CharacterGeneratorController control) {
        this.w = w;
        this.h = h;
        mode = text = t;
        ctrl = control;
        exportButton = new UITextButton(TXDB.get("Copy to R48 Clipboard"), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                // Job of this is to composite results.
                IGrDriver img = GaBIEn.makeOffscreenBuffer(w, h, true);
                render(img);
                int[] tx = img.getPixels();
                int idx = 0;
                byte[] buffer = BMPConnection.prepareBMP(w, h, 32, 0, true, false);
                BMPConnection bc;
                try {
                    bc = new BMPConnection(buffer, BMPConnection.CMode.Normal, 0, false);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
                for (int j = 0; j < h; j++)
                    for (int i = 0; i < w; i++)
                        bc.putPixel(i, j, tx[idx++]);
                AppMain.theClipboard = new RubyIO().setUser("Image", buffer);
            }
        });
        layoutAddElement(exportButton);
        setWantedSize(new Size(w, h + exportButton.getWantedSize().height));
        forceToRecommended();
    }

    @Override
    public String toString() {
        return text + " (" + w + "x" + h + ")";
    }

    @Override
    public void runLayout() {
        Size mySize = getSize();
        Size ebWS = exportButton.getWantedSize();
        exportButton.setForcedBounds(this, new Rect(0, mySize.height - ebWS.height, mySize.width, ebWS.height));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
    }

    @Override
    public void render(IGrDriver igd) {
        super.render(igd);
        for (CharacterGeneratorController.LayerImage li : ctrl.charLay) {
            if (mode.equals(li.mode)) {
                CharacterGeneratorController.Layer l = ctrl.charCfg.get(li.layerId);
                if (l.enabled) {
                    int a = (l.swatch.col >> 24) & 0xFF;
                    int r = (l.swatch.col >> 16) & 0xFF;
                    int g = (l.swatch.col >> 8) & 0xFF;
                    int b = l.swatch.col & 0xFF;

                    IImage im = GaBIEn.getImage(li.img);
                    im = AppMain.imageFXCache.process(im, new MultiplyImageEffect(a, r, g, b));
                    igd.blitImage(0, 0, im.getWidth(), im.getHeight(), 0, 0, im);
                }
            }
        }
    }
}

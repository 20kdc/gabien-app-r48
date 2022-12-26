/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.dmicg;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.IPeripherals;
import gabien.ui.IPointer;
import gabien.ui.IPointerReceiver;
import gabien.ui.Size;
import r48.AppMain;
import r48.imagefx.MultiplyImageEffect;
import r48.ui.UIPlaneView;

/**
 * Created on December 16, 2018.
 */
public class UICharGenView extends UIPlaneView {
    public String mode, text;
    private final CharacterGeneratorController ctrl;
    public final int genWidth, genHeight;
    private boolean dragLock = true;

    public UICharGenView(String t, final int w, final int h, CharacterGeneratorController control) {
        this.genWidth = w;
        this.genHeight = h;
        mode = text = t;
        ctrl = control;
        setWantedSize(new Size(w, h));
        forceToRecommended();
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

    }

    @Override
    public void render(IGrDriver igd) {
        Size mySize = getSize();
        int ew = (int) planeMulZoom(genWidth);
        int eh = (int) planeMulZoom(genHeight);
        int ex = ((mySize.width - ew) / 2) - ((int) planeMulZoom(camX));
        int ey = ((mySize.height - eh) / 2) - ((int) planeMulZoom(camY));

        int[] st = igd.getLocalST();
        st[0] += ex;
        st[1] += ey;
        igd.updateST();
        render(igd, ew, eh);
        st[0] -= ex;
        st[1] -= ey;
        igd.updateST();

        super.render(igd);
    }

    @Override
    public String toString() {
        return text + " (" + genWidth + "x" + genHeight + ")";
    }

    public void render(IGrDriver igd, int w, int h) {
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
                    int imw = im.getWidth();
                    int imh = im.getHeight();
                    int smw = (imw * w) / genWidth;
                    int smh = (imh * h) / genHeight;
                    igd.blitScaledImage(0, 0, imw, imh, 0, 0, smw, smh, im);
                }
            }
        }
    }

    @Override
    protected String planeGetStatus() {
        return null;
    }

    @Override
    protected boolean planeGetDragLock() {
        return dragLock;
    }

    @Override
    protected void planeToggleDragLock() {
        dragLock = !dragLock;
    }

    @Override
    protected IPointerReceiver planeHandleDrawPointer(IPointer state) {
        camX = 0;
        camY = 0;
        return null;
    }
}

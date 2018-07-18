/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Created on 14th July 2018
 */
public class CopyImageEditorTool implements IImageEditorTool {
    public int stage;
    public int aX, aY;
    public int bW, bH;
    public boolean flipX, flipY, swapXY;

    @Override
    public void enter(UIImageEditView uiev) {

    }

    @Override
    public void apply(UIImageEditView.ImPoint imp, UIImageEditView view, boolean major, boolean dragging) {
        if (dragging)
            return;
        if (!major)
            return;
        if (stage == 0) {
            aX = imp.x;
            aY = imp.y;
            stage++;
        } else if (stage == 1) {
            bW = (Math.max(aX, imp.x) + 1) - Math.min(aX, imp.x);
            bH = (Math.max(aY, imp.y) + 1) - Math.min(aY, imp.y);
            aX = Math.min(aX, imp.x);
            aY = Math.min(aY, imp.y);
            stage++;
        } else {
            view.eds.startSection();
            UIImageEditView.ImPoint src = new UIImageEditView.ImPoint(0, 0);
            int[] cols = new int[bW * bH];
            for (int i = 0; i < bW; i++) {
                src.x = aX + i;
                for (int j = 0; j < bH; j++) {
                    src.y = aY + j;
                    src.updateCorrected(view);
                    cols[i + (j * bW)] = view.image.getRaw(src.correctedX, src.correctedY);
                }
            }
            UIImageEditView.ImPoint dst = new UIImageEditView.ImPoint(0, 0);
            for (int i = 0; i < bW; i++) {
                for (int j = 0; j < bH; j++) {
                    transform(dst, imp, i, j);
                    dst.updateCorrected(view);
                    view.image.setRaw(dst.correctedX, dst.correctedY, cols[i + (j * bW)]);
                }
            }
            stage = 0;
            view.eds.endSection();
        }
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    private void transform(UIImageEditView.ImPoint dst, UIImageEditView.ImPoint imp, int i, int j) {
        if (flipX)
            i = bW - (1 + i);
        if (flipY)
            j = bH - (1 + j);
        if (swapXY) {
            int k = i;
            i = j;
            j = k;
        }
        dst.x = imp.x + i;
        dst.y = imp.y + j;
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        UIScrollLayout uie = RootImageEditorTool.createToolPalette(uiev, CopyImageEditorTool.class);
        UITextButton a = new UITextButton(TXDB.get("FlipX"), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                flipX = !flipX;
            }
        }).togglable(flipX);
        UITextButton b = new UITextButton(TXDB.get("FlipY"), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                flipY = !flipY;
            }
        }).togglable(flipY);
        UITextButton c = new UITextButton(TXDB.get("SwapXY"), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                swapXY = !swapXY;
            }
        }).togglable(swapXY);
        UISplitterLayout sl = new UISplitterLayout(a, b, false, 0.5d);
        uie.panelsAdd(new UISplitterLayout(sl, c, false, 0.6666d));
        return uie;
    }

    @Override
    public Rect getSelection() {
        if (stage == 1)
            return new Rect(aX, aY, 0, 0);
        if (stage == 2)
            return new Rect(aX, aY, bW, bH);
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (stage == 0) {
            return TXDB.get("Tap top-left pixel of area to copy.");
        } else if (stage == 1) {
            return TXDB.get("Tap bottom-right pixel of area to copy.");
        } else {
            return TXDB.get("Tap top-left pixel of destination.");
        }
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }

}

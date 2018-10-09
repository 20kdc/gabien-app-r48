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
public class CopyImageEditorTool extends StagedImageEditorTool {
    public boolean flipX, flipY, swapXY;

    public CopyImageEditorTool() {
        super(3);
    }

    @Override
    protected void performOperation(UIImageEditView view) {
        view.eds.startSection();
        UIImageEditView.ImPoint src = new UIImageEditView.ImPoint(0, 0);

        Rect bStuff = getRectWithPoints();
        int targetX = stageXs[2];
        int targetY = stageYs[2];

        int[] cols = new int[bStuff.width * bStuff.height];
        for (int i = 0; i < bStuff.width; i++) {
            src.x = bStuff.x + i;
            for (int j = 0; j < bStuff.height; j++) {
                src.y = bStuff.y + j;
                src.updateCorrected(view);
                cols[i + (j * bStuff.width)] = view.image.getRaw(src.correctedX, src.correctedY);
            }
        }
        UIImageEditView.ImPoint dst = new UIImageEditView.ImPoint(0, 0);
        for (int i = 0; i < bStuff.width; i++) {
            for (int j = 0; j < bStuff.height; j++) {
                int i2 = i, j2 = j;
                if (flipX)
                    i2 = bStuff.width - (1 + i);
                if (flipY)
                    j2 = bStuff.height - (1 + j);
                if (swapXY) {
                    int k = i2;
                    i2 = j;
                    j2 = k;
                }
                dst.x = targetX + i2;
                dst.y = targetY + j2;
                dst.updateCorrected(view);
                view.image.setRaw(dst.correctedX, dst.correctedY, cols[i + (j * bStuff.width)]);
            }
        }
        stage = 0;
        view.eds.endSection();
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
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (stage == 0) {
            return TXDB.get("Tap top-left pixel of area to copy.");
        } else if (stage == 1) {
            return TXDB.get("Tap bottom-right pixel of area to copy.");
        } else {
            return TXDB.get("Tap top-left pixel of destination.");
        }
    }

}

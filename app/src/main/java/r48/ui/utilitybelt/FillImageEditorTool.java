/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Created on October 09, 2018.
 */
public class FillImageEditorTool implements IImageEditorTool {
    public boolean autoshade, autoshadeLRX, autoshadeUDX;

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, final UIImageEditView view, boolean major, boolean dragging) {
        if ((!major) || dragging)
            return;
        view.eds.startSection();

        // NOTE! THIS DOESN'T USE CORRECTPOINT PROPERLY.
        // Need to adapt the fill-algorithm to make a very clear distinction between pre-transform and post-transform points while avoiding an infinite loop.

        FillAlgorithm.Point start = view.correctPoint(x, y);
        if (start == null)
            return;
        final int spi = view.image.getRaw(start.x, start.y);

        FillAlgorithm fa = new FillAlgorithm(new IFunction<FillAlgorithm.Point, FillAlgorithm.Point>() {
            @Override
            public FillAlgorithm.Point apply(FillAlgorithm.Point point) {
                return view.correctPoint(point.x, point.y);
            }
        }, new IFunction<FillAlgorithm.Point, Boolean>() {
            @Override
            public Boolean apply(FillAlgorithm.Point point) {
                return view.image.getRaw(point.x, point.y) == spi;
            }
        });
        fa.availablePointSet.add(start);
        while (!fa.availablePointSet.isEmpty())
            fa.pass();
        int shA = Math.max(view.selPaletteIndex - 1, 0);
        int shB = view.selPaletteIndex;
        int shC = Math.min(view.selPaletteIndex + 1, view.image.paletteSize() - 1);
        for (FillAlgorithm.Point p : fa.executedPointSet) {
            boolean above = !fa.executedPointSet.contains(tileAS(view, p.offset(0, -1)));
            boolean below = !fa.executedPointSet.contains(tileAS(view, p.offset(0, 1)));
            boolean left = !fa.executedPointSet.contains(tileAS(view, p.offset(-1, 0)));
            boolean right = !fa.executedPointSet.contains(tileAS(view, p.offset(1, 0)));

            if (above && (!below)) {
                if (autoshade) {
                    view.image.setPixel(p.x, p.y, autoshadeUDX ? shC : shA);
                } else {
                    view.image.setPixel(p.x, p.y, shB);
                }
            } else if ((!above) && below) {
                if (autoshade) {
                    view.image.setPixel(p.x, p.y, autoshadeUDX ? shA : shC);
                } else {
                    view.image.setPixel(p.x, p.y, shB);
                }
            } else if (left && (!right)) {
                if (autoshade) {
                    view.image.setPixel(p.x, p.y, autoshadeLRX ? shC : shA);
                } else {
                    view.image.setPixel(p.x, p.y, shB);
                }
            } else if ((!left) && right) {
                if (autoshade) {
                    view.image.setPixel(p.x, p.y, autoshadeLRX ? shA : shC);
                } else {
                    view.image.setPixel(p.x, p.y, shB);
                }
            } else {
                view.image.setPixel(p.x, p.y, shB);
            }
        }
        view.eds.endSection();
    }

    private FillAlgorithm.Point tileAS(UIImageEditView view, FillAlgorithm.Point point) {
        return view.correctPoint(point.x, point.y);
    }

    @Override
    public void endApply(UIImageEditView view) {

    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        UIElement uie = RootImageEditorTool.createToolPalette(uiev, FillImageEditorTool.class);
        UIScrollLayout usl = new UIScrollLayout(false, FontSizes.mapToolbarScrollersize);
        usl.panelsAdd(new UITextButton(TXDB.get("Autoshade"), FontSizes.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                autoshade = !autoshade;
            }
        }).togglable(autoshade));
        usl.panelsAdd(new UITextButton(TXDB.get("LR"), FontSizes.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                autoshadeLRX = !autoshadeLRX;
            }
        }).togglable(autoshadeLRX));
        usl.panelsAdd(new UITextButton(TXDB.get("UD"), FontSizes.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                autoshadeUDX = !autoshadeUDX;
            }
        }).togglable(autoshadeUDX));
        return new UISplitterLayout(uie, usl, true, 1.0);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return TXDB.get("Press to fill area.");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }
}

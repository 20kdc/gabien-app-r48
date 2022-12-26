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
import r48.ui.UISymbolButton;
import r48.ui.Art.Symbol;

/**
 * Created on 13th July 2018.
 */
public class RootImageEditorTool implements IImageEditorTool {
    private boolean activeSection = false;

    public static UIScrollLayout createToolPalette(final UIImageEditView uiev, Class<?> oneTool) {
        @SuppressWarnings("rawtypes")
        final Class[] toolClasses = new Class[] {
                RootImageEditorTool.class,
                RectangleImageEditorTool.class,
                LineImageEditorTool.class,
                FillImageEditorTool.class,
                CopyImageEditorTool.class,
                PasteImageEditorTool.class,
                TileImageEditorTool.class,
                EDImageEditorTool.class
        };
        Symbol[] toolSymbol = new Symbol[] {
                Symbol.Pencil,
                Symbol.Rectangle,
                Symbol.Line,
                Symbol.Fill,
                Symbol.CopyRectangle,
                Symbol.PasteRectangle,
                Symbol.Area,
                Symbol.Eyedropper
        };
        UIScrollLayout svl = new UIScrollLayout(true, FontSizes.mapToolbarScrollersize);
        UIElement left = null;
        for (int i = 0; i < toolClasses.length; i++) {
            final int ic = i;
            UIElement nx = new UISymbolButton(toolSymbol[i], FontSizes.schemaFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    try {
                        uiev.currentTool = (IImageEditorTool) toolClasses[ic].newInstance();
                        uiev.newToolCallback.run();
                    } catch (InstantiationException e) {
                    } catch (IllegalAccessException e) {
                    }
                }
            }).togglable(toolClasses[i] == oneTool);
            if (left == null) {
                left = nx;
            } else {
                svl.panelsAdd(new UISplitterLayout(left, nx, false, 0.5d));
                left = null;
            }
        }
        if (left != null)
            svl.panelsAdd(left);
        return svl;
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return createToolPalette(uiev, RootImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        if (dedicatedDragControl)
            return TXDB.get("Tap/drag: Draw, Camera button: Pan");
        return TXDB.get("LMB: Draw, Shift-LMB: Grab Colour, Other: Pan");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {

    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {
        if (!activeSection) {
            activeSection = true;
            view.eds.startSection();
        }
        FillAlgorithm.Point p = view.correctPoint(x, y);
        if (p == null)
            return;
        view.image.setPixel(p.x, p.y, view.selPaletteIndex);
    }

    @Override
    public void endApply(UIImageEditView view) {
        view.eds.endSection();
        activeSection = false;
    }
}

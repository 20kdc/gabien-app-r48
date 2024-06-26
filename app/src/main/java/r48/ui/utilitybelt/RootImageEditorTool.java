/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import java.util.LinkedList;

import gabien.ui.*;
import gabien.ui.elements.UIIconButton;
import gabien.ui.layouts.UIListLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;
import r48.App;
import r48.ui.Art.Symbol;

/**
 * Created on 13th July 2018.
 */
public class RootImageEditorTool extends ImageEditorTool {
    private boolean activeSection = false;

    public RootImageEditorTool(App app) {
        super(app);
    }

    public static UIElement createToolPalette(final UIImageEditView uiev, Class<?> oneTool) {
        App app = uiev.app;
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
        LinkedList<UIElement> svl = new LinkedList<>();
        UIElement left = null;
        for (int i = 0; i < toolClasses.length; i++) {
            final int ic = i;
            @SuppressWarnings("unchecked")
            UIElement nx = new UIIconButton(toolSymbol[i].i(app), app.f.schemaFieldTH, () -> {
                try {
                    uiev.currentTool = (ImageEditorTool) (toolClasses[ic].getConstructor(App.class).newInstance(app));
                    uiev.newToolCallback.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).togglable(toolClasses[i] == oneTool);
            if (left == null) {
                left = nx;
            } else {
                svl.add(new UISplitterLayout(left, nx, false, 0.5d));
                left = null;
            }
        }
        if (left != null)
            svl.add(left);
        return new UIListLayout(true, svl);
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
            return T.ie.hint;
        return T.ie.hintDesktop;
    }

    @Override
    public ImageEditorTool getCamModeLT() {
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
        view.newToolCallback.run();
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabienapp.Application;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.ui.Art;
import r48.ui.UISymbolButton;

/**
 * Created on 13th July 2018.
 */
public class RootImageEditorTool implements IImageEditorTool {
    public static UIElement createToolPalette(final UIImageEditView uiev, Class oneTool) {
        final Class[] toolClasses = new Class[] {
                RootImageEditorTool.class,
                NopImageEditorTool.class,
                NopImageEditorTool.class
        };
        Art.Symbol[] toolSymbol = new Art.Symbol[] {
                Art.Symbol.Pencil,
                Art.Symbol.Rectangle,
                Art.Symbol.Copy,
        };
        UIScrollLayout svl = new UIScrollLayout(false, FontSizes.mapToolbarScrollersize);
        for (int i = 0; i < toolClasses.length; i++) {
            final int ic = i;
            svl.panelsAdd(new UISymbolButton(toolSymbol[i], FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    try {
                        uiev.currentTool = (IImageEditorTool) toolClasses[ic].newInstance();
                        uiev.updatePalette.run();
                    } catch (InstantiationException e) {
                    } catch (IllegalAccessException e) {
                    }
                }
            }).togglable(toolClasses[i].isAssignableFrom(oneTool)));
        }
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
        String info = TXDB.get("LMB: Draw/place, others: scroll, camera button: scroll mode");
        if (Application.mobileExtremelySpecialBehavior)
            info = TXDB.get("Tap/Drag: Draw, camera button: Switch to scrolling");
        if (Application.mobileExtremelySpecialBehavior)
            return TXDB.get("Tap: Position cursor, Drag: Scroll");
        return TXDB.get("All mouse buttons position cursor & scroll");
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }

    @Override
    public void accept(UIImageEditView uiImageEditView) {
        uiImageEditView.image.setPixel(uiImageEditView.cursorX, uiImageEditView.cursorY, uiImageEditView.selPaletteIndex);
    }
}

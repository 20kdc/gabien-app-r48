/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.ui.UIAutoclosingPopupMenu;
import gabien.ui.UINumberBox;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.UIMapView;

/**
 * Created on 18/06/17.
 */
public class UIMTPopupButtons extends UIMTBase {
    public UIMTPopupButtons(final IMapToolContext mtc, final boolean disableResize) {
        super(mtc, true);
        final UIMapView view = mtc.getMapView();
        UIAutoclosingPopupMenu u = new UIAutoclosingPopupMenu(new String[] {
                TXDB.get("Reload Panorama/TS"),
                TXDB.get("Properties"),
                TXDB.get("Resize"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.stuffRendererIndependent.imageLoader.flushCache();
                        view.mapTable.renderer.imageLoader.flushCache();
                        view.mapTable = view.map.rendererRetriever.get();
                        view.mapTable.renderer.imageLoader.flushCache();
                        view.reinitLayerVis();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.launchSchema(view.map.objectSchema, view.map.object, view);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        if (disableResize) {
                            AppMain.launchDialog(TXDB.get("Tiles are apparently readonly, so resizing is not possible."));
                        } else {
                            mtc.accept(new UIMTMapResizer(mtc));
                        }
                    }
                }
        }, FontSizes.dialogWindowTextHeight, FontSizes.menuScrollersize, true);
        changeInner(u);
    }

    private static class UIMTMapResizer extends UIMTBase {
        private UIMTMapResizer(final IMapToolContext mtc) {
            super(mtc, false);
            final UIMapView view = mtc.getMapView();
            final UINumberBox a = new UINumberBox(FontSizes.textDialogFieldTextHeight);
            final UINumberBox b = new UINumberBox(FontSizes.textDialogFieldTextHeight);
            a.number = view.mapTable.width;
            a.onEdit = new Runnable() {
                @Override
                public void run() {
                    if (a.number < 1)
                        a.number = 1;
                }
            };
            b.number = view.mapTable.height;
            b.onEdit = new Runnable() {
                @Override
                public void run() {
                    if (b.number < 1)
                        b.number = 1;
                }
            };
            UISplitterLayout root = new UISplitterLayout(new UISplitterLayout(a, b, false, 0.5d), new UITextButton(FontSizes.textDialogFieldTextHeight, "Resize", new Runnable() {
                @Override
                public void run() {
                    int w = a.number;
                    if (w < 1)
                        w = 1;
                    int h = b.number;
                    if (h < 1)
                        h = 1;
                    view.mapTable.resize(w, h);
                    view.passModificationNotification();
                }
            }), true, 0);
            changeInner(root);
        }
    }
}

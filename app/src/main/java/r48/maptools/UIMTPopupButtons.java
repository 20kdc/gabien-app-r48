/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.ui.*;
import gabien.ui.UIPopupMenu.Entry;
import gabienapp.Application;
import r48.AdHocSaveLoad;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.UIMapView;

import java.io.OutputStream;

/**
 * Created on 18/06/17.
 */
public class UIMTPopupButtons extends UIMTBase {
    public UIMTPopupButtons(final IMapToolContext mtc, final boolean disableResize, final ToolButton[] addendum) {
        super(mtc);

        final UIMapView view = mtc.getMapView();

        ToolButton[] mainToolButtons = {
                new ToolButton(TXDB.get("Reload Panorama/TS")) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        UIMapView.performFullCacheFlush(view.app, view);
                        return null;
                    }
                },
                new ToolButton(TXDB.get("Properties")) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        AppMain.launchSchema(view.map.objectSchema, view.map.object, view);
                        return null;
                    }
                },
                new ToolButton(TXDB.get("Resize")) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        if (disableResize) {
                            AppMain.launchDialog(TXDB.get("Tiles are apparently readonly, so resizing is not possible."));
                        } else {
                            return new UIMTMapResizer(mtc);
                        }
                        return null;
                    }
                },
                new ToolButton(TXDB.get("Export shot.png")) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        IGrDriver igd = GaBIEn.makeOffscreenBuffer(view.tileSize * view.mapTable.width, view.tileSize * view.mapTable.height, true);
                        view.mapTable.renderCore(igd, 0, 0, view.layerVis, view.currentLayer, view.debugToggle);
                        AdHocSaveLoad.prepare();
                        OutputStream os = GaBIEn.getOutFile(Application.BRAND + "/shot.png");
                        if (os != null) {
                            try {
                                os.write(igd.createPNG());
                                os.close();
                                AppMain.launchDialog(TXDB.get("Wrote 'shot.png' in R48 settings directory."));
                            } catch (Exception e) {
                                AppMain.launchDialog(TXDB.get("Failed for... ") + e);
                            }
                        } else {
                            AppMain.launchDialog(TXDB.get("Failed to open file."));
                        }
                        igd.shutdown();
                        return null;
                    }
                },
                new ToolButton(TXDB.get("Show/Hide Tile IDs")) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        view.debugToggle = !view.debugToggle;
                        return null;
                    }
                }
        };

        ToolButton[] allToolButtons = new ToolButton[mainToolButtons.length + addendum.length];
        System.arraycopy(mainToolButtons, 0, allToolButtons, 0, mainToolButtons.length);
        System.arraycopy(addendum, 0, allToolButtons, mainToolButtons.length, addendum.length);

        UIPopupMenu.Entry[] allEntries = new UIPopupMenu.Entry[allToolButtons.length];
        for (int i = 0; i < allEntries.length; i++) {
            final ToolButton tb = allToolButtons[i];
            allEntries[i] = new Entry(tb.text, new Runnable() {
                @Override
                public void run() {
                    UIMTBase ub = tb.apply(mtc);
                    if (ub != null)
                        mtc.accept(ub);
                }
            });
        }

        UIAutoclosingPopupMenu u = new UIAutoclosingPopupMenu(allEntries, FontSizes.dialogWindowTextHeight, FontSizes.menuScrollersize, true);
        changeInner(u, true);
    }

    private static class UIMTMapResizer extends UIMTBase {
        private UIMTMapResizer(final IMapToolContext mtc) {
            super(mtc);
            final UIMapView view = mtc.getMapView();
            final UINumberBox a = new UINumberBox(view.mapTable.width, FontSizes.textDialogFieldTextHeight);
            final UINumberBox b = new UINumberBox(view.mapTable.height, FontSizes.textDialogFieldTextHeight);
            a.onEdit = new Runnable() {
                @Override
                public void run() {
                    if (a.number < 1)
                        a.number = 1;
                }
            };
            b.onEdit = new Runnable() {
                @Override
                public void run() {
                    if (b.number < 1)
                        b.number = 1;
                }
            };
            UISplitterLayout root = new UISplitterLayout(new UISplitterLayout(a, b, false, 0.5d), new UITextButton("Resize", FontSizes.textDialogFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    int w = (int) a.number;
                    if (w < 1)
                        w = 1;
                    int h = (int) b.number;
                    if (h < 1)
                        h = 1;
                    view.mapTable.resize(w, h);
                    view.passModificationNotification();
                }
            }), true, 0);
            changeInner(root, true);
            setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(128), getSize().height));
        }
    }
}

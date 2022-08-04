/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.ui.*;
import gabien.uslx.append.IConsumer;
import gabienapp.Application;
import r48.AdHocSaveLoad;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.UIMapView;

import java.io.OutputStream;

/**
 * Created on 18/06/17.
 */
public class UIMTPopupButtons extends UIMTBase {
    public UIMTPopupButtons(final IMapToolContext mtc, final boolean disableResize, final String[] addendumNames, final IConsumer<IMapToolContext>[] addendumFuncs) {
        super(mtc);

        final UIMapView view = mtc.getMapView();

        String[] mainNames = new String[] {
                TXDB.get("Reload Panorama/TS"),
                TXDB.get("Properties"),
                TXDB.get("Resize"),
                TXDB.get("Export shot.png"),
                TXDB.get("Show/Hide Tile IDs")
        };
        Runnable[] mainRunnables = new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        UIMapView.performFullCacheFlush(view);
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
                },
                new Runnable() {
                    @Override
                    public void run() {
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
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        view.debugToggle = !view.debugToggle;
                    }
                }
        };

        String[] allNames = new String[mainNames.length + addendumNames.length];
        System.arraycopy(mainNames, 0, allNames, 0, mainNames.length);
        System.arraycopy(addendumNames, 0, allNames, mainNames.length, addendumNames.length);

        Runnable[] allRunnables = new Runnable[mainRunnables.length + addendumFuncs.length];
        System.arraycopy(mainRunnables, 0, allRunnables, 0, mainRunnables.length);
        for (int i = 0; i < addendumFuncs.length; i++) {
            final IConsumer<IMapToolContext> wrapped = addendumFuncs[i];
            allRunnables[i + mainRunnables.length] = new Runnable() {
                @Override
                public void run() {
                    wrapped.accept(mtc);
                }
            };
        }

        UIAutoclosingPopupMenu u = new UIAutoclosingPopupMenu(allNames, allRunnables, FontSizes.dialogWindowTextHeight, FontSizes.menuScrollersize, true);
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

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
import r48.App;
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
        final App app = view.app;

        ToolButton[] mainToolButtons = {
                new ToolButton(T.z.l13) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        UIMapView.performFullCacheFlush(view.app, view);
                        return null;
                    }
                },
                new ToolButton(T.z.l14) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        app.ui.launchSchema(view.map.objectSchema, view.map.object, view);
                        return null;
                    }
                },
                new ToolButton(T.z.l15) {
                    @Override
                    public UIMTBase apply(IMapToolContext a) {
                        if (disableResize) {
                            app.ui.launchDialog(T.z.l16);
                        } else {
                            return new UIMTMapResizer(mtc);
                        }
                        return null;
                    }
                },
                new ToolButton(T.z.l17) {
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
                                app.ui.launchDialog(T.z.l18);
                            } catch (Exception e) {
                                app.ui.launchDialog(T.z.l19 + e);
                            }
                        } else {
                            app.ui.launchDialog(T.z.l20);
                        }
                        igd.shutdown();
                        return null;
                    }
                },
                new ToolButton(T.z.l21) {
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

        UIAutoclosingPopupMenu u = new UIAutoclosingPopupMenu(allEntries, app.f.dialogWindowTH, app.f.menuS, true);
        changeInner(u, true);
    }

    private static class UIMTMapResizer extends UIMTBase {
        private UIMTMapResizer(final IMapToolContext mtc) {
            super(mtc);
            final UIMapView view = mtc.getMapView();
            final UINumberBox a = new UINumberBox(view.mapTable.width, app.f.textDialogFieldTH);
            final UINumberBox b = new UINumberBox(view.mapTable.height, app.f.textDialogFieldTH);
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
            UISplitterLayout root = new UISplitterLayout(new UISplitterLayout(a, b, false, 0.5d), new UITextButton("Resize", app.f.textDialogFieldTH, new Runnable() {
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
            setForcedBounds(null, new Rect(0, 0, app.f.scaleGuess(128), getSize().height));
        }
    }
}

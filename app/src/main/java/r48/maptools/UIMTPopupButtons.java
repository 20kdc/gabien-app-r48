/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.ui.dialogs.UIAutoclosingPopupMenu;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.dialogs.UIPopupMenu.Entry;
import gabien.ui.elements.UINumberBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.elements.UIThumbnail;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;
import gabienapp.Application;
import r48.AdHocSaveLoad;
import r48.App;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.UIMapView;
import r48.schema.util.SchemaDynamicContext;

import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Created on 18/06/17.
 */
public class UIMTPopupButtons extends UIMTBase {
    public UIMTPopupButtons(final IMapToolContext mtc, final boolean disableResize, final ToolButton[] addendum) {
        super(mtc);

        final UIMapView view = mtc.getMapView();
        final App app = view.app;

        final LinkedList<ToolButton> mainToolButtons = new LinkedList<>();
        mainToolButtons.add(new ToolButton(T.m.bReloadPanoramaTS) {
            @Override
            public UIMTBase apply(IMapToolContext a) {
                UIMapView.performFullCacheFlush(view.app, view);
                return null;
            }
        });
        mainToolButtons.add(new ToolButton(T.g.bProperties) {
            @Override
            public UIMTBase apply(IMapToolContext a) {
                app.ui.launchSchema(view.map.object, new SchemaDynamicContext(app, view));
                return null;
            }
        });
        if (!disableResize)
            mainToolButtons.add(new ToolButton(T.g.bResize) {
                @Override
                public UIMTBase apply(IMapToolContext a) {
                    return new UIMTMapResizer(mtc);
                }
            });
        mainToolButtons.add(new ToolButton(T.m.bExportShot) {
            @Override
            public UIMTBase apply(IMapToolContext a) {
                IGrDriver igd = GaBIEn.makeOffscreenBuffer(view.tileSize * view.mapTable.width, view.tileSize * view.mapTable.height);
                view.mapTable.renderCore(igd, 0, 0, view.layerVis, view.currentLayer, view.debugToggle);
                AdHocSaveLoad.prepare();
                OutputStream os = GaBIEn.getOutFile(Application.BRAND + "/shot.png");
                if (os != null) {
                    try {
                        os.write(igd.createPNG());
                        os.close();
                        app.ui.launchDialog(T.m.dlgWroteShot);
                    } catch (Exception e) {
                        app.ui.launchDialog(e);
                    }
                } else {
                    app.ui.launchDialog(T.m.dlgFailedToOpenFile);
                }
                igd.shutdown();
                return null;
            }
        });
        mainToolButtons.add(new ToolButton(T.m.tShowIDs) {
            @Override
            public UIMTBase apply(IMapToolContext a) {
                view.debugToggle = !view.debugToggle;
                return null;
            }
        });
        mainToolButtons.add(new ToolButton(T.m.bShowAtlases) {
            @Override
            public UIMTBase apply(IMapToolContext a) {
                LinkedList<IGrDriver> potential = a.getMapView().mapTable.renderer.tileRenderer.getAtlasSet();
                if (potential == null)
                    return null;
                for (IGrDriver ap : potential)
                    app.ui.wm.createWindow(new UIThumbnail(ap));
                return null;
            }
        });

        for (ToolButton t : addendum)
            mainToolButtons.add(t);

        ToolButton[] allToolButtons = mainToolButtons.toArray(new ToolButton[0]);

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
            a.onEdit = () -> {
                if (a.getNumber() < 1)
                    a.setNumber(1);
            };
            b.onEdit = () -> {
                if (b.getNumber() < 1)
                    b.setNumber(1);
            };
            UISplitterLayout root = new UISplitterLayout(new UISplitterLayout(a, b, false, 0.5d), new UITextButton("Resize", app.f.textDialogFieldTH, new Runnable() {
                @Override
                public void run() {
                    int w = (int) a.getNumber();
                    if (w < 1)
                        w = 1;
                    int h = (int) b.getNumber();
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

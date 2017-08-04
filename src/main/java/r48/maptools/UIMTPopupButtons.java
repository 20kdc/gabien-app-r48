/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.maptools;

import gabien.ui.Rect;
import gabien.ui.UINumberBox;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.UIMapView;

/**
 * Created on 18/06/17.
 */
public class UIMTPopupButtons extends gabien.ui.UIPopupMenu {
    public UIMTPopupButtons(final UIMapView view) {
        super(new String[] {
                TXDB.get("Reload Panorama/TS"),
                TXDB.get("Properties"),
                TXDB.get("Resize"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.stuffRendererIndependent.imageLoader.flushCache();
                        view.renderer.imageLoader.flushCache();
                        view.renderer = AppMain.system.rendererFromMap(view.map);
                        view.renderer.imageLoader.flushCache();
                        view.reinitLayerVis();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.launchSchema("RPG::Map", view.map, view);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.nextMapTool = new UIMTMapResizer(view);
                    }
                }
        }, FontSizes.dialogWindowTextHeight, false);
    }

    private static class UIMTMapResizer extends gabien.ui.UIPanel {
        public UISplitterLayout root;

        private UIMTMapResizer(final UIMapView view) {
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
            root = new UISplitterLayout(new UISplitterLayout(a, b, false, 0.5d), new UITextButton(FontSizes.textDialogFieldTextHeight, "Resize", new Runnable() {
                @Override
                public void run() {
                    RubyIO resizeTarg = view.map.getInstVarBySymbol("@data");
                    int[] r = new int[view.mapTable.planeCount];
                    int w = a.number;
                    if (w < 1)
                        w = 1;
                    int h = b.number;
                    if (h < 1)
                        h = 1;
                    if (view.mapTable.width > 0)
                        if (view.mapTable.height > 0)
                            for (int i = 0; i < r.length; i++)
                                r[i] = view.mapTable.getTiletype(0, 0, i) & 0xFFFF;
                    resizeTarg.userVal = view.mapTable.resize(w, h, r).innerBytes;
                    if (view.map.getInstVarBySymbol("@width") != null)
                        view.map.getInstVarBySymbol("@width").fixnumVal = w;
                    if (view.map.getInstVarBySymbol("@height") != null)
                        view.map.getInstVarBySymbol("@height").fixnumVal = h;
                    view.passModificationNotification();
                }
            }), true, 0);
            setBounds(root.getBounds());
            allElements.add(root);
        }

        @Override
        public void setBounds(Rect r) {
            super.setBounds(r);
            root.setBounds(new Rect(0, 0, r.width, r.height));
        }
    }
}

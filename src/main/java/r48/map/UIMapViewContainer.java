/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.UITest;
import r48.map.tiles.VXATileRenderer;
import r48.maptools.UIMTAutotile;
import r48.maptools.UIMTEventPicker;
import r48.maptools.UIMTShadowLayer;

import java.util.LinkedList;

/**
 * WARNING: May Contain Minigame.
 * Created on 1/1/17.
 */
public class UIMapViewContainer extends UIPanel {
    private final ISupplier<IConsumer<UIElement>> windowMakerSupplier;
    public UIMapView view;
    private final IMapViewCallbacks nullMapTool = new IMapViewCallbacks() {
        @Override
        public short shouldDrawAtCursor(short there, int layer, int currentLayer) {
            return there;
        }

        @Override
        public int wantOverlay(boolean minimap) {
            return 0;
        }

        @Override
        public void performOverlay(int tx, int ty, IGrInDriver igd, int px, int py, int ol, boolean minimap) {
        }

        @Override
        public void confirmAt(int x, int y, int layer) {
            LinkedList<String> toolNames = new LinkedList<String>();
            LinkedList<Runnable> toolRunnables = new LinkedList<Runnable>();

            toolNames.add("Tiles");
            toolRunnables.add(new Runnable() {
                @Override
                public void run() {
                    if (view != null)
                        AppMain.nextMapTool = new UIMTAutotile(view);
                }
            });
            if (AppMain.stuffRenderer != null) {
                if (AppMain.stuffRenderer.tileRenderer instanceof VXATileRenderer) {
                    toolNames.add("Shadow/Region");
                    toolRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                AppMain.nextMapTool = new UIMTShadowLayer(view);
                        }
                    });
                }
            }
            toolNames.add("Edit Direct.");
            toolRunnables.add(new Runnable() {
                @Override
                public void run() {
                    if (view != null)
                        AppMain.launchSchema("RPG::Map", view.map);
                }
            });
            toolNames.add("Event List");
            toolRunnables.add(new Runnable() {
                @Override
                public void run() {
                    if (view != null)
                        AppMain.nextMapTool = new UIMTEventPicker(windowMakerSupplier.get(), view);
                }
            });
            toolNames.add("Reload Tileset");
            toolRunnables.add(new Runnable() {
                @Override
                public void run() {
                    AppMain.stuffRenderer.imageLoader.flushCache();
                    if (view != null)
                        AppMain.stuffRenderer = AppMain.system.rendererFromMap(view.map);
                    AppMain.stuffRenderer.imageLoader.flushCache();
                }
            });
            toolNames.add("<for dev only>");
            toolRunnables.add(new Runnable() {
                @Override
                public void run() {
                    if (view != null)
                        windowMakerSupplier.get().accept(new UITest(view.map));
                }
            });
            AppMain.nextMapTool = new UIPopupMenu(toolNames.toArray(new String[0]), toolRunnables.toArray(new Runnable[0]), FontSizes.mapToolSelectorTextHeight, true);
        }
    };

    // Map tool switch happens at the start of each frame, so it stays out of the way of windowing code.
    private UIMapToolWrapper mapTool = null;
    private boolean wantsToolHide = false;

    private TimeWaster timeWaster = new TimeWaster();

    public UIMapViewContainer(ISupplier<IConsumer<UIElement>> wms) {
        windowMakerSupplier = wms;
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        //iconPlanX = (r.width / 2) - 32;
        //iconPlanY = (r.textHeight / 2) - 32;
        if (view != null)
            view.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {

        // remove stale tools.
        // (The way this code is written implies tools must be on rootView for now.)
        if (mapTool != null) {
            if (wantsToolHide)
                mapTool.selfClose = true;
            if (mapTool.hasClosed) {
                mapTool = null;
                if (view != null)
                    view.callbacks = nullMapTool;
            }
        }
        wantsToolHide = false;
        // switch to next tool
        if (view != null) {
            if (AppMain.nextMapTool != null) {
                if (mapTool != null) {
                    // let's just hope the user doesn't do anything in a frame
                    // that would actually somehow lead to an inconsistent state
                    mapTool.selfClose = true;
                }
                view.callbacks = nullMapTool;
                if (AppMain.nextMapTool instanceof IMapViewCallbacks)
                    view.callbacks = (IMapViewCallbacks) AppMain.nextMapTool;
                mapTool = new UIMapToolWrapper(AppMain.nextMapTool);
                AppMain.nextMapTool = null;
                windowMakerSupplier.get().accept(mapTool);
            }
        }

        super.updateAndRender(ox, oy, deltaTime, select, igd);
        if (view != null)
            return;
        Rect r = getBounds();
        timeWaster.draw(igd, ox, oy, deltaTime, r.width, r.height);
    }

    public void loadMap(String k) {
        wantsToolHide = true;
        allElements.clear();
        if (view != null)
            view.windowClosed();
        view = new UIMapView(k, 640, 480);
        view.callbacks = nullMapTool;
        allElements.add(view);
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.map;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.Application;
import gabienapp.StuffRenderer;
import gabienapp.UITest;
import gabienapp.maptools.UIMTAutotile;
import gabienapp.maptools.UIMTEventPicker;

import java.util.Random;

/**
 * WARNING: May Contain Minigame.
 * Created on 1/1/17.
 */
public class UIMapViewContainer extends UIPanel {
    public UIMapView view;
    private final ISupplier<IConsumer<UIElement>> windowMakerSupplier;

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
            Application.nextMapTool = new UIPopupMenu(new String[]{
                    "Tiles",
                    "Inspect",
                    "Edit Direct.",
                    "Event List",
                    "Reload Tileset",
            }, new Runnable[]{
                    new Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                Application.nextMapTool = new UIMTAutotile(view);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                windowMakerSupplier.get().accept(new UITest(view.map));
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                Application.launchSchema("RPG::Map", view.map);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                Application.nextMapTool = new UIMTEventPicker(windowMakerSupplier.get(), view);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            GaBIEn.hintFlushAllTheCaches();
                            if (view != null)
                                Application.stuffRenderer = StuffRenderer.rendererFromMap(view.map);
                        }
                    }
            }, true, true);
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
        //iconPlanY = (r.height / 2) - 32;
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
            if (Application.nextMapTool != null) {
                if (mapTool != null) {
                    // let's just hope the user doesn't do anything in a frame
                    // that would actually somehow lead to an inconsistent state
                    mapTool.selfClose = true;
                }
                view.callbacks = nullMapTool;
                if (Application.nextMapTool instanceof IMapViewCallbacks)
                    view.callbacks = (IMapViewCallbacks) Application.nextMapTool;
                mapTool = new UIMapToolWrapper(Application.nextMapTool);
                Application.nextMapTool = null;
                windowMakerSupplier.get().accept(mapTool);
            }
        }

        super.updateAndRender(ox, oy, deltaTime, select, igd);
        if (view != null)
            return;
        Rect r = getBounds();
        timeWaster.draw(igd, ox, oy, deltaTime, r.width, r.height);
    }

    public void loadMap(int k) {
        wantsToolHide = true;
        allElements.clear();
        if (view != null)
            view.windowClosed();
        view = new UIMapView(k, 640, 480);
        view.callbacks = nullMapTool;
        allElements.add(view);
    }
}

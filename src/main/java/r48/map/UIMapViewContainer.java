/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.tiles.VXATileRenderer;
import r48.maptools.*;
import r48.ui.UINSVertLayout;

import java.util.LinkedList;

/**
 * WARNING: May Contain Minigame.
 * Created on 1/1/17.
 */
public class UIMapViewContainer extends UIPanel {
    private final ISupplier<IConsumer<UIElement>> windowMakerSupplier;
    public UIMapView view;
    private UINSVertLayout viewToolbarSplit;
    // Use when mapTool is being set to null.
    private Runnable internalNoToolCallback = new Runnable() {
        @Override
        public void run() {

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
            viewToolbarSplit.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {

        // remove stale tools.
        // (The way this code is written implies tools must be on rootView for now.)
        if (mapTool != null) {
            if (wantsToolHide)
                mapTool.selfClose = true;
            if (mapTool.hasClosed) {
                if (AppMain.nextMapTool == mapTool.pattern)
                    AppMain.nextMapTool = null;
                mapTool = null;
                internalNoToolCallback.run();
                if (view != null)
                    view.callbacks = null;
            }
        }
        wantsToolHide = false;
        // switch to next tool
        if (view != null) {
            if (AppMain.nextMapTool != null) {
                boolean sameAsBefore = true;
                if (mapTool != null) {
                    if (mapTool.pattern != AppMain.nextMapTool) {
                        // let's just hope the user doesn't do anything in a frame
                        // that would actually somehow lead to an inconsistent state
                        mapTool.selfClose = true;
                        sameAsBefore = false;
                    }
                } else {
                    sameAsBefore = false;
                }
                if (!sameAsBefore) {
                    view.callbacks = null;
                    if (AppMain.nextMapTool instanceof IMapViewCallbacks)
                        view.callbacks = (IMapViewCallbacks) AppMain.nextMapTool;
                    mapTool = new UIMapToolWrapper(AppMain.nextMapTool);
                    windowMakerSupplier.get().accept(mapTool);
                }
            } else {
                if (mapTool != null) {
                    mapTool.selfClose = true;
                    mapTool = null;
                    internalNoToolCallback.run();
                    view.callbacks = null;
                }
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
        Rect b = getBounds();
        // Creating the MapView and such causes quite a few side-effects (specifically global StuffRenderer kick-in-the-pants).
        // Also kick the dictionaries because of the event dictionary.
        view = new UIMapView(k, b.width, b.height);
        view.pickTileHelper = new IConsumer<Short>() {
            @Override
            public void accept(Short aShort) {
                UIMTAutotile atf = new UIMTAutotile(view);
                atf.selectTile(aShort);
                AppMain.nextMapTool = atf;
            }
        };
        UIScrollLayout layerTabLayout = new UIScrollLayout(false);
        layerTabLayout.scrollbar.setBounds(new Rect(0, 0, 8, 8));

        final LinkedList<UITextButton> tools = new LinkedList<UITextButton>();
        final IConsumer<Integer> clearTools = new IConsumer<Integer>() {
            @Override
            public void accept(Integer t) {
                for (UITextButton utb : tools)
                    utb.state = false;
                if (t != -1)
                    tools.get(t).state = true;
            }
        };

        internalNoToolCallback = new Runnable() {
            @Override
            public void run() {
                clearTools.accept(-1);
            }
        };

        // -- Kind of a monolith here. Map tools ALWAYS go first, and must be togglables.
        // It is assumed that this is the only class capable of causing tool changes.

        for (int i = 0; i < view.mapTable.planeCount; i++) {
            final int thisButton = i;
            final UITextButton button = new UITextButton(FontSizes.mapLayertabTextHeight, "L" + i, new Runnable() {
                @Override
                public void run() {
                    clearTools.accept(thisButton);
                    view.currentLayer = thisButton;
                    AppMain.nextMapTool = new UIMTAutotile(view);
                }
            }).togglable();
            tools.add(button);
        }
        if (view.renderer.tileRenderer instanceof VXATileRenderer) {
            final int thisButton = tools.size();
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Shadow/Region"), new Runnable() {
                @Override
                public void run() {
                    clearTools.accept(thisButton);
                    AppMain.nextMapTool = new UIMTShadowLayer(view);
                }
            }).togglable());
        }
        {
            final int thisButton = tools.size();
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Events"), new Runnable() {
                @Override
                public void run() {
                    clearTools.accept(thisButton);
                    AppMain.nextMapTool = new UIMTEventPicker(windowMakerSupplier.get(), view);
                }
            }).togglable());
        }
        {
            final int thisButton = tools.size();
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Layer Visibility"), new Runnable() {
                @Override
                public void run() {
                    clearTools.accept(thisButton);
                    UIScrollLayout svl = new UIScrollLayout(true);
                    int h = 0;
                    for (int i = 0; i < view.renderer.layers.length; i++) {
                        final int fi = i;
                        UITextButton layerVis = new UITextButton(FontSizes.mapLayertabTextHeight, view.renderer.layers[i].getName(), new Runnable() {
                            @Override
                            public void run() {
                                view.layerVis[fi] = !view.layerVis[fi];
                            }
                        }).togglable();
                        layerVis.state = view.layerVis[i];
                        h += layerVis.getBounds().height;
                        svl.panels.add(layerVis);
                    }
                    svl.setBounds(new Rect(0, 0, 320, h));
                    AppMain.nextMapTool = svl;
                }
            }).togglable());
        }

        // Utility buttons

        tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Tile From Map"), new Runnable() {
            @Override
            public void run() {
                // Select the current tile layer
                clearTools.accept(view.currentLayer);
                AppMain.nextMapTool = new UIMTPickTile(view);
            }
        }));
        {
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("..."), new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools.accept(thisButton);
                    AppMain.nextMapTool = new UIMTPopupButtons(view);
                }
            }).togglable());
        }

        // finish layout
        for (UITextButton utb : tools)
            layerTabLayout.panels.add(utb);

        layerTabLayout.setBounds(new Rect(0, 0, 28, 28));
        viewToolbarSplit = new UINSVertLayout(layerTabLayout, view);
        allElements.add(viewToolbarSplit);
        AppMain.schemas.kickAllDictionariesForMapChange();
    }
}

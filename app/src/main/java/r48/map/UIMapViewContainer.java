/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;

import org.eclipse.jdt.annotation.Nullable;

import gabien.render.IGrDriver;
import gabien.ui.UIElement;
import gabien.ui.UILayer;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.maptools.UIMTAutotile;
import r48.maptools.UIMTBase;

/**
 * WARNING: May Contain Minigame.
 * Created on 1/1/17.
 */
public class UIMapViewContainer extends App.Pan {
    public UIMapView view;
    private UISplitterLayout viewToolbarSplit;
    // Use when mapTool is being set to null.
    private Runnable internalNoToolCallback = new Runnable() {
        @Override
        public void run() {

        }
    };

    public UIMTBase nextMapTool = null;

    // Map tool switch happens at the start of each frame, so it stays out of the way of windowing code.
    private UIMTBase mapTool = null;

    private final TimeWaster timeWaster;

    private double deltaTimeAccum = 0;

    private boolean masterRenderDisableSwitch = false;
    private boolean masterAnimDisableSwitch = false;
    private boolean pickTileSwitch = true;

    public UIMapViewContainer(App app) {
        super(app);
        timeWaster = new TimeWaster(app);
    }

    @Override
    public String toString() {
        return T.t.map;
    }

    @Override
    protected void layoutRunImpl() {
        if (viewToolbarSplit != null)
            viewToolbarSplit.setForcedBounds(this, new Rect(getSize()));
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        if (viewToolbarSplit != null)
            return viewToolbarSplit.getWantedSize();
        return super.layoutRecalculateMetricsImpl();
    }

    @Override
    public int layoutGetHForW(int width) {
        if (viewToolbarSplit != null)
            return viewToolbarSplit.layoutGetHForW(width);
        return super.layoutGetHForW(width);
    }

    @Override
    public int layoutGetWForH(int height) {
        if (viewToolbarSplit != null)
            return viewToolbarSplit.layoutGetWForH(height);
        return super.layoutGetWForH(height);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        deltaTimeAccum += deltaTime;
    }

    @Override
    public void renderLayer(IGrDriver igd, UILayer layer) {
        // remove stale tools.
        // (this code is weird!)
        if (mapTool != null) {
            if (mapTool.hasClosed) {
                if (nextMapTool == mapTool)
                    nextMapTool = null;
                mapTool = null;
                internalNoToolCallback.run();
                if (view != null)
                    view.callbacks = null;
            }
        }
        // switch to next tool
        if (view != null) {
            if (nextMapTool != null) {
                boolean sameAsBefore = true;
                if (mapTool != null) {
                    if (mapTool != nextMapTool) {
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
                    if (nextMapTool instanceof IMapViewCallbacks)
                        view.callbacks = (IMapViewCallbacks) nextMapTool;
                    mapTool = nextMapTool;
                    app.ui.wm.createWindow(mapTool);
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

        super.renderLayer(igd, layer);
        if (layer == UILayer.Content) {
            if (view != null) {
                deltaTimeAccum = 0;
                return;
            }
            Size r = getSize();
            timeWaster.draw(igd, deltaTimeAccum, r.width, r.height);
            deltaTimeAccum = 0;
        }
    }

    public void loadMap(String gum) {
        if (mapTool != null)
            mapTool.selfClose = true;

        for (UIElement u : layoutGetElements())
            layoutRemoveElement(u);
        if (gum == null) {
            view = null;
            viewToolbarSplit = null;
            internalNoToolCallback = new Runnable() {
                @Override
                public void run() {
                }
            };
            return;
        }
        Size b = getSize();
        // Creating the MapView and such causes quite a few side-effects (specifically global StuffRenderer kick-in-the-pants).
        // Also kick the dictionaries because of the event dictionary.
        view = new UIMapView(app, gum, b.width, b.height);
        view.viewRenderDisableSwitch = masterRenderDisableSwitch;
        view.viewAnimDisableSwitch = masterAnimDisableSwitch;
        final IMapToolContext mtc = new IMapToolContext() {
            @Override
            public UIMapView getMapView() {
                return view;
            }

            @Override
            public void createWindow(UIElement window) {
                app.ui.wm.createWindow(window);
            }

            @Override
            public void accept(UIMTBase nextTool) {
                nextMapTool = nextTool;
            }

            @Override
            public UIMTAutotile showATField() {
                if (mapTool instanceof UIMTAutotile) {
                    ((UIMTAutotile) mapTool).refresh();
                    return (UIMTAutotile) mapTool;
                } else {
                    return (UIMTAutotile) (nextMapTool = new UIMTAutotile(this));
                }
            }

            @Override
            public boolean getMasterRenderDisableSwitch() {
                return masterRenderDisableSwitch;
            }

            @Override
            public void setMasterRenderDisableSwitch(boolean value) {
                masterRenderDisableSwitch = value;
                if (view != null)
                    view.viewRenderDisableSwitch = value;
            }

            @Override
            public boolean getMasterAnimDisableSwitch() {
                return masterAnimDisableSwitch;
            }

            @Override
            public void setMasterAnimDisableSwitch(boolean value) {
                masterAnimDisableSwitch = value;
                if (view != null)
                    view.viewAnimDisableSwitch = value;
            }

            @Override
            public boolean getPickTileSwitch() {
                return pickTileSwitch;
            }
            @Override
            public void setPickTileSwitch(boolean value) {
                pickTileSwitch = value;
            }
        };

        final IEditingToolbarController metc = view.map.makeToolbar(mtc);

        if (metc.allowPickTile()) {
            view.pickTileHelper = (aShort) -> {
                UIMTAutotile atf = mtc.showATField();
                atf.selectTile(aShort, pickTileSwitch);
                nextMapTool = atf;
            };
        }

        viewToolbarSplit = new UISplitterLayout(metc.getBar(), view, true, 0);
        layoutAddElement(viewToolbarSplit);
        app.sdb.kickAllDictionariesForMapChange();
        internalNoToolCallback = () -> {
            metc.noTool();
        };
        metc.noTool();
    }
}

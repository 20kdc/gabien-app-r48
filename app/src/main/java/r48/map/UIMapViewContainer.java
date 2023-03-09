/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.uslx.append.*;
import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement;
import r48.App;
import r48.maptools.UIMTAutotile;
import r48.maptools.UIMTBase;
import r48.ui.UINSVertLayout;

/**
 * WARNING: May Contain Minigame.
 * Created on 1/1/17.
 */
public class UIMapViewContainer extends UIElement.UIPanel {
    public UIMapView view;
    private UINSVertLayout viewToolbarSplit;
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

    public final App app;

    public UIMapViewContainer(App app) {
        this.app = app;
        timeWaster = new TimeWaster(app);
    }

    @Override
    public String toString() {
        return app.ts("Map");
    }

    @Override
    public void runLayout() {
        if (viewToolbarSplit != null) {
            viewToolbarSplit.setForcedBounds(this, new Rect(getSize()));
            setWantedSize(viewToolbarSplit.getWantedSize());
        }
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        deltaTimeAccum += deltaTime;
    }

    @Override
    public void render(IGrDriver igd) {
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

        super.render(igd);
        if (view != null) {
            deltaTimeAccum = 0;
            return;
        }
        Size r = getSize();
        timeWaster.draw(igd, 0, 0, deltaTimeAccum, r.width, r.height);
        deltaTimeAccum = 0;
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
        };

        final IEditingToolbarController metc = view.map.toolbar.apply(mtc);

        if (metc.allowPickTile()) {
            view.pickTileHelper = new IConsumer<Short>() {
                @Override
                public void accept(Short aShort) {
                    UIMTAutotile atf = mtc.showATField();
                    atf.selectTile(aShort);
                    nextMapTool = atf;
                }
            };
        }

        viewToolbarSplit = new UINSVertLayout(metc.getBar(), view);
        layoutAddElement(viewToolbarSplit);
        app.sdb.kickAllDictionariesForMapChange();
        internalNoToolCallback = new Runnable() {
            @Override
            public void run() {
                metc.noTool();
            }
        };
        metc.noTool();
    }
}

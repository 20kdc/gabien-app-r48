/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.App;
import r48.RubyTable;
import r48.maptools.*;

import java.util.LinkedList;

/**
 * The standard map editing toolbar is the responsibility of this class.
 * Created on 11/08/17.
 */
public class MapEditingToolbarController extends App.Svc implements IEditingToolbarController {
    private UIScrollLayout rootLayout = new UIScrollLayout(false, app.f.mapToolbarScrollersize);
    private final LinkedList<UITextButton> tools = new LinkedList<UITextButton>();
    private final boolean readonlyTiles;

    public MapEditingToolbarController(final IMapToolContext viewGiver, boolean rd) {
        this(viewGiver, rd, new ToolButton[0]);
    }

    public MapEditingToolbarController(final IMapToolContext viewGiver, boolean rd, ToolButton[] toolFuncs) {
        this(viewGiver, rd, toolFuncs, new ToolButton[0]);
    }

    public MapEditingToolbarController(final IMapToolContext viewGiver, boolean rd, final ToolButton[] toolFuncs, final ToolButton[] addendum) {
        super(viewGiver.getMapView().app);
        readonlyTiles = rd;

        final UIMapView view = viewGiver.getMapView();

        // -- Kind of a monolith here. Map tools ALWAYS go first, and must be togglables.
        // It is assumed that this is the only class capable of causing tool changes (unless noTool is called)

        if (!readonlyTiles) {
            for (int i = 0; i < view.mapTable.planeCount; i++) {
                final int thisButton = i;
                final UITextButton button = new UITextButton("L" + i, app.f.mapLayertabTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        clearTools(thisButton);
                        view.currentLayer = thisButton;
                        viewGiver.showATField();
                    }
                }).togglable(false);
                tools.add(button);
            }
        }
        for (int i = 0; i < toolFuncs.length; i++) {
            final int toolId = i;
            tools.add(new UITextButton(toolFuncs[i].text, app.f.mapLayertabTextHeight, new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(toolFuncs[toolId].apply(viewGiver));
                }
            }).togglable(false));
        }
        if (view.mapTable.eventAccess != null) {
            tools.add(new UITextButton(view.mapTable.eventAccess.customEventsName(), app.f.mapLayertabTextHeight, new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTEventPicker(viewGiver));
                }
            }).togglable(false));
        }
        tools.add(new UITextButton(app.ts("Layer Visibility"), app.f.mapLayertabTextHeight, new Runnable() {
            final int thisButton = tools.size();

            @Override
            public void run() {
                clearTools(thisButton);
                UIScrollLayout svl = new UIScrollLayout(true, app.f.generalScrollersize);
                svl.panelsAdd(new UITextButton(app.ts("Disable rendering (even across maps!)"), app.f.mapLayertabTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        viewGiver.setMasterRenderDisableSwitch(!viewGiver.getMasterRenderDisableSwitch());
                    }
                }).togglable(viewGiver.getMasterRenderDisableSwitch()));
                for (int i = 0; i < view.mapTable.renderer.layers.length; i++) {
                    final int fi = i;
                    UITextButton layerVis = new UITextButton(view.mapTable.renderer.layers[i].getName(), app.f.mapLayertabTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            view.layerVis[fi] = !view.layerVis[fi];
                        }
                    }).togglable(view.layerVis[i]);
                    svl.panelsAdd(layerVis);
                }
                viewGiver.accept(UIMTBase.wrap(viewGiver, svl));
            }
        }).togglable(false));

        // Utility buttons

        if (!readonlyTiles) {
            tools.add(new UITextButton(app.ts("Grab Tile"), app.f.mapLayertabTextHeight, new Runnable() {
                @Override
                public void run() {
                    // Select the current tile layer
                    clearTools(view.currentLayer);
                    viewGiver.accept(new UIMTPickTile(viewGiver));
                }
            }));

            tools.add(new UITextButton(app.ts("B.Copy"), app.f.mapLayertabTextHeight, new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTCopyRectangle(viewGiver));
                }
            }));

            tools.add(new UITextButton(app.ts("B.Paste"), app.f.mapLayertabTextHeight, new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    if (app.theClipboard == null) {
                        app.ui.launchDialog("Unable - there is no clipboard.");
                        return;
                    }
                    if (app.theClipboard.type != 'u') {
                        app.ui.launchDialog("Unable - the clipboard must contain a section of map data - This is not a usertype.");
                        return;
                    }
                    if (!app.theClipboard.symVal.equals("Table")) {
                        app.ui.launchDialog("Unable - the clipboard must contain a section of map data - This is not a Table.");
                        return;
                    }
                    RubyTable rt = new RubyTable(app.theClipboard.userVal);
                    if (rt.planeCount != viewGiver.getMapView().mapTable.planeCount) {
                        app.ui.launchDialog("Unable - the map data must contain the same amount of layers for transfer.");
                        return;
                    }
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTPasteRectangle(viewGiver, rt));
                }
            }));
        }

        tools.add(new UITextButton(app.ts("..."), app.f.mapLayertabTextHeight, new Runnable() {
            final int thisButton = tools.size();

            @Override
            public void run() {
                clearTools(thisButton);
                viewGiver.accept(new UIMTPopupButtons(viewGiver, readonlyTiles, addendum));
            }
        }).togglable(false));

        // finish layout
        int maxH = 1;
        for (UITextButton utb : tools) {
            rootLayout.panelsAdd(utb);
            maxH = Math.max(maxH, utb.getWantedSize().height);
        }
    }

    public void clearTools(int t) {
        for (UITextButton utb : tools)
            utb.state = false;
        if (t != -1)
            tools.get(t).state = true;
    }

    @Override
    public void noTool() {
        clearTools(-1);
    }

    @Override
    public UIElement getBar() {
        return rootLayout;
    }

    @Override
    public boolean allowPickTile() {
        return !readonlyTiles;
    }

    public abstract static class ToolButton implements IFunction<IMapToolContext, UIMTBase> {
        public final String text;
        public ToolButton(String txt) {
            text = txt;
        }
    }
}

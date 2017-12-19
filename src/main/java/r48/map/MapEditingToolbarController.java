/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.maptools.*;

import java.util.LinkedList;

/**
 * The standard map editing toolbar is the responsibility of this class.
 * Created on 11/08/17.
 */
public class MapEditingToolbarController implements IEditingToolbarController {
    private UIScrollLayout rootLayout = new UIScrollLayout(false, FontSizes.mapToolbarScrollersize);
    private final LinkedList<UITextButton> tools = new LinkedList<UITextButton>();
    private final boolean readonlyTiles;

    public MapEditingToolbarController(final IMapToolContext viewGiver, boolean rd) {
        // Usual stupid complaints, please ignore (if you add the diamond w/ or w/o types the compiler errors)
        this(viewGiver, rd, new String[] {}, new IFunction[0]);
    }

    public MapEditingToolbarController(final IMapToolContext viewGiver, boolean rd, String[] toolNames, final IFunction<IMapToolContext, UIMTBase>[] toolFuncs) {
        readonlyTiles = rd;

        final UIMapView view = viewGiver.getMapView();

        // -- Kind of a monolith here. Map tools ALWAYS go first, and must be togglables.
        // It is assumed that this is the only class capable of causing tool changes (unless noTool is called)

        if (!readonlyTiles) {
            for (int i = 0; i < view.mapTable.planeCount; i++) {
                final int thisButton = i;
                final UITextButton button = new UITextButton(FontSizes.mapLayertabTextHeight, "L" + i, new Runnable() {
                    @Override
                    public void run() {
                        clearTools(thisButton);
                        view.currentLayer = thisButton;
                        viewGiver.accept(new UIMTAutotile(viewGiver));
                    }
                }).togglable();
                tools.add(button);
            }
        }
        for (int i = 0; i < toolNames.length; i++) {
            final int toolId = i;
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, toolNames[i], new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(toolFuncs[toolId].apply(viewGiver));
                }
            }).togglable());
        }
        if (view.mapTable.eventAccess != null) {
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Events"), new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTEventPicker(viewGiver));
                }
            }).togglable());
        }
        tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Layer Visibility"), new Runnable() {
            final int thisButton = tools.size();

            @Override
            public void run() {
                clearTools(thisButton);
                UIScrollLayout svl = new UIScrollLayout(true, FontSizes.generalScrollersize);
                int h = 0;
                for (int i = 0; i < view.mapTable.renderer.layers.length; i++) {
                    final int fi = i;
                    UITextButton layerVis = new UITextButton(FontSizes.mapLayertabTextHeight, view.mapTable.renderer.layers[i].getName(), new Runnable() {
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
                viewGiver.accept(UIMTBase.wrap(viewGiver, svl, false));
            }
        }).togglable());

        // Utility buttons

        if (!readonlyTiles) {
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Grab Tile"), new Runnable() {
                @Override
                public void run() {
                    // Select the current tile layer
                    clearTools(view.currentLayer);
                    viewGiver.accept(new UIMTPickTile(viewGiver));
                }
            }));

            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("B.Copy"), new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTCopyRectangle(viewGiver));
                }
            }));

            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("B.Paste"), new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    if (AppMain.theClipboard == null) {
                        AppMain.launchDialog("Unable - there is no clipboard.");
                        return;
                    }
                    if (AppMain.theClipboard.type != 'u') {
                        AppMain.launchDialog("Unable - the clipboard must contain a section of map data - This is not a usertype.");
                        return;
                    }
                    if (!AppMain.theClipboard.symVal.equals("Table")) {
                        AppMain.launchDialog("Unable - the clipboard must contain a section of map data - This is not a Table.");
                        return;
                    }
                    RubyTable rt = new RubyTable(AppMain.theClipboard.userVal);
                    if (rt.planeCount != viewGiver.getMapView().mapTable.planeCount) {
                        AppMain.launchDialog("Unable - the map data must contain the same amount of layers for transfer.");
                        return;
                    }
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTPasteRectangle(viewGiver, rt));
                }
            }));
        }

        tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("..."), new Runnable() {
            final int thisButton = tools.size();

            @Override
            public void run() {
                clearTools(thisButton);
                viewGiver.accept(new UIMTPopupButtons(viewGiver, readonlyTiles));
            }
        }).togglable());

        // finish layout
        int maxH = 1;
        for (UITextButton utb : tools) {
            rootLayout.panels.add(utb);
            maxH = Math.max(maxH, utb.getBounds().height);
        }
        rootLayout.setBounds(new Rect(0, 0, maxH + FontSizes.mapToolbarScrollersize, maxH + FontSizes.mapToolbarScrollersize));
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
}

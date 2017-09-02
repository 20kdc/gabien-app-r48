/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.tiles.VXATileRenderer;
import r48.maptools.*;

import java.util.LinkedList;

/**
 * The standard map editing toolbar is the responsibility of this class.
 * Created on 11/08/17.
 */
public class MapEditingToolbarController implements IEditingToolbarController {
    public UIScrollLayout rootLayout = new UIScrollLayout(false, FontSizes.mapToolbarScrollersize);
    private final LinkedList<UITextButton> tools = new LinkedList<UITextButton>();

    public MapEditingToolbarController(final IMapToolContext viewGiver, final ISupplier<IConsumer<UIElement>> windowMakerSupplier) {

        final UIMapView view = viewGiver.getMapView();

        // -- Kind of a monolith here. Map tools ALWAYS go first, and must be togglables.
        // It is assumed that this is the only class capable of causing tool changes.

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
        if (view.renderer.tileRenderer instanceof VXATileRenderer) {
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Shadow/Region"), new Runnable() {
                final int thisButton = tools.size();
                @Override
                public void run() {
                    clearTools(thisButton);
                    viewGiver.accept(new UIMTShadowLayer(viewGiver));
                }
            }).togglable());
        }
        tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Events"), new Runnable() {
            final int thisButton = tools.size();
            @Override
            public void run() {
                clearTools(thisButton);
                viewGiver.accept(new UIMTEventPicker(viewGiver));
            }
        }).togglable());
        tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Layer Visibility"), new Runnable() {
            final int thisButton = tools.size();
            @Override
            public void run() {
                clearTools(thisButton);
                UIScrollLayout svl = new UIScrollLayout(true, FontSizes.generalScrollersize);
                int h = 0;
                for (int i = 0; i < view.renderer.layers.length; i++) {
                    final int fi = i;
                    UITextButton layerVis = new UITextButton(FontSizes.mapLayertabTextHeight, view.renderer.layers[i].getName(), new Runnable() {
                            @Override
                            public void run() {view.layerVis[fi] = !view.layerVis[fi];
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
                    AppMain.launchDialog("Unable - the clipboard must contain a section of map data.\nThis is not a usertype.");
                    return;
                }
                if (!AppMain.theClipboard.symVal.equals("Table")) {
                    AppMain.launchDialog("Unable - the clipboard must contain a section of map data.\nThis is not a Table.");
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

        tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("..."), new Runnable() {
            final int thisButton = tools.size();

            @Override
            public void run() {
                clearTools(thisButton);
                viewGiver.accept(new UIMTPopupButtons(viewGiver));
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
}

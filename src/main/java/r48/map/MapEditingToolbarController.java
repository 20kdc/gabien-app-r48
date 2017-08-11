/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.tiles.VXATileRenderer;
import r48.maptools.*;

import java.util.LinkedList;

/**
 * The standard map editing toolbar is the responsibility of this class.
 * Created on 11/08/17.
 */
public class MapEditingToolbarController implements IEditingToolbarController {
    public UIScrollLayout rootLayout = new UIScrollLayout(false);
    private final LinkedList<UITextButton> tools = new LinkedList<UITextButton>();

    public MapEditingToolbarController(final UIMapView view, final ISupplier<IConsumer<UIElement>> windowMakerSupplier) {
        rootLayout.scrollbar.setBounds(new Rect(0, 0, 8, 8));

        // -- Kind of a monolith here. Map tools ALWAYS go first, and must be togglables.
        // It is assumed that this is the only class capable of causing tool changes.

        for (int i = 0; i < view.mapTable.planeCount; i++) {
            final int thisButton = i;
            final UITextButton button = new UITextButton(FontSizes.mapLayertabTextHeight, "L" + i, new Runnable() {
                @Override
                public void run() {
                    clearTools(thisButton);
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
                    clearTools(thisButton);
                    AppMain.nextMapTool = new UIMTShadowLayer(view);
                }
            }).togglable());
        }
        {
            final int thisButton = tools.size();
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Events"), new Runnable() {
                @Override
                public void run() {
                    clearTools(thisButton);
                    AppMain.nextMapTool = new UIMTEventPicker(windowMakerSupplier.get(), view);
                }
            }).togglable());
        }
        {
            final int thisButton = tools.size();
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("Layer Visibility"), new Runnable() {
                @Override
                public void run() {
                    clearTools(thisButton);
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
                clearTools(view.currentLayer);
                AppMain.nextMapTool = new UIMTPickTile(view);
            }
        }));
        {
            tools.add(new UITextButton(FontSizes.mapLayertabTextHeight, TXDB.get("..."), new Runnable() {
                final int thisButton = tools.size();

                @Override
                public void run() {
                    clearTools(thisButton);
                    AppMain.nextMapTool = new UIMTPopupButtons(view);
                }
            }).togglable());
        }

        // finish layout
        for (UITextButton utb : tools)
            rootLayout.panels.add(utb);

        rootLayout.setBounds(new Rect(0, 0, 28, 28));
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

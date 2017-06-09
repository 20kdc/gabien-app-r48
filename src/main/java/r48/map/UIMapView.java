/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.IOsbDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.map.drawlayers.IMapViewDrawLayer;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Note: this class will slightly draw out of bounds.
 * Created on 12/27/16.
 */
public class UIMapView extends UIElement implements IWindowElement {
    // NOTE: camX/camY is negated display offset, from TL corner.
    private int camX, camY, lmX, lmY;

    private boolean dragging = false;
    public final RubyIO map;
    // replaced when the map is edited
    public RubyTable mapTable;
    // Set by the layer tabs UI
    public int currentLayer;

    public final String mapId;
    public boolean minimap = false;
    public boolean shiftDown = false;
    public IMapViewCallbacks callbacks;
    // Responsible for starting a tool with the given tile.
    public IConsumer<Short> pickTileHelper = null;

    public final int tileSize;

    private MapViewUpdateScheduler scheduler = new MapViewUpdateScheduler();
    // Managed using finalize for now.
    private IOsbDriver offscreenBuf;

    private Runnable listener = new Runnable() {
        @Override
        public void run() {
            // Not an incredibly high-cost operation, thankfully,
            //  since it'll have to run on any edits.
            mapTable = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            AppMain.stuffRenderer = AppMain.system.rendererFromMap(map);
            scheduler.forceNextUpdate = true;
        }
    };
    public boolean[] layerVis;

    public UIMapView(String mapN, int i, int i1) {
        // Note that using setBounds directly causes camera adjustment (bad, only just created element)
        setBounds(new Rect(0, 0, i, i1));

        mapId = mapN;
        map = AppMain.objectDB.getObject(mapN, "RPG::Map");
        AppMain.objectDB.registerModificationHandler(map, listener);
        listener.run();

        // begin!
        layerVis = new boolean[AppMain.stuffRenderer.layers.length];
        for (int j = 0; j < layerVis.length; j++)
            layerVis[j] = true;

        AppMain.stuffRenderer = AppMain.system.rendererFromMap(map);
        tileSize = AppMain.stuffRenderer.tileRenderer.getTileSize();

        camX = -i / 2;
        camY = -i1 / 2;
        if (minimap) {
            camX += 2 * mapTable.width;
            camY += 2 * mapTable.height;
        } else {
            camX += (tileSize * mapTable.width) / 2;
            camY += (tileSize * mapTable.height) / 2;
        }
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        boolean debug = igd.isKeyDown(IGrInDriver.VK_D);
        shiftDown = igd.isKeyDown(IGrInDriver.VK_SHIFT);
        if (selected)
            if (igd.isKeyJustPressed(IGrInDriver.VK_M))
                toggleMinimap();

        int eTileSize = tileSize;
        if (minimap)
            eTileSize = 2;

        int mouseXT = UIElement.sensibleCellDiv((igd.getMouseX() - ox) + camX, eTileSize);
        int mouseYT = UIElement.sensibleCellDiv((igd.getMouseY() - oy) + camY, eTileSize);
        Rect camR = getBounds();
        // Stuff any possible important information...
        char[] visConfig = new char[layerVis.length];
        for (int i = 0; i < layerVis.length; i++)
            visConfig[i] = layerVis[i] ? 'T' : 'F';
        String config = camR.width + "_" + camR.height + "_" + camX + "_" + camY + "_" + mouseXT + "_" + mouseYT + "_" + eTileSize + "_" + debug + "_" + AppMain.stuffRenderer.tileRenderer.getFrame() + "_" + AppMain.stuffRenderer.hashCode() + "_" + currentLayer + "_" + new String(visConfig) + "_" + AppMain.nextMapTool;
        if (scheduler.needsUpdate(config)) {
            boolean remakeBuf = true;
            if (offscreenBuf != null)
                if ((offscreenBuf.getWidth() == camR.width) && (offscreenBuf.getHeight() == camR.height))
                    remakeBuf = false;
            if (remakeBuf) {
                if (offscreenBuf != null)
                    offscreenBuf.shutdown();
                offscreenBuf = GaBIEn.makeOffscreenBuffer(camR.width, camR.height);
            }
            render(mouseXT, mouseYT, eTileSize, currentLayer, debug, offscreenBuf);
        }
        if (offscreenBuf != null)
            igd.blitImage(0, 0, camR.width, camR.height, ox, oy, offscreenBuf);
    }

    public void render(int mouseXT, int mouseYT, int eTileSize, int currentLayer, boolean debug, IGrDriver igd) {
        // The offscreen image implicitly crops.
        igd.clearAll(0, 0, 0);
        IMapViewDrawLayer[] layers = AppMain.stuffRenderer.layers;
        int camTR = UIElement.sensibleCellDiv((camX + igd.getWidth()), eTileSize) + 1;
        int camTB = UIElement.sensibleCellDiv((camY + igd.getHeight()), eTileSize) + 1;
        int camTX = UIElement.sensibleCellDiv(camX, eTileSize);
        int camTY = UIElement.sensibleCellDiv(camY, eTileSize);
        for (int i = 0; i < layers.length; i++)
            if (layerVis[i])
                layers[i].draw(camX, camY, camTX, camTY, camTR, camTB, mouseXT, mouseYT, eTileSize, currentLayer, callbacks, debug, igd);

        if (callbacks != null) {
            int ovlLayers = callbacks.wantOverlay(minimap);
            for (int l = 0; l < ovlLayers; l++) {
                for (int i = camTX; i < camTR; i++) {
                    for (int j = camTY; j < camTB; j++) {
                        int px = i * eTileSize;
                        int py = j * eTileSize;
                        px -= camX;
                        py -= camY;
                        // Keeping in mind px/py is the TL corner...
                        px += eTileSize / 2;
                        px -= tileSize / 2;
                        py += eTileSize / 2;
                        py -= tileSize / 2;
                        callbacks.performOverlay(i, j, igd, px, py, l, minimap);
                    }
                }
            }
        }
        String shortcuts = "Any mouse button: Scroll, Shift-left: Pick tile.";
        if (callbacks != null)
            shortcuts = "Left mouse button: Use tool, others: Scroll, Shift-left: Pick tile.";
        UILabel.drawLabel(igd, 0, 0, 0, mapId + ";" + mouseXT + ", " + mouseYT + "; " + shortcuts, false, FontSizes.mapPositionTextHeight);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        lmX = x;
        lmY = y;
        if (button != 1) {
            dragging = true;
        } else {
            // implicit support for one-button mice
            if (!minimap) {
                int mouseXT = UIElement.sensibleCellDiv(x + camX, tileSize);
                int mouseYT = UIElement.sensibleCellDiv(y + camY, tileSize);
                dragging = false;
                if (shiftDown) {
                    if (!mapTable.outOfBounds(mouseXT, mouseYT))
                        pickTileHelper.accept(mapTable.getTiletype(mouseXT, mouseYT, currentLayer));
                } else if (callbacks != null) {
                    callbacks.confirmAt(mouseXT, mouseYT, currentLayer);
                } else {
                    dragging = true;
                }
            } else {
                dragging = true;
            }
        }
    }

    @Override
    public void handleDrag(int x, int y) {
        if (dragging) {
            camX -= x - lmX;
            camY -= y - lmY;
            lmX = x;
            lmY = y;
        } else if (!minimap) {
            int mouseXT = UIElement.sensibleCellDiv(x + camX, tileSize);
            int mouseYT = UIElement.sensibleCellDiv(y + camY, tileSize);
            if (!shiftDown)
                if (callbacks != null)
                    pickTileHelper.accept(mapTable.getTiletype(mouseXT, mouseYT, currentLayer));
        }
    }

    public void toggleMinimap() {
        Rect camR = getBounds();
        int camW = camR.width;
        int camH = camR.height;

        camX += camW / 2;
        camY += camH / 2;
        if (minimap) {
            camX *= tileSize / 2;
            camY *= tileSize / 2;
        } else {
            camX /= tileSize / 2;
            camY /= tileSize / 2;
        }
        camX -= camW / 2;
        camY -= camH / 2;
        minimap = !minimap;
    }

    @Override
    public boolean wantsSelfClose() {
        return false;
    }

    @Override
    public void windowClosed() {
        AppMain.objectDB.deregisterModificationHandler(map, listener);
    }

    // Used by tools, after they're done doing whatever.
    // Basically a convenience method.
    public void passModificationNotification() {
        AppMain.objectDB.objectRootModified(map);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (offscreenBuf != null)
            offscreenBuf.shutdown();
    }
}

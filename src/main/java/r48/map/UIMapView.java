/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.drawlayers.PassabilityMapViewDrawLayer;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

/**
 * Note: this class will slightly draw out of bounds.
 * Created on 12/27/16.
 */
public class UIMapView extends UIElement implements IWindowElement {
    // NOTE: camX/camY is negated display offset, from TL corner.
    // Also note that they are adjusted to internal scale.
    private double camX, camY;
    // System scale mouse values.
    private int lmX, lmY;

    private boolean dragging = false;
    public final RubyIO map;
    // replaced when the map is edited
    public RubyTable mapTable;
    // Set by the layer tabs UI
    public int currentLayer;

    // internal scaling is a UIMapView-controlled thing, for good reason.
    private int internalScaling = 1;

    public final String mapId;
    public boolean minimap = false;
    public boolean shiftDown = false;
    public IMapViewCallbacks callbacks;
    // Responsible for starting a tool with the given tile.
    public IConsumer<Short> pickTileHelper = null;

    public final int tileSize;

    private MapViewUpdateScheduler scheduler = new MapViewUpdateScheduler();
    // Managed using finalize for now.
    private IGrDriver offscreenBuf;

    private IConsumer<SchemaPath> listener = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            // Not an incredibly high-cost operation, thankfully,
            //  since it'll have to run on any edits.
            mapTable = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            renderer = AppMain.system.rendererFromMap(map);
            reinitLayerVis();
            scheduler.forceNextUpdate = true;
        }
    };
    public boolean[] layerVis;
    public StuffRenderer renderer;

    public UIMapView(String mapN, int i, int i1) {
        setBounds(new Rect(0, 0, i, i1));

        mapId = mapN;
        map = AppMain.objectDB.getObject(mapN, "RPG::Map");
        AppMain.objectDB.registerModificationHandler(map, listener);
        listener.accept(null);

        // begin!
        reinitLayerVis();

        renderer = AppMain.system.rendererFromMap(map);
        tileSize = renderer.tileRenderer.getTileSize();

        showTile(mapTable.width / 2, mapTable.height / 2);
    }

    public void reinitLayerVis() {
        if (layerVis != null)
            if (layerVis.length == renderer.layers.length)
                return;
        layerVis = new boolean[renderer.layers.length];
        for (int j = 0; j < layerVis.length; j++)
            layerVis[j] = !(renderer.layers[j] instanceof PassabilityMapViewDrawLayer);
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

        int mouseXT = UIElement.sensibleCellDiv((igd.getMouseX() - ox) + (int) (camX * internalScaling), eTileSize * internalScaling);
        int mouseYT = UIElement.sensibleCellDiv((igd.getMouseY() - oy) + (int) (camY * internalScaling), eTileSize * internalScaling);
        Rect camR = getBounds();
        camR = new Rect(0, 0, (camR.width / internalScaling) + ((camR.width % internalScaling != 0) ? 1 : 0), (camR.height / internalScaling) + ((camR.height % internalScaling != 0) ? 1 : 0));
        // Stuff any possible important information...
        char[] visConfig = new char[layerVis.length];
        for (int i = 0; i < layerVis.length; i++)
            visConfig[i] = layerVis[i] ? 'T' : 'F';
        String config = camR.width + "_" + camR.height + "_" + camX + "_" + camY + "_" + mouseXT + "_" + mouseYT + "_" + eTileSize + "_" + debug + "_" + renderer.tileRenderer.getFrame() + "_" + renderer.hashCode() + "_" + currentLayer + "_" + new String(visConfig) + "_" + callbacks + "_" + internalScaling;
        if (scheduler.needsUpdate(config)) {
            boolean remakeBuf = true;
            if (offscreenBuf != null)
                if ((offscreenBuf.getWidth() == camR.width) && (offscreenBuf.getHeight() == camR.height))
                    remakeBuf = false;
            if (remakeBuf) {
                if (offscreenBuf != null)
                    offscreenBuf.shutdown();
                offscreenBuf = GaBIEn.makeOffscreenBuffer(camR.width, camR.height, false);
            }
            render(mouseXT, mouseYT, eTileSize, currentLayer, debug, offscreenBuf);
        }
        if (offscreenBuf != null) {
            if (internalScaling == 1) {
                igd.blitImage(0, 0, camR.width, camR.height, ox, oy, offscreenBuf);
            } else {
                // Unworkable due to 'damage' caused by ScissorGrInDriver.
                // Either composite into a mapview-sized buffer before doing final unscaled subpixel pan, change scissoring, or don't bother
                //int spox = (int) ((camX - Math.floor(camX)) * internalScaling);
                //int spoy = (int) ((camY - Math.floor(camY)) * internalScaling);
                igd.blitScaledImage(0, 0, camR.width, camR.height, ox, oy, camR.width * internalScaling, camR.height * internalScaling, offscreenBuf);
            }
        }
        String shortcuts = TXDB.get("Mouse drag: Scroll, Shift-left: Pick tile.");
        if (callbacks != null) {
            shortcuts = TXDB.get("LMB: Use tool, others: Scroll, Shift-left: Pick tile.");
            if (pickTileHelper == null)
                shortcuts = TXDB.get("LMB: Use tool, others: Scroll.");
        } else {
            if (pickTileHelper == null)
                shortcuts = TXDB.get("Mouse drag: Scroll.");
        }
        String status = mapId + ";" + mouseXT + ", " + mouseYT + "; " + shortcuts;

        Rect plusRect = getZPlusRect();
        Rect minusRect = getZMinusRect();
        UILabel.drawLabel(igd, UILabel.getRecommendedSize(status, FontSizes.mapPositionTextHeight).width, ox + plusRect.x + plusRect.width + getZoomButtonMargin(), oy + plusRect.y, status, false, FontSizes.mapPositionTextHeight);
        Art.drawZoom(igd, true, ox + plusRect.x, oy + plusRect.y, plusRect.height);
        Art.drawZoom(igd, false, ox + minusRect.x, oy + minusRect.y, minusRect.height);
    }

    public void render(int mouseXT, int mouseYT, int eTileSize, int currentLayer, boolean debug, IGrDriver igd) {
        // The offscreen image implicitly crops.
        igd.clearAll(0, 0, 0);
        IMapViewDrawLayer[] layers = renderer.layers;
        int camTR = UIElement.sensibleCellDiv((int) (camX + igd.getWidth()), eTileSize) + 1;
        int camTB = UIElement.sensibleCellDiv((int) (camY + igd.getHeight()), eTileSize) + 1;
        int camTX = UIElement.sensibleCellDiv((int) camX, eTileSize);
        int camTY = UIElement.sensibleCellDiv((int) camY, eTileSize);
        for (int i = 0; i < layers.length; i++)
            if (layerVis[i])
                layers[i].draw((int) camX, (int) camY, camTX, camTY, camTR, camTB, mouseXT, mouseYT, eTileSize, currentLayer, callbacks, debug, igd);

        if (callbacks != null) {
            int ovlLayers = callbacks.wantOverlay(minimap);
            for (int l = 0; l < ovlLayers; l++) {
                for (int i = camTX; i < camTR; i++) {
                    for (int j = camTY; j < camTB; j++) {
                        int px = i * eTileSize;
                        int py = j * eTileSize;
                        px -= (int) camX;
                        py -= (int) camY;
                        // Keeping in mind px/py is the TL corner...
                        px += eTileSize / 2;
                        px -= tileSize / 2;
                        py += eTileSize / 2;
                        py -= tileSize / 2;
                        callbacks.performOverlay(i, j, igd, px, py, l, minimap);
                    }
                }
                callbacks.performGlobalOverlay(igd, - ((int) camX), - ((int) camY), l, minimap, eTileSize);
            }
        }
    }

    // These can be adjusted later if there's a need to alter the UI
    // Right now it's tied to map position text height, which is the most reliable metric of what the user wants the UI to be
    public int getZoomButtonSize() {
        return UILabel.getRecommendedSize("", FontSizes.mapPositionTextHeight).height;
    }

    public int getZoomButtonMargin() {
        return 4;
    }

    private Rect getZPlusRect() {
        int zbs = getZoomButtonSize();
        int zbm = getZoomButtonMargin();
        return new Rect(zbm, zbm, zbs, zbs);
    }

    private Rect getZMinusRect() {
        int zbs = getZoomButtonSize();
        int zbm = getZoomButtonMargin();
        return new Rect(zbm, (zbm * 2) + zbs, zbs, zbs);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        lmX = x;
        lmY = y;
        // Zoom is mousewheel emulation.
        if (getZPlusRect().contains(x, y)) {
            dragging = false;
            handleMousewheel(x, y, true);
            return;
        }
        if (getZMinusRect().contains(x, y)) {
            dragging = false;
            handleMousewheel(x, y, false);
            return;
        }
        if (button != 1) {
            dragging = true;
        } else {
            // implicit support for one-button mice
            if (!minimap) {
                int mouseXT = UIElement.sensibleCellDiv((int) ((x / internalScaling) + camX), tileSize);
                int mouseYT = UIElement.sensibleCellDiv((int) ((y / internalScaling) + camY), tileSize);
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
        if (getZPlusRect().contains(x, y))
            return;
        if (getZMinusRect().contains(x, y))
            return;
        if (dragging) {
            camX -= (x - lmX) / (double) internalScaling;
            camY -= (y - lmY) / (double) internalScaling;
            lmX = x;
            lmY = y;
        } else if (!minimap) {
            int mouseXT = UIElement.sensibleCellDiv((int) ((x / internalScaling) + camX), tileSize);
            int mouseYT = UIElement.sensibleCellDiv((int) ((y / internalScaling) + camY), tileSize);
            if (!shiftDown)
                if (callbacks != null)
                    if (!callbacks.shouldIgnoreDrag())
                        callbacks.confirmAt(mouseXT, mouseYT, currentLayer);
        }
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        Rect camR = getBounds();
        camX += camR.width / (internalScaling * 2.0d);
        camY += camR.height / (internalScaling * 2.0d);
        if (north) {
            internalScaling++;
        } else if (internalScaling > 1) {
            internalScaling--;
        }
        camX -= camR.width / (internalScaling * 2.0d);
        camY -= camR.height / (internalScaling * 2.0d);
    }

    public void toggleMinimap() {
        Rect camR = getBounds();
        double camW = camR.width / (double) internalScaling;
        double camH = camR.height / (double) internalScaling;

        camX += camW / 2;
        camY += camH / 2;
        if (minimap) {
            camX *= tileSize / 2d;
            camY *= tileSize / 2d;
        } else {
            camX /= tileSize / 2d;
            camY /= tileSize / 2d;
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
        AppMain.objectDB.objectRootModified(map, new SchemaPath(AppMain.schemas.getSDBEntry("RPG::Map"), map));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // Unfortunately this does have to happen sometimes, specifically for embedded UIMapView instances,
        //  so don't put up a warning about it.
        // In the *general* (main map view) case, AppMain manages this properly.
        if (offscreenBuf != null)
            offscreenBuf.shutdown();
    }

    public void freeOsbResources() {
        if (offscreenBuf != null) {
            offscreenBuf.shutdown();
            offscreenBuf = null;
        }
    }

    public void showTile(int x, int y) {
        Rect b = getBounds();
        camX = -b.width / (2d * internalScaling);
        camY = -b.height / (2d * internalScaling);
        int effectiveSize = minimap ? 2 : tileSize;
        camX += effectiveSize * x;
        camY += effectiveSize * y;
    }
}

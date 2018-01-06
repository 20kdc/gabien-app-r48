/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.drawlayers.PassabilityMapViewDrawLayer;
import r48.map.systems.MapSystem;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

/**
 * Note: this class will slightly draw out of bounds.
 * Created on 12/27/16.
 */
public class UIMapView extends UIElement implements IWindowElement {
    // This is drawn within the tile view. I.E. It's in content-pixels, not device-pixels
    public static final int mapDebugTextHeight = 6;

    // NOTE: camX/camY is negated display offset, from TL corner.
    // Also note that they are adjusted to internal scale.
    private double camX, camY;
    // System scale mouse values.
    private int lmX, lmY;

    // useful on mobile to allow dragging camera when using any tool
    private boolean camDragSwitch = false;

    private boolean dragging = false;

    // controlled from UIMTPopupButtons
    public boolean debugToggle = false;

    public final MapSystem.MapViewDetails map;
    public final String mapGUM;
    public MapSystem.MapViewState mapTable;
    // Set by the layer tabs UI
    public int currentLayer;

    // internal scaling is a UIMapView-controlled thing, for good reason.
    // should start off equal to internalScalingDiv
    private int internalScalingMul = 1;
    private int internalScalingDiv = 1;
    // Zoom fractions are deliberately converted to this, corrected, and then converted back.
    // Do not set above 8 or possibly 4, can be harmful on large screens.
    private final int tuningZoomWorkingDiv = 4;

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
            mapTable = map.rendererRetriever.get();
            reinitLayerVis();
            scheduler.forceNextUpdate = true;
        }
    };
    public boolean[] layerVis;

    public UIMapView(String mapN, int i, int i1) {
        setBounds(new Rect(0, 0, i, i1));

        map = AppMain.system.mapViewRequest(mapN, true);
        mapGUM = mapN;
        AppMain.objectDB.registerModificationHandler(map.object, listener);
        listener.accept(null);

        // begin!
        reinitLayerVis();

        tileSize = mapTable.renderer.tileRenderer.getTileSize();
        showTile(mapTable.width / 2, mapTable.height / 2);
    }

    private int internalScaling(int i) {
        return (i * internalScalingMul) / internalScalingDiv;
    }

    private double internalScaling(double i) {
        return (i * internalScalingMul) / internalScalingDiv;
    }

    private int internalScalingReverse(int i) {
        return ((i * internalScalingDiv) / internalScalingMul);
    }

    private double internalScalingReverse(double i) {
        return ((i * internalScalingDiv) / internalScalingMul);
    }

    private int internalScalingReversePad(int i) {
        int m = (i * internalScalingDiv);
        return (m / internalScalingMul) + (((m % internalScalingMul) != 0) ? 1 : 0);
    }



    public void reinitLayerVis() {
        if (layerVis != null)
            if (layerVis.length == mapTable.renderer.layers.length)
                return;
        layerVis = new boolean[mapTable.renderer.layers.length];
        for (int j = 0; j < layerVis.length; j++)
            layerVis[j] = !(mapTable.renderer.layers[j] instanceof PassabilityMapViewDrawLayer);
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        shiftDown = igd.isKeyDown(IGrInDriver.VK_SHIFT);

        int mouseXT = UIElement.sensibleCellDiv((igd.getMouseX() - ox) + (int) internalScaling(camX), internalScaling(tileSize));
        int mouseYT = UIElement.sensibleCellDiv((igd.getMouseY() - oy) + (int) internalScaling(camY), internalScaling(tileSize));
        Rect camR = getBounds();
        camR = new Rect(0, 0, internalScalingReversePad(camR.width), internalScalingReversePad(camR.height));
        // Stuff any possible important information...
        char[] visConfig = new char[layerVis.length];
        for (int i = 0; i < layerVis.length; i++)
            visConfig[i] = layerVis[i] ? 'T' : 'F';
        String config = camR.width + "_" + camR.height + "_" + camX + "_" + camY + "_" + mouseXT + "_" + mouseYT + "_" + debugToggle + "_" + mapTable.renderer.tileRenderer.getFrame() + "_" + mapTable.hashCode() + "_" + currentLayer + "_" + new String(visConfig) + "_" + callbacks + "_" + internalScalingMul + "_" + internalScalingDiv;
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
            render(mouseXT, mouseYT, currentLayer, offscreenBuf);
        }
        if (offscreenBuf != null) {
            if ((internalScalingMul == 1) && (internalScalingDiv == 1)) {
                igd.blitImage(0, 0, camR.width, camR.height, ox, oy, offscreenBuf);
            } else {
                igd.blitScaledImage(0, 0, camR.width, camR.height, ox, oy, internalScaling(camR.width), internalScaling(camR.height), offscreenBuf);
            }
        }
        boolean dedicatedDragControl = useDragControl();
        String shortcuts = TXDB.get("Mouse drag: Scroll, Shift-left: Pick tile.");
        if (dedicatedDragControl)
            shortcuts = TXDB.get("Drag scrolls about.");
        if (callbacks != null) {
            shortcuts = TXDB.get("LMB: Use tool, others: Scroll, Shift-left: Pick tile.");
            if (pickTileHelper == null)
                shortcuts = TXDB.get("LMB: Use tool, others: Scroll.");
            if (dedicatedDragControl)
                shortcuts = TXDB.get("Tap/Drag: Use tool. Camera button: Scroll.");
            if (camDragSwitch)
                shortcuts = TXDB.get("Dragging scrolls about. Camera button: Return.");
        } else {
            if (pickTileHelper == null)
                shortcuts = TXDB.get("Mouse drag: Scroll.");
        }
        String status = mapGUM + ";" + mouseXT + ", " + mouseYT + " Z" + internalScalingMul + ":" + internalScalingDiv + "; " + shortcuts;

        Rect plusRect = Art.getZIconRect(false, 0);
        Rect plusRectFull = Art.getZIconRect(true, 0); // used for X calc on the label
        Rect minusRect = Art.getZIconRect(false, 1);
        Rect dragRect = Art.getZIconRect(false, 2);
        UILabel.drawLabel(igd, UILabel.getRecommendedSize(status, FontSizes.mapPositionTextHeight).width, ox + plusRectFull.x + plusRectFull.width, oy + plusRect.y, status, 0, FontSizes.mapPositionTextHeight);
        Art.drawZoom(igd, true, ox + plusRect.x, oy + plusRect.y, plusRect.height);
        Art.drawZoom(igd, false, ox + minusRect.x, oy + minusRect.y, minusRect.height);
        if (dedicatedDragControl)
            Art.drawDragControl(igd, camDragSwitch, ox + dragRect.x, oy + dragRect.y, minusRect.height);
    }

    private boolean useDragControl() {
        return GaBIEn.singleWindowApp() || camDragSwitch; // SWA means "make sure the user can use a 1-button mouse w/no hover".
    }

    public void renderCore(IGrDriver igd, int vCX, int vCY) {
        IMapViewDrawLayer[] layers = mapTable.renderer.layers;
        int camTR = UIElement.sensibleCellDiv(vCX + igd.getWidth(), tileSize) + 1;
        int camTB = UIElement.sensibleCellDiv(vCY + igd.getHeight(), tileSize) + 1;
        int camTX = UIElement.sensibleCellDiv(vCX, tileSize);
        int camTY = UIElement.sensibleCellDiv(vCY, tileSize);
        // mouse position constants specifically chosen to reduce chance of overlap
        for (int i = 0; i < layers.length; i++)
            if (layerVis[i])
                layers[i].draw(vCX, vCY, camTX, camTY, camTR, camTB, 0xC0000000, 0xC0000000, tileSize, currentLayer, null, debugToggle, igd);
    }

    private void render(int mouseXT, int mouseYT, int currentLayer, IGrDriver igd) {
        // The offscreen image implicitly crops.
        igd.clearAll(0, 0, 0);
        IMapViewDrawLayer[] layers = mapTable.renderer.layers;
        int camTR = UIElement.sensibleCellDiv((int) (camX + igd.getWidth()), tileSize) + 1;
        int camTB = UIElement.sensibleCellDiv((int) (camY + igd.getHeight()), tileSize) + 1;
        int camTX = UIElement.sensibleCellDiv((int) camX, tileSize);
        int camTY = UIElement.sensibleCellDiv((int) camY, tileSize);
        for (int i = 0; i < layers.length; i++)
            if (layerVis[i])
                layers[i].draw((int) camX, (int) camY, camTX, camTY, camTR, camTB, mouseXT, mouseYT, tileSize, currentLayer, callbacks, debugToggle, igd);

        boolean minimap = internalScalingDiv > 1;
        if (callbacks != null) {
            int ovlLayers = callbacks.wantOverlay(minimap);
            for (int l = 0; l < ovlLayers; l++) {
                for (int i = camTX; i < camTR; i++) {
                    for (int j = camTY; j < camTB; j++) {
                        int px = i * tileSize;
                        int py = j * tileSize;
                        px -= (int) camX;
                        py -= (int) camY;
                        callbacks.performOverlay(i, j, igd, px, py, l, minimap);
                    }
                }
                callbacks.performGlobalOverlay(igd, -((int) camX), -((int) camY), l, minimap, tileSize);
            }
        }
    }

    @Override
    public void handleClick(int x, int y, int button) {
        lmX = x;
        lmY = y;
        // Zoom is mousewheel emulation.
        if (Art.getZIconRect(true, 0).contains(x, y)) {
            dragging = false;
            handleMousewheel(x, y, true);
            return;
        }
        if (Art.getZIconRect(true, 1).contains(x, y)) {
            dragging = false;
            handleMousewheel(x, y, false);
            return;
        }
        if (useDragControl()) {
            if (Art.getZIconRect(true, 2).contains(x, y)) {
                dragging = false;
                camDragSwitch = !camDragSwitch;
                return;
            }
        }
        if (button != 1) {
            dragging = true;
        } else {
            // implicit support for one-button mice
            int mouseXT = UIElement.sensibleCellDiv((int) (internalScalingReverse(x) + camX), tileSize);
            int mouseYT = UIElement.sensibleCellDiv((int) (internalScalingReverse(y) + camY), tileSize);
            dragging = false;
            if (shiftDown) {
                if (!mapTable.outOfBounds(mouseXT, mouseYT))
                    if (pickTileHelper != null)
                        pickTileHelper.accept(mapTable.getTileData.apply(new int[] {mouseXT, mouseYT, currentLayer}));
            } else if ((callbacks != null) && (!camDragSwitch)) {
                    callbacks.confirmAt(mouseXT, mouseYT, currentLayer);
            } else {
                    dragging = true;
            }
        }
    }

    @Override
    public void handleDrag(int x, int y) {
        if (dragging) {
            camX -= internalScalingReverse((double) (x - lmX));
            camY -= internalScalingReverse((double) (y - lmY));
            lmX = x;
            lmY = y;
        } else {
            int mouseXT = UIElement.sensibleCellDiv((int) (internalScalingReverse(x) + camX), tileSize);
            int mouseYT = UIElement.sensibleCellDiv((int) (internalScalingReverse(y) + camY), tileSize);
            if (!shiftDown)
                if (callbacks != null)
                    if (!callbacks.shouldIgnoreDrag())
                        callbacks.confirmAt(mouseXT, mouseYT, currentLayer);
        }
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        Rect camR = getBounds();
        camX += camR.width / internalScaling(2.0d);
        camY += camR.height / internalScaling(2.0d);
        // Firstly, convert fraction to tuningZoomWorkingDiv
        internalScalingMul = (internalScalingMul * tuningZoomWorkingDiv) / internalScalingDiv;
        if (internalScalingMul < 1)
            internalScalingMul = 1;
        internalScalingDiv = tuningZoomWorkingDiv;
        // Secondly, zoom
        if (north) {
            // Zoom in.
            internalScalingMul++;
        } else {
            // Zoom out.
            internalScalingMul--;
            if (internalScalingMul < 1)
                internalScalingMul = 1;
        }
        // Thirdly, convert back. Basically, try to see if both parts of the fraction are divisible by some number.
        // This does an okay job - it always gets the major fractions right.
        // 1:2/1:4/3:4/1:1/5:4/3:2/7:4/2:1/9:4/5:2/11:4/3:1.
        for (int i = internalScalingDiv; i > 1; i--) {
            if ((internalScalingMul % i) == 0) {
                if ((internalScalingDiv % i) == 0) {
                    // There's an equivalent fraction, with both parameters divided by i.
                    internalScalingMul /= i;
                    internalScalingDiv /= i;
                    break;
                }
            }
        }
        camX -= camR.width / internalScaling(2.0d);
        camY -= camR.height / internalScaling(2.0d);
    }

    @Override
    public boolean wantsSelfClose() {
        return false;
    }

    @Override
    public void windowClosed() {
        AppMain.objectDB.deregisterModificationHandler(map.object, listener);
    }

    // Used by tools, after they're done doing whatever.
    // Basically a convenience method.
    public void passModificationNotification() {
        AppMain.objectDB.objectRootModified(map.object, new SchemaPath(AppMain.schemas.getSDBEntry(map.objectSchema), map.object));
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
        camX = -b.width / internalScaling(2.0d);
        camY = -b.height / internalScaling(2.0d);
        camX += tileSize * x;
        camY += tileSize * y;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map;

import gabien.*;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.systems.MapSystem;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

/**
 * The beginning.
 * Created on 12/27/16.
 */
public class UIMapView extends UIElement implements OldMouseEmulator.IOldMouseReceiver {
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
    private final int tuningZoomWorkingDivCtrl = 1;

    public boolean ctrlDown = false;
    public boolean shiftDown = false;
    public IMapViewCallbacks callbacks;
    // Responsible for starting a tool with the given tile.
    public IConsumer<Short> pickTileHelper = null;

    public final int tileSize;

    private MapViewUpdateScheduler scheduler = new MapViewUpdateScheduler();
    // Managed using finalize for now.
    private IGrDriver offscreenBuf;
    public OldMouseEmulator mouseEmulator;
    public UILabel.StatusLine statusLine = new UILabel.StatusLine();

    private IConsumer<SchemaPath> listener = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            performRefresh(AppMain.objectDB.getIdByObject(sp.findRoot().targetElement));
        }
    };
    private String[] listenAdditionals = new String[0];

    public void performRefresh(String cause) {
        // Not an incredibly high-cost operation, thankfully,
        //  since it'll have to run on any edits.
        for (String s : listenAdditionals)
            if (!map.objectId.equals(s))
                AppMain.objectDB.deregisterModificationHandler(s, listener);
        mapTable = map.rendererRetriever.apply(cause);
        listenAdditionals = mapTable.refreshOnObjectChange;
        for (String s : listenAdditionals)
            if (!map.objectId.equals(s))
                AppMain.objectDB.registerModificationHandler(s, listener);
        reinitLayerVis();
        scheduler.forceNextUpdate = true;
    }

    public boolean[] layerVis;

    public UIMapView(String mapN, int i, int i1) {
        mouseEmulator = new OldMouseEmulator(this);
        Rect fakeWorldRect = new Rect(0, 0, i, i1);
        setWantedSize(fakeWorldRect);
        setForcedBounds(null, fakeWorldRect);

        map = AppMain.system.mapViewRequest(mapN, true);
        mapGUM = mapN;
        AppMain.objectDB.registerModificationHandler(map.object, listener);
        performRefresh(null);

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
        System.arraycopy(mapTable.renderer.activeDef, 0, layerVis, 0, mapTable.renderer.activeDef.length);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        shiftDown = false;
        ctrlDown = false;
        if (peripherals instanceof IDesktopPeripherals) {
            mouseEmulator.mouseX = ((IDesktopPeripherals) peripherals).getMouseX();
            mouseEmulator.mouseY = ((IDesktopPeripherals) peripherals).getMouseY();
            shiftDown = ((IDesktopPeripherals) peripherals).isKeyDown(IGrInDriver.VK_SHIFT);
            ctrlDown = ((IDesktopPeripherals) peripherals).isKeyDown(IGrInDriver.VK_CONTROL);
        }
    }

    @Override
    public void render(IGrDriver igd) {
        int mouseXT = UIElement.sensibleCellDiv(mouseEmulator.mouseX + (int) internalScaling(camX), internalScaling(tileSize));
        int mouseYT = UIElement.sensibleCellDiv(mouseEmulator.mouseY + (int) internalScaling(camY), internalScaling(tileSize));
        Size camR = getSize();
        camR = new Size(internalScalingReversePad(camR.width), internalScalingReversePad(camR.height));
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
                igd.blitImage(0, 0, camR.width, camR.height, 0, 0, offscreenBuf);
            } else {
                igd.blitScaledImage(0, 0, camR.width, camR.height, 0, 0, internalScaling(camR.width), internalScaling(camR.height), offscreenBuf);
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

        int textX = plusRectFull.x + plusRectFull.width;
        int textW = getSize().width - (textX + ((plusRectFull.width - plusRect.width) / 2));
        statusLine.draw(status, FontSizes.mapPositionTextHeight, igd, textX, plusRect.y, textW);

        Art.drawZoom(igd, true, plusRect.x, plusRect.y, plusRect.height);
        Art.drawZoom(igd, false, minusRect.x, minusRect.y, minusRect.height);
        if (dedicatedDragControl)
            Art.drawDragControl(igd, camDragSwitch, dragRect.x, dragRect.y, minusRect.height);
    }

    private boolean useDragControl() {
        return GaBIEn.singleWindowApp() || camDragSwitch; // SWA means "make sure the user can use a 1-button mouse w/no hover".
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
    public void handlePointerBegin(IPointer state) {
        mouseEmulator.handlePointerBegin(state);
    }

    @Override
    public void handlePointerUpdate(IPointer state) {
        mouseEmulator.handlePointerUpdate(state);
    }

    @Override
    public void handlePointerEnd(IPointer state) {
        mouseEmulator.handlePointerEnd(state);
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
    public void handleRelease(int x, int y) {

    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        Size camR = getSize();
        camX += camR.width / internalScaling(2.0d);
        camY += camR.height / internalScaling(2.0d);
        int zwd = tuningZoomWorkingDiv;
        if (ctrlDown)
            zwd = tuningZoomWorkingDivCtrl;
        // Firstly, convert fraction to tuningZoomWorkingDiv
        internalScalingMul = (internalScalingMul * zwd) / internalScalingDiv;
        if (internalScalingMul < 1)
            internalScalingMul = 1;
        internalScalingDiv = zwd;
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
    public void handleRootDisconnect() {
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
        Size b = getSize();
        camX = -b.width / internalScaling(2.0d);
        camY = -b.height / internalScaling(2.0d);
        camX += tileSize * x;
        camY += tileSize * y;
    }
}

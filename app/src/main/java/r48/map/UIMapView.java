/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.*;
import gabien.uslx.append.*;
import gabien.ui.*;
import r48.App;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.systems.MapSystem;
import r48.schema.util.SchemaPath;
import r48.ui.UIPlaneView;

/**
 * The beginning.
 * Created on 12/27/16.
 */
public class UIMapView extends UIPlaneView {
    // This is drawn within the tile view. I.E. It's in content-pixels, not device-pixels
    public static final int mapDebugTextHeight = 6;

    // useful on mobile to allow dragging camera when using any tool
    private boolean camDragSwitch = false;

    // controlled from UIMTPopupButtons
    public boolean debugToggle = false;

    public final MapSystem.MapViewDetails map;
    public final String mapGUM;
    public MapSystem.MapViewState mapTable;
    // Set by the layer tabs UI
    public int currentLayer;

    // Zoom fractions are deliberately converted to this, corrected, and then converted back.
    // Do not set above 8 or possibly 4, can be harmful on large screens.
    private final int tuningZoomWorkingDiv = 4;
    private final int tuningZoomWorkingDivCtrl = 1;

    public boolean ctrlDown = false;
    public boolean shiftDown = false;
    public IMapViewCallbacks callbacks;
    // Responsible for starting a tool with the given tile.
    public IConsumer<Short> pickTileHelper = null;

    /**
     * set from UIMapViewContainer 
     */
    public boolean viewRenderDisableSwitch = false;

    public final int tileSize;

    private MapViewUpdateScheduler scheduler = new MapViewUpdateScheduler();
    // Managed using finalize for now.
    private IGrDriver offscreenBuf;
    private int mouseXT, mouseYT;
    private int mouseXTP, mouseYTP;

    // Used to control shouldDrawAt visuals
    private boolean visCurrentlyDrawing = false;

    // Regarding how these now work:
    // Modification listeners have to be held by the things that need to be notified.
    // This way, they conveniently disappear when the notified things do.
    private IConsumer<SchemaPath> listener = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            performRefresh(app.odb.getIdByObject(sp.root));
        }
    };

    private String[] listenAdditionals = new String[0];

    public void performRefresh(String cause) {
        // Not an incredibly high-cost operation, thankfully,
        //  since it'll have to run on any edits.
        for (String s : listenAdditionals)
            if (!map.objectId.equals(s))
                app.odb.deregisterModificationHandler(s, listener);
        mapTable = map.rendererRetriever.apply(cause);
        listenAdditionals = mapTable.refreshOnObjectChange;
        for (String s : listenAdditionals)
            if (!map.objectId.equals(s))
                app.odb.registerModificationHandler(s, listener);
        reinitLayerVis();
        scheduler.forceNextUpdate = true;
    }

    public boolean[] layerVis;

    public UIMapView(App app, String mapN, int i, int i1) {
        super(app);
        Rect fakeWorldRect = new Rect(0, 0, i, i1);
        setWantedSize(fakeWorldRect);
        setForcedBounds(null, fakeWorldRect);

        map = app.system.mapViewRequest(mapN, true);
        mapGUM = mapN;
        app.odb.registerModificationHandler(map.objectId, listener);
        performRefresh(null);

        tileSize = mapTable.renderer.tileRenderer.getTileSize();
        showTile(mapTable.width / 2, mapTable.height / 2);
    }

    private void recalcXYT(int x, int y) {
        Size wSize = getSize();
        int xi = (int) Math.floor(camX + planeDivZoom(x - (wSize.width / 2.0d)));
        int yi = (int) Math.floor(camY + planeDivZoom(y - (wSize.height / 2.0d)));
        mouseXT = UIElement.sensibleCellDiv(xi, tileSize);
        mouseXTP = UIElement.sensibleCellMod(xi, tileSize);
        mouseYT = UIElement.sensibleCellDiv(yi, tileSize);
        mouseYTP = UIElement.sensibleCellMod(yi, tileSize);
    }

    private int internalScaling(int i) {
        return (i * planeZoomMul) / planeZoomDiv;
    }

    private int internalScalingReversePad(int i) {
        int m = (i * planeZoomDiv);
        return (m / planeZoomMul) + (((m % planeZoomMul) != 0) ? 1 : 0);
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
            recalcXYT(((IDesktopPeripherals) peripherals).getMouseX(), ((IDesktopPeripherals) peripherals).getMouseY());
            shiftDown = ((IDesktopPeripherals) peripherals).isKeyDown(IGrInDriver.VK_SHIFT);
            ctrlDown = ((IDesktopPeripherals) peripherals).isKeyDown(IGrInDriver.VK_CONTROL);
        }
    }

    @Override
    protected String planeGetStatus() {
        String shortcuts;
        if (camDragSwitch) {
            shortcuts = T.z.l258;
        } else if (callbacks == null) {
            if ((pickTileHelper != null) && (!app.ui.isMobile)) {
                shortcuts = T.z.l259;
            } else {
                shortcuts = T.z.l260;
            }
        } else {
            shortcuts = T.z.l261;
        }
        return mapGUM + ";" + mouseXT + ", " + mouseYT + " Z" + planeZoomDiv + ":" + planeZoomMul + "; " + shortcuts;
    }

    @Override
    protected boolean planeGetDragLock() {
        return camDragSwitch || ((callbacks == null) && (!shiftDown));
    }

    @Override
    protected void planeToggleDragLock() {
        if (planeGetDragLock()) {
            camDragSwitch = false;
        } else {
            camDragSwitch = true;
        }
    }

    @Override
    protected void planeZoomLogic(boolean north) {
        int zwd = tuningZoomWorkingDiv;
        if (shiftDown || ctrlDown)
            zwd = tuningZoomWorkingDivCtrl;
        // Firstly, convert fraction to tuningZoomWorkingDiv
        planeZoomMul = (planeZoomMul * zwd) / planeZoomDiv;
        if (planeZoomMul < 1)
            planeZoomMul = 1;
        planeZoomDiv = zwd;
        // Secondly, zoom
        if (north) {
            // Zoom in.
            planeZoomMul++;
        } else {
            // Zoom out.
            planeZoomMul--;
            if (planeZoomMul < 1)
                planeZoomMul = 1;
        }
        // Thirdly, convert back. Basically, try to see if both parts of the fraction are divisible by some number.
        // This does an okay job - it always gets the major fractions right.
        // 1:2/1:4/3:4/1:1/5:4/3:2/7:4/2:1/9:4/5:2/11:4/3:1.
        for (int i = planeZoomDiv; i > 1; i--) {
            if ((planeZoomMul % i) == 0) {
                if ((planeZoomDiv % i) == 0) {
                    // There's an equivalent fraction, with both parameters divided by i.
                    planeZoomMul /= i;
                    planeZoomDiv /= i;
                    break;
                }
            }
        }
    }

    @Override
    protected boolean planeCanZoom(boolean north) {
        return true;
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        recalcXYT(state.getX(), state.getY());
        return super.handleNewPointer(state);
    }

    @Override
    protected IPointerReceiver planeHandleDrawPointer(IPointer state) {
        return new IPointerReceiver() {
            @Override
            public void handlePointerBegin(IPointer state) {
                visCurrentlyDrawing = true;
                recalcXYT(state.getX(), state.getY());
                if (shiftDown) {
                    if (!mapTable.outOfBounds(mouseXT, mouseYT))
                        if (pickTileHelper != null)
                            pickTileHelper.accept(mapTable.getTileData.apply(new int[] {mouseXT, mouseYT, currentLayer}));
                } else if (callbacks != null) {
                    callbacks.confirmAt(mouseXT, mouseYT, mouseXTP, mouseYTP, currentLayer, true);
                }
            }

            @Override
            public void handlePointerUpdate(IPointer state) {
                recalcXYT(state.getX(), state.getY());
                if (!shiftDown)
                    if (callbacks != null)
                        callbacks.confirmAt(mouseXT, mouseYT, mouseXTP, mouseYTP, currentLayer, false);
            }

            @Override
            public void handlePointerEnd(IPointer state) {
                visCurrentlyDrawing = false;
            }
        };
    }

    @Override
    public void render(IGrDriver igd) {
        Size camR = getSize();
        camR = new Size(internalScalingReversePad(camR.width), internalScalingReversePad(camR.height));

        // Stuff any possible important information...
        char[] visConfig = new char[layerVis.length];
        for (int i = 0; i < layerVis.length; i++)
            visConfig[i] = layerVis[i] ? 'T' : 'F';
        String config = camR.width + "_" + camR.height + "_" + camX + "_" + camY + "_" + mouseXT + "_" + mouseYT + "_" + debugToggle + "_" + mapTable.renderer.tileRenderer.getFrame() + "_" + mapTable.hashCode() + "_" + currentLayer + "_" + new String(visConfig) + "_" + callbacks + "_" + planeZoomMul + "_" + planeZoomDiv + "_" + viewRenderDisableSwitch;
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
            render(currentLayer, offscreenBuf);
        }
        if (offscreenBuf != null) {
            if ((planeZoomMul == 1) && (planeZoomDiv == 1)) {
                igd.blitImage(0, 0, camR.width, camR.height, 0, 0, offscreenBuf);
            } else {
                igd.blitScaledImage(0, 0, camR.width, camR.height, 0, 0, internalScaling(camR.width), internalScaling(camR.height), offscreenBuf);
            }
        }

        super.render(igd);
    }

    // mousePT can be null
    private void render(int currentLayer, IGrDriver igd) {

        // Translate new camX/camY (planespace central coord) into old camX/camY (position of the top-left corner of the screen in planespace)
        Size wSize = getSize();
        int iCamX = (int) Math.floor(camX - planeDivZoom(wSize.width / 2.0d));
        int iCamY = (int) Math.floor(camY - planeDivZoom(wSize.height / 2.0d));

        MapViewDrawContext mvdc = new MapViewDrawContext(app, new Rect(iCamX, iCamY, igd.getWidth(), igd.getHeight()), tileSize);

        // The offscreen image implicitly crops.
        igd.clearAll(0, 0, 0);

        // Everything is performed in a transformed context to save some headaches.
        int[] stb = igd.getLocalST();
        int oldTX = stb[0];
        int oldTY = stb[1];
        stb[0] = -iCamX;
        stb[1] = -iCamY;
        igd.updateST();

        IMapViewDrawLayer[] layers = mapTable.renderer.layers;

        // NOTE: Block copy/paste isn't nice this way... add confirmation or something instead?
        // If so, make sure that camDragSwitch still disables this.
        mvdc.mouseStatus = app.ui.isMobile ? null : new MapViewDrawContext.MouseStatus(visCurrentlyDrawing, mouseXT, mouseYT);

        mvdc.callbacks = callbacks;
        mvdc.currentLayer = currentLayer;
        mvdc.debugToggle = debugToggle;
        mvdc.igd = igd;

        if (!viewRenderDisableSwitch)
            for (int i = 0; i < layers.length; i++)
                if (layerVis[i])
                    layers[i].draw(mvdc);

        boolean minimap = planeZoomDiv > 1;
        if (callbacks != null) {
            int ovlLayers = callbacks.wantOverlay(minimap);
            for (int l = 0; l < ovlLayers; l++)
                callbacks.performGlobalOverlay(mvdc, l, minimap);
        }
        stb[0] = oldTX;
        stb[1] = oldTY;
        igd.updateST();
    }

    // Used by tools, after they're done doing whatever.
    // Basically a convenience method.
    public void passModificationNotification() {
        app.odb.objectRootModified(map.object, new SchemaPath(app.sdb.getSDBEntry(map.objectSchema), map.object));
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
        camX = (x * tileSize) + (tileSize / 2.0);
        camY = (y * tileSize) + (tileSize / 2.0);
    }

    // Safe to pass null here.
    public static void performFullCacheFlush(@NonNull App app, @Nullable UIMapView view) {
        app.stuffRendererIndependent.imageLoader.flushCache();
        if (view != null) {
            view.mapTable.renderer.imageLoader.flushCache();
            view.performRefresh(null);
            view.mapTable.renderer.imageLoader.flushCache();
            view.reinitLayerVis();
        }
    }
}

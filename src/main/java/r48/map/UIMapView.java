/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.IWindowElement;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Note: this class will slightly draw out of bounds.
 * Created on 12/27/16.
 */
public class UIMapView extends UIElement implements IWindowElement {
    private int camX, camY, lmX, lmY;
    private int currentLayer = 0;
    private boolean[] layerInvisible;
    private boolean dragging = false;
    public final RubyIO map;
    public final String mapId;
    // This needs to be interchangable in case a table update happens...
    public RubyTable mapTable;
    public boolean minimap = false;
    public IMapViewCallbacks callbacks;

    public final int tileSize;

    private Runnable listener = new Runnable() {
        @Override
        public void run() {
            // Not an incredibly high-cost operation, thankfully,
            //  since it'll have to run on any edits.
            mapTable = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            AppMain.stuffRenderer = StuffRenderer.rendererFromMap(map);
        }
    };

    public UIMapView(String mapN, int i, int i1) {
        // This makes it load in the right spot.
        setBounds(new Rect(0, 0, i, i1));

        mapId = mapN;
        map = AppMain.objectDB.getObject(mapN, "RPG::Map");
        AppMain.objectDB.registerModificationHandler(map, listener);
        listener.run();
        layerInvisible = new boolean[mapTable.planeCount + 2];

        AppMain.stuffRenderer = StuffRenderer.rendererFromMap(map);
        tileSize = AppMain.stuffRenderer.tileRenderer.getTileSize();

        camX = tileSize * (mapTable.width / 2);
        camY = tileSize * (mapTable.height / 2);
        if (minimap) {
            camX /= (tileSize / 2);
            camY /= (tileSize / 2);
        }
        camX -= i / 2;
        camY -= i1 / 2;
    }

    public Rect getLayerTabRect(int i) {
        return new Rect(i * 18, getBounds().height - 18, 18, 18);
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        // This assumes it's the background element for now - no cropping... :(
        // Ok, decode Table
        int w = mapTable.width;
        int h = mapTable.height;
        boolean debug = igd.isKeyDown(IGrInDriver.VK_D);
        int eTileSize = tileSize;
        if (minimap)
            eTileSize = 2;

        int mouseXT = UIElement.sensibleCellDiv((igd.getMouseX() - ox) + camX, eTileSize);
        int mouseYT = UIElement.sensibleCellDiv((igd.getMouseY() - oy) + camY, eTileSize);

        Rect camR = getBounds();
        int camW = camR.width;
        int camH = camR.height;

        int camTR = UIElement.sensibleCellDiv((camX + camW), eTileSize) + 1;
        int camTB = UIElement.sensibleCellDiv((camY + camH), eTileSize) + 1;
        int camTX = UIElement.sensibleCellDiv(camX, eTileSize);
        int camTY = UIElement.sensibleCellDiv(camY, eTileSize);

        if (!layerInvisible[mapTable.planeCount]) {
            // Panorama Enable
            String panorama = AppMain.stuffRenderer.tileRenderer.getPanorama();
            if (panorama.length() > 0) {
                IGrInDriver.IImage im = AppMain.stuffRenderer.imageLoader.getImage(panorama, 0, 0, 0);
                // Need to tile the area with the image.
                // I give up, this is what I've got now.
                // It works better this way than the other way under some cases.
                int eCamX = camX;
                int eCamY = camY;
                //eCamX -= im.getWidth() / 4;
                //eCamY -= im.getHeight() / 4;
                int camOTX = sensibleCellDiv(eCamX, im.getWidth());
                int camOTY = sensibleCellDiv(eCamY, im.getHeight());
                int camOTeX = sensibleCellDiv(eCamX + camW, im.getWidth()) + 1;
                int camOTeY = sensibleCellDiv(eCamY + camH, im.getHeight()) + 1;
                for (int i = camOTX; i <= camOTeX; i++)
                    for (int j = camOTY; j <= camOTeY; j++)
                        igd.blitImage(0, 0, im.getWidth(), im.getHeight(), ox + (i * im.getWidth()) - eCamX, oy + (j * im.getHeight()) - eCamY, im);
            }
        }

        int[] layerOrder = AppMain.stuffRenderer.tileRenderer.tileLayerDrawOrder();
        for (int li = 0; li < layerOrder.length; li++) {
            int l = layerOrder[li];
            if (!layerInvisible[l]) {
                for (int i = camTX; i < camTR; i++) {
                    if (i < 0)
                        continue;
                    if (i >= w)
                        continue;
                    for (int j = camTY; j < camTB; j++) {
                        if (j < 0)
                            continue;
                        if (j >= h)
                            continue;
                        int px = i * eTileSize;
                        int py = j * eTileSize;
                        px = (ox + px) - camX;
                        py = (oy + py) - camY;
                        // 5, 26-29: cafe main bar. In DEBUG, shows as 255, 25d, 25d, ???, I think.
                        // 1c8 >> 3 == 39.
                        // 39 in binary is 00111001.
                        // Possible offset of 1?
                        if (debug) {
                            String t = Integer.toString(mapTable.getTiletype(i, j, li), 16);
                            UILabel.drawString(igd, px, py + (li * FontSizes.mapDebugTextHeight), t, false, FontSizes.mapDebugTextHeight);
                        } else {
                            short tidx = mapTable.getTiletype(i, j, l);
                            if (i == mouseXT)
                                if (j == mouseYT)
                                    tidx = callbacks.shouldDrawAtCursor(tidx, l, currentLayer);
                            AppMain.stuffRenderer.tileRenderer.drawTile(l, tidx, px, py, igd, eTileSize);
                        }
                    }
                }
            }
            drawEventLayer(ox, oy, igd, camTX, camTY, camTR, camTB, li);
        }

        int layers = callbacks.wantOverlay(minimap);
        for (int l = 0; l < layers; l++) {
            for (int i = camTX; i < camTR; i++) {
                for (int j = camTY; j < camTB; j++) {
                    int px = i * eTileSize;
                    int py = j * eTileSize;
                    px = (ox + px) - camX;
                    py = (oy + py) - camY;
                    // Keeping in mind px/py is the TL corner...
                    px += eTileSize / 2;
                    px -= tileSize / 2;
                    py += eTileSize / 2;
                    py -= tileSize / 2;
                    callbacks.performOverlay(i, j, igd, px, py, l, minimap);
                }
            }
        }
        if (selected)
            if (igd.isKeyJustPressed(IGrInDriver.VK_M))
                toggleMinimap();
        for (int i = 0; i < layerInvisible.length; i++) {
            Rect l = getLayerTabRect(i);
            igd.blitImage((i == currentLayer) ? 0 : 18, layerInvisible[i] ? 0 : 18, 18, 18, ox + l.x, oy + l.y, AppMain.layerTabs);
            String text = Integer.toString(i);
            if (i == mapTable.planeCount)
                text = "P";
            if (i == mapTable.planeCount + 1)
                text = "E";
            // Notably, this gets to cheat because it fits into an image
            UILabel.drawString(igd, ox + l.x + 1, oy + l.y + 1, text, true, 8);
        }
        UILabel.drawLabel(igd, 0, ox, oy, "Map" + mapId + ";" + mouseXT + ", " + mouseYT, false, FontSizes.mapPositionTextHeight);
    }

    private void drawEventLayer(int ox, int oy, IGrInDriver igd, int camTX, int camTY, int camTR, int camTB, int l) {
        if ((!layerInvisible[mapTable.planeCount + 1]) && (!minimap)) {
            // Event Enable
            // Having it here is more efficient than having it as a tool overlay,
            // and sometimes the user might want to see events when using other tools.
            LinkedList<RubyIO> ev = new LinkedList<RubyIO>(map.getInstVarBySymbol("@events").hashVal.values());
            ev.sort(new Comparator<RubyIO>() {
                @Override
                public int compare(RubyIO a, RubyIO b) {
                    int yA = (int) a.getInstVarBySymbol("@y").fixnumVal;
                    int yB = (int) b.getInstVarBySymbol("@y").fixnumVal;
                    if (yA < yB)
                        return -1;
                    if (yA > yB)
                        return 1;
                    return 0;
                }
            });
            for (RubyIO evI : ev) {
                if (AppMain.stuffRenderer.eventRenderer.determineEventLayer(evI) != l)
                    continue;
                int x = (int) evI.getInstVarBySymbol("@x").fixnumVal;
                int y = (int) evI.getInstVarBySymbol("@y").fixnumVal;
                if (x < camTX)
                    continue;
                if (y < camTY)
                    continue;
                if (x >= camTR)
                    continue;
                if (y >= camTB)
                    continue;
                int px = ox + ((x * tileSize) - camX);
                int py = oy + ((y * tileSize) - camY);
                RubyIO g = AppMain.stuffRenderer.eventRenderer.extractEventGraphic(evI);
                if (g != null)
                    AppMain.stuffRenderer.eventRenderer.drawEventGraphic(g, px, py, igd);
            }
        }
    }

    @Override
    public void handleClick(int x, int y, int button) {
        lmX = x;
        lmY = y;
        for (int l = 0; l < layerInvisible.length; l++) {
            if (getLayerTabRect(l).contains(x, y)) {
                if (button == 1)
                    if (l < mapTable.planeCount)
                        currentLayer = l;
                if (button == 3)
                    layerInvisible[l] = !layerInvisible[l];
                return;
            }
        }
        if (button == 3) {
            dragging = true;
        } else {
            if (button == 1) {
                if (!minimap) {
                    int mouseXT = (x + camX) / tileSize;
                    int mouseYT = (y + camY) / tileSize;
                    callbacks.confirmAt(mouseXT, mouseYT, currentLayer);
                }
            }
            dragging = false;
        }
    }

    @Override
    public void handleDrag(int x, int y) {
        if (dragging) {
            camX -= x - lmX;
            camY -= y - lmY;
            lmX = x;
            lmY = y;
        }
    }

    @Override
    public String toString() {
        return "Map" + mapId;
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

    public int getCurrentLayer() {
        return currentLayer;
    }
}

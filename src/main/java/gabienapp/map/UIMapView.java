/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.map;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.*;

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
    public final int mapId;
    // This needs to be interchangable in case a table update happens...
    public RubyTable mapTable;
    public boolean minimap = false;
    public IMapViewCallbacks callbacks;
    private Runnable listener = new Runnable() {
        @Override
        public void run() {
            // Not an incredibly high-cost operation, thankfully,
            //  since it'll have to run on any edits.
            mapTable = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            Application.stuffRenderer = StuffRenderer.rendererFromMap(map);
        }
    };

    public UIMapView(int mapN, int i, int i1) {
        // This makes it load in the right spot.
        setBounds(new Rect(0, 0, i, i1));

        mapId = mapN;
        map = Application.objectDB.getObject(getMapName(mapN), "RPG::Map");
        Application.objectDB.registerModificationHandler(map, listener);
        listener.run();
        layerInvisible = new boolean[mapTable.planeCount + 2];

        Application.stuffRenderer = StuffRenderer.rendererFromMap(map);

        camX = StuffRenderer.tileSize * (mapTable.width / 2);
        camY = StuffRenderer.tileSize * (mapTable.height / 2);
        if (minimap) {
            camX /= (StuffRenderer.tileSize / 2);
            camY /= (StuffRenderer.tileSize / 2);
        }
        camX -= i / 2;
        camY -= i1 / 2;
    }

    public static String getMapName(int mapN) {
        String mapStr = Integer.toString(mapN);
        while (mapStr.length() < 3)
            mapStr = "0" + mapStr;
        return "Map" + mapStr;
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
        boolean debug = false;
        int eTileSize = StuffRenderer.tileSize;
        if (minimap)
            eTileSize = 2;

        int mouseXT = ((igd.getMouseX() - ox) + camX) / eTileSize;
        int mouseYT = ((igd.getMouseY() - oy) + camY) / eTileSize;

        Rect camR = getBounds();
        int camW = camR.width;
        int camH = camR.height;

        int camTR = ((camX + camW) / eTileSize) + 1;
        int camTB = ((camY + camH) / eTileSize) + 1;
        int camTX = (camX / eTileSize);
        int camTY = (camY / eTileSize);

        if (!layerInvisible[mapTable.planeCount]) {
            // Panorama Enable
            String panorama = Application.stuffRenderer.getPanorama();
            if (panorama.length() > 0) {
                IGrInDriver.IImage im = GaBIEn.getImage(Application.rootPath + "Graphics/Panoramas/" + panorama + ".png", 0, 0, 0);
                double ratioX = (mapTable.width * eTileSize) / (double) im.getWidth();
                double ratioY = (mapTable.height * eTileSize) / (double) im.getHeight();
                // Need to tile the area with the image.
                // This is nowhere NEAR exact, but I'm not going to risk peeking at the panorama code to find out.
                // Though I am tempted to have Niko walk around the apartments a bit and count the exact ratio out.
                int eCamX = (int) ((camX + (camW / 2)) / ratioX);
                int eCamY = (int) ((camY + (camH / 2)) / ratioY);
                eCamX += im.getWidth() / 2;
                eCamY += im.getHeight() / 2;
                int camOTX = (eCamX / im.getWidth());
                int camOTY = (eCamY / im.getHeight());
                int camOTeX = ((eCamX + camW) / im.getWidth()) + 1;
                int camOTeY = ((eCamY + camH) / im.getHeight()) + 1;
                for (int i = camOTX; i <= camOTeX; i++)
                    for (int j = camOTY; j <= camOTeY; j++)
                        igd.blitImage(0, 0, im.getWidth(), im.getHeight(), ox + (i * im.getWidth()) - eCamX, oy + (j * im.getHeight()) - eCamY, im);
            }
        }

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
                    String t = Integer.toString(mapTable.getTiletype(i, j, 0), 16);
                    String t2 = Integer.toString(mapTable.getTiletype(i, j, 1), 16);
                    String t3 = Integer.toString(mapTable.getTiletype(i, j, 2), 16);
                    UILabel.drawString(igd, px, py, t, false, false);
                    UILabel.drawString(igd, px, py + 8, t2, false, false);
                    UILabel.drawString(igd, px, py + 16, t3, false, false);
                } else {
                    for (int l = 0; l < mapTable.planeCount; l++) {
                        if (!layerInvisible[l]) {
                            short tidx = mapTable.getTiletype(i, j, l);
                            if (i == mouseXT)
                                if (j == mouseYT)
                                    tidx = callbacks.shouldDrawAtCursor(tidx, l, currentLayer);
                            Application.stuffRenderer.drawTile(tidx, px, py, igd, eTileSize);
                        }
                    }
                }
            }
        }

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
                int px = ox + ((x * StuffRenderer.tileSize) - camX);
                int py = oy + ((y * StuffRenderer.tileSize) - camY);
                Application.stuffRenderer.drawEventGraphic(evI.getInstVarBySymbol("@pages").arrVal[0].getInstVarBySymbol("@graphic"), px, py, igd);
            }
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
                    px -= StuffRenderer.tileSize / 2;
                    py += eTileSize / 2;
                    py -= StuffRenderer.tileSize / 2;
                    callbacks.performOverlay(i, j, igd, px, py, l, minimap);
                }
            }
        }
        if (selected)
            if (igd.isKeyJustPressed(IGrInDriver.VK_M))
                toggleMinimap();
        for (int i = 0; i < layerInvisible.length; i++) {
            Rect l = getLayerTabRect(i);
            igd.blitImage((i == currentLayer) ? 0 : 18, layerInvisible[i] ? 0 : 18, 18, 18, ox + l.x, oy + l.y, Application.layerTabs);
            String text = Integer.toString(i);
            if (i == mapTable.planeCount)
                text = "P";
            if (i == mapTable.planeCount + 1)
                text = "E";
            UILabel.drawString(igd, ox + l.x + 1, oy + l.y + 1, text, true, true);
        }
        UILabel.drawLabel(igd, 0, ox, oy, "Map" + mapId + ";" + mouseXT + ", " + mouseYT, false);
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
                    int mouseXT = (x + camX) / StuffRenderer.tileSize;
                    int mouseYT = (y + camY) / StuffRenderer.tileSize;
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
            camX *= StuffRenderer.tileSize / 2;
            camY *= StuffRenderer.tileSize / 2;
        } else {
            camX /= StuffRenderer.tileSize / 2;
            camY /= StuffRenderer.tileSize / 2;
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
        Application.objectDB.deregisterModificationHandler(map, listener);
    }

    // Used by tools, after they're done doing whatever.
    // Basically a convenience method.
    public void passModificationNotification() {
        Application.objectDB.objectRootModified(map);
    }
}

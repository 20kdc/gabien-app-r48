/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.dbs.ValueSyntax;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.ui.Art;
import r48.ui.UIAppendButton;

import java.util.HashMap;

/**
 * Created on 12/29/16.
 */
public class UIMTEventPicker extends UIMTBase implements IMapViewCallbacks {
    public UIMapView mapView;
    public UIScrollLayout svl = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public HashMap<String, IRIO> eventCache = new HashMap<String, IRIO>();

    public UIMTEventPicker(IMapToolContext mv) {
        super(mv);
        mapView = mv.getMapView();
        svl.panelsAdd(new UILabel(TXDB.get("Click on a target to show the events."), FontSizes.eventPickerEntryTextHeight) {
            @Override
            public void setWantedSize(Size size) {
                super.setWantedSize(new Size(size.width, size.height * 8));
            }
        });
        changeInner(svl, true);
    }

    // -- Tool things --
    @Override
    public short shouldDrawAt(MapViewDrawContext.MouseStatus mouse, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        // use this time to cache the essentials, this should vastly speed up drawing
        eventCache.clear();
        for (IRIO evK : mapView.mapTable.eventAccess.getEventKeys()) {
            long x = mapView.mapTable.eventAccess.getEventX(evK);
            long y = mapView.mapTable.eventAccess.getEventY(evK);
            eventCache.put(x + ";" + y, evK);
        }
        return 1;
    }

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, int l, boolean minimap) {
        for (int tx = mvdc.camT.x; tx < mvdc.camT.x + mvdc.camT.width; tx++) {
            for (int ty = mvdc.camT.y; ty < mvdc.camT.y + mvdc.camT.height; ty++) {
                if (eventCache.containsKey(tx + ";" + ty))
                    Art.drawTarget(tx * mvdc.tileSize, ty * mvdc.tileSize, mapView.tileSize, mvdc.igd);
            }
        }
    }

    @Override
    public void confirmAt(final int x, final int y, int pixx, int pixy, final int layer, boolean first) {
        svl.panelsClear();
        for (final IRIO evK : mapView.mapTable.eventAccess.getEventKeys()) {
            final long eventX = mapView.mapTable.eventAccess.getEventX(evK);
            final long eventY = mapView.mapTable.eventAccess.getEventY(evK);
            String eventName = mapView.mapTable.eventAccess.getEventName(evK);
            if (eventX == x)
                if (eventY == y) {
                    String nam = evK.toString();
                    if (eventName != null)
                        nam = eventName;

                    Runnable r = mapView.mapTable.eventAccess.hasSync(evK);
                    if (r == null) {
                        // Note the checks in case of out of date panel.
                        UIElement button = new UITextButton(nam, FontSizes.eventPickerEntryTextHeight, new Runnable() {
                            @Override
                            public void run() {
                                if (mapView.mapTable.eventAccess.hasSync(evK) != null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                IRIO evI = mapView.mapTable.eventAccess.getEvent(evK);
                                if (evI == null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                showEvent(evK, mapView, evI);
                            }
                        });
                        button = new UIAppendButton(TXDB.get("Move"), button, new Runnable() {
                            @Override
                            public void run() {
                                if (mapView.mapTable.eventAccess.hasSync(evK) != null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                IRIO evI = mapView.mapTable.eventAccess.getEvent(evK);
                                if (evI == null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                mapToolContext.accept(new UIMTEventMover(mapToolContext, evK));
                            }
                        }, FontSizes.eventPickerEntryTextHeight);
                        button = new UIAppendButton(TXDB.get("Clone"), button, new Runnable() {
                            @Override
                            public void run() {
                                if (mapView.mapTable.eventAccess.hasSync(evK) != null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                IRIO evI = mapView.mapTable.eventAccess.getEvent(evK);
                                if (evI == null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                RubyIO newEvent = new RubyIO().setDeepClone(evI);
                                IRIO nevK = mapView.mapTable.eventAccess.addEvent(newEvent, mapView.mapTable.eventAccess.getEventType(evK));
                                if (nevK == null)
                                    return;
                                mapToolContext.accept(new UIMTEventMover(mapToolContext, nevK));
                            }
                        }, FontSizes.eventPickerEntryTextHeight);
                        button = new UIAppendButton(TXDB.get("Del."), button, new Runnable() {
                            @Override
                            public void run() {
                                if (mapView.mapTable.eventAccess.hasSync(evK) != null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                IRIO evI = mapView.mapTable.eventAccess.getEvent(evK);
                                if (evI == null) {
                                    confirmAt(x, y, 123, 123, layer, true);
                                    return;
                                }
                                mapView.mapTable.eventAccess.delEvent(evK);
                                confirmAt(x, y, 123, 123, layer, true);
                            }
                        }, FontSizes.eventPickerEntryTextHeight);
                        svl.panelsAdd(button);
                    } else {
                        UIElement button = new UILabel(nam, FontSizes.eventPickerEntryTextHeight);
                        button = new UIAppendButton(TXDB.get("Sync"), button, new Runnable() {
                            @Override
                            public void run() {
                                // It's possible (if unlikely) that this action actually became invalid.
                                // Consider: confirmAt -> object change -> click Sync
                                Runnable r = mapView.mapTable.eventAccess.hasSync(evK);
                                if (r != null)
                                    r.run();
                                confirmAt(x, y, 123, 123, layer, true);
                            }
                        }, FontSizes.eventPickerEntryTextHeight);
                        svl.panelsAdd(button);
                    }
                }
        }
        String[] types = mapView.mapTable.eventAccess.eventTypes();
        for (int i = 0; i < types.length; i++) {
            final int i2 = i;
            if (types[i] == null)
                continue;
            svl.panelsAdd(new UITextButton(types[i], FontSizes.eventPickerEntryTextHeight, new Runnable() {
                @Override
                public void run() {
                    IRIO k = mapView.mapTable.eventAccess.addEvent(null, i2);
                    if (k == null)
                        return;
                    IRIO v = mapView.mapTable.eventAccess.getEvent(k);
                    if (v == null)
                        throw new RuntimeException("IEventAccess implementation not sane.");
                    IRIO evName = v.getIVar("@name");
                    if (evName != null) {
                        String n = Integer.toString((int) k.getFX());
                        while (n.length() < 4)
                            n = "0" + n;
                        evName.setString("EV" + n);
                    }
                    mapView.mapTable.eventAccess.setEventXY(k, x, y);
                    showEvent(k, mapView, v);
                }
            }));
        }
    }

    @Override
    public String toString() {
        return FormatSyntax.formatExtended(TXDB.get("Ev.Pick #[#A total#]"), new RubyIO().setFX(eventCache.size()));
    }

    public static void showEvent(IRIO key, UIMapView map, IRIO event) {
        String[] root = map.mapTable.eventAccess.getEventSchema(key);
        if (root == null)
            return;
        key = ValueSyntax.decode(root[3]);
        AppMain.launchNonRootSchema(AppMain.objectDB.getObject(root[1]), root[2], key, event, root[0], "E" + key, map);
    }

    public static void showEventDivorced(IRIO key, IObjectBackend.ILoadedObject map, String mapSchema, IRIO event, String eventSchema) {
        AppMain.launchNonRootSchema(map, mapSchema, key, event, eventSchema, "E" + key, null);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.maptools;

import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.uslx.append.Size;
import r48.App;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.map.MapViewDrawContext;
import r48.map.UIMapView;
import r48.map.events.IEventAccess;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaDynamicContext;
import r48.ui.UIAppendButton;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Created on 12/29/16.
 */
public class UIMTEventPicker extends UIMTBase implements IMapViewCallbacks {
    public UIMapView mapView;
    public UIScrollLayout svl = new UIScrollLayout(true, app.f.generalS);
    public HashMap<String, DMKey> eventCache = new HashMap<String, DMKey>();

    public UIMTEventPicker(IMapToolContext mv) {
        super(mv);
        mapView = mv.getMapView();
        svl.panelsSet(new UILabel(T.m.tsClickToShowEv, app.f.eventPickerEntryTH));
        svl.setWantedSizeOverride(new Size(app.f.eventPickerEntryTH * 12, app.f.eventPickerEntryTH * 8));
        changeInner(svl, true);
    }

    // -- Tool things --

    @Override
    public void performGlobalOverlay(MapViewDrawContext mvdc, boolean minimap) {
        // use this time to cache the essentials, this should vastly speed up drawing
        eventCache.clear();
        for (DMKey evK : mapView.mapTable.eventAccess.getEventKeys()) {
            long x = mapView.mapTable.eventAccess.getEventX(evK);
            long y = mapView.mapTable.eventAccess.getEventY(evK);
            eventCache.put(x + ";" + y, evK);
        }
        for (int tx = mvdc.camT.x; tx < mvdc.camT.x + mvdc.camT.width; tx++) {
            for (int ty = mvdc.camT.y; ty < mvdc.camT.y + mvdc.camT.height; ty++) {
                if (eventCache.containsKey(tx + ";" + ty))
                    app.a.drawTarget(tx * mvdc.tileSize, ty * mvdc.tileSize, mapView.tileSize, mvdc.igd, mvdc.atOrBelowHalfSize);
            }
        }
    }

    @Override
    public void confirmAt(final int x, final int y, int pixx, int pixy, final int layer, boolean first) {
        LinkedList<UIElement> elms = new LinkedList<>();
        for (final DMKey evK : mapView.mapTable.eventAccess.getEventKeys()) {
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
                        UIElement button = new UITextButton(nam, app.f.eventPickerEntryTH, () -> {
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
                        });
                        button = new UIAppendButton(T.m.bMove, button, () -> {
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
                        }, app.f.eventPickerEntryTH);
                        button = new UIAppendButton(T.g.bCopy, button, () -> {
                            if (mapView.mapTable.eventAccess.hasSync(evK) != null) {
                                confirmAt(x, y, 123, 123, layer, true);
                                return;
                            }
                            IRIO evI = mapView.mapTable.eventAccess.getEvent(evK);
                            if (evI == null) {
                                confirmAt(x, y, 123, 123, layer, true);
                                return;
                            }
                            app.setClipboardFrom(evI);
                        }, app.f.eventPickerEntryTH);
                        UIAppendButton delAppend = new UIAppendButton(T.m.bDel, button, null, app.f.eventPickerEntryTH);
                        delAppend.button.onClick = () -> {
                            app.ui.confirmDeletion(false, eventName, delAppend, () -> {
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
                            });
                        };
                        button = delAppend;
                        elms.add(button);
                    } else {
                        UIElement button = new UILabel(nam, app.f.eventPickerEntryTH);
                        button = new UIAppendButton(T.m.bSync, button, () -> {
                            // It's possible (if unlikely) that this action actually became invalid.
                            // Consider: confirmAt -> object change -> click Sync
                            Runnable syncCB = mapView.mapTable.eventAccess.hasSync(evK);
                            if (syncCB != null)
                                syncCB.run();
                            confirmAt(x, y, 123, 123, layer, true);
                        }, app.f.eventPickerEntryTH);
                        elms.add(button);
                    }
                }
        }
        String[] types = mapView.mapTable.eventAccess.eventTypes();
        for (int i = 0; i < types.length; i++) {
            final int i2 = i;
            if (types[i] == null)
                continue;
            UIElement pan = new UITextButton(types[i], app.f.eventPickerEntryTH, () -> {
                DMKey k = mapView.mapTable.eventAccess.addEvent(null, i2);
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
            });
            pan = new UIAppendButton(T.g.bPaste, pan, () -> {
                DMKey k = mapView.mapTable.eventAccess.addEvent(app.theClipboard, i2);
                if (k == null)
                    return;
                mapView.mapTable.eventAccess.setEventXY(k, x, y);
                mapToolContext.accept(new UIMTEventMover(mapToolContext, k));
            }, app.f.eventPickerEntryTH);
            elms.add(pan);
        }
        svl.panelsSet(elms);
    }

    @Override
    public String toString() {
        return T.m.tsEvPick.r(eventCache.size());
    }

    public static void showEvent(DMKey key, @NonNull UIMapView map, IRIO event) {
        IEventAccess.EventSchema root = map.mapTable.eventAccess.getEventSchema(key);
        if (root == null)
            return;
        key = root.key;
        map.app.ui.launchDisconnectedSchema(root.root, key, event, root.eventSchema, "E" + key, new SchemaDynamicContext(map.app, map));
    }

    public static void showEventDivorced(App app, DMKey key, ObjectRootHandle map, IRIO event, SchemaElement eventSchema) {
        app.ui.launchDisconnectedSchema(map, key, event, eventSchema, "E" + key, null);
    }

    @Override
    @NonNull
    public String viewState(int mouseXT, int mouseYT) {
        return "EventPicker";
    }
}

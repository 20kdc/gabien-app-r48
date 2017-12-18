/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
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
    public HashMap<String, RubyIO> eventCache = new HashMap<String, RubyIO>();

    public UIMTEventPicker(IMapToolContext mv) {
        super(mv, false);
        mapView = mv.getMapView();
        changeInner(svl);
        setBounds(new Rect(0, 0, 320, 200));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        svl.setBounds(new Rect(0, 0, r.width, r.height));
    }

    // -- Tool things --
    @Override
    public short shouldDrawAt(int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        // use this time to cache the essentials, this should vastly speed up drawing
        eventCache.clear();
        for (RubyIO evK : mapView.mapTable.eventAccess.getEventKeys()) {
            RubyIO evI = mapView.mapTable.eventAccess.getEvent(evK);
            eventCache.put((evI.getInstVarBySymbol("@x").fixnumVal) + ";" + (evI.getInstVarBySymbol("@y").fixnumVal), evI);
        }
        return 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        if (eventCache.containsKey(tx + ";" + ty))
            Art.drawTarget(px, py, mapView.tileSize, igd);
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(final int x, final int y, final int layer) {
        svl.panels.clear();
        for (final RubyIO evK : mapView.mapTable.eventAccess.getEventKeys()) {
            final RubyIO evI = mapView.mapTable.eventAccess.getEvent(evK);
            if (evI.getInstVarBySymbol("@x").fixnumVal == x)
                if (evI.getInstVarBySymbol("@y").fixnumVal == y) {
                    String nam = "event" + evK.toString();
                    if (evI.getInstVarBySymbol("@name") != null)
                        nam = evI.getInstVarBySymbol("@name").decString();
                    UIElement button = new UITextButton(FontSizes.eventPickerEntryTextHeight, nam, new Runnable() {
                        @Override
                        public void run() {
                            showEvent(evK, mapView, evI);
                        }
                    });
                    button = new UIAppendButton(TXDB.get("Move"), button, new Runnable() {
                        @Override
                        public void run() {
                            mapToolContext.accept(new UIMTEventMover(evI, mapToolContext));
                        }
                    }, FontSizes.eventPickerEntryTextHeight);
                    button = new UIAppendButton(TXDB.get("Clone"), button, new Runnable() {
                        @Override
                        public void run() {
                            RubyIO newEvent = new RubyIO().setDeepClone(evI);
                            if (mapView.mapTable.eventAccess.addEvent(newEvent, mapView.mapTable.eventAccess.getEventType(evK)) == null) {
                                AppMain.launchDialog(TXDB.get("Couldn't add the event."));
                                return;
                            }
                            // This'll fix the potential inconsistencies
                            mapView.passModificationNotification();
                            mapToolContext.accept(new UIMTEventMover(newEvent, mapToolContext));
                        }
                    }, FontSizes.eventPickerEntryTextHeight);
                    button = new UIAppendButton(TXDB.get("Del."), button, new Runnable() {
                        @Override
                        public void run() {
                            mapView.mapTable.eventAccess.delEvent(evK);
                            mapView.passModificationNotification();
                            confirmAt(x, y, layer);
                        }
                    }, FontSizes.eventPickerEntryTextHeight);
                    svl.panels.add(button);
                }
        }
        String[] types = mapView.mapTable.eventAccess.eventTypes();
        for (int i = 0; i < types.length; i++) {
            final int i2 = i;
            if (types[i] == null)
                continue;
            svl.panels.add(new UITextButton(FontSizes.eventPickerEntryTextHeight, types[i], new Runnable() {
                @Override
                public void run() {
                    RubyIO k = mapView.mapTable.eventAccess.addEvent(null, i2);
                    if (k == null) {
                        AppMain.launchDialog(TXDB.get("Couldn't add the event."));
                        return;
                    }
                    RubyIO v = mapView.mapTable.eventAccess.getEvent(k);
                    if (v == null)
                        throw new RuntimeException("IEventAccess implementation not sane.");
                    RubyIO evName = v.getInstVarBySymbol("@name");
                    if (evName != null) {
                        String n = Integer.toString((int) k.fixnumVal);
                        while (n.length() < 4)
                            n = "0" + n;
                        evName.encString("EV" + n, false);
                    }
                    v.getInstVarBySymbol("@x").fixnumVal = x;
                    v.getInstVarBySymbol("@y").fixnumVal = y;
                    mapView.passModificationNotification();
                    showEvent(k, mapView, v);
                }
            }));
        }
        svl.runLayout();
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return false;
    }

    @Override
    public String toString() {
        return FormatSyntax.formatExtended(TXDB.get("Ev.Pick #[#A total#]"), new RubyIO().setFX(eventCache.size()));
    }

    public static void showEvent(RubyIO key, UIMapView map, RubyIO event) {
        AppMain.launchNonRootSchema(map.map.object, map.map.objectSchema, key, event, map.mapTable.eventAccess.getEventSchema(key), "E" + key, map);
    }

    public static void showEventDivorced(RubyIO key, RubyIO map, String mapSchema, RubyIO event, String eventSchema) {
        AppMain.launchNonRootSchema(map, mapSchema, key, event, eventSchema, "E" + key, null);
    }
}

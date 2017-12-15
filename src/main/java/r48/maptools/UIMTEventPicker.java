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
import r48.schema.util.SchemaPath;
import r48.ui.Art;
import r48.ui.UIAppendButton;

import java.util.HashMap;
import java.util.Map;

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
        for (RubyIO evI : mapView.map.object.getInstVarBySymbol("@events").hashVal.values())
            eventCache.put((evI.getInstVarBySymbol("@x").fixnumVal) + ";" + (evI.getInstVarBySymbol("@y").fixnumVal), evI);
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
        for (Map.Entry<RubyIO, RubyIO> evE : mapView.map.object.getInstVarBySymbol("@events").hashVal.entrySet()) {
            final RubyIO evK = evE.getKey();
            final RubyIO evI = evE.getValue();
            if (evI.getInstVarBySymbol("@x").fixnumVal == x)
                if (evI.getInstVarBySymbol("@y").fixnumVal == y) {
                    String nam = "event" + evK.toString();
                    if (evI.getInstVarBySymbol("@name") != null)
                        nam = evI.getInstVarBySymbol("@name").decString();
                    UIElement button = new UITextButton(FontSizes.eventPickerEntryTextHeight, nam, new Runnable() {
                        @Override
                        public void run() {
                            showEvent(evK.fixnumVal, mapView, evI);
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
                            RubyIO evtHash = mapView.map.object.getInstVarBySymbol("@events");
                            int unusedIndex = getFreeIndex(evtHash);
                            RubyIO newEvent = new RubyIO().setDeepClone(evI);
                            evtHash.hashVal.put(new RubyIO().setFX(unusedIndex), newEvent);
                            // This'll fix the potential inconsistencies
                            mapView.passModificationNotification();
                            mapToolContext.accept(new UIMTEventMover(newEvent, mapToolContext));
                        }
                    }, FontSizes.eventPickerEntryTextHeight);
                    button = new UIAppendButton(TXDB.get("Del."), button, new Runnable() {
                        @Override
                        public void run() {
                            mapView.map.object.getInstVarBySymbol("@events").hashVal.remove(evK);
                            mapView.passModificationNotification();
                            confirmAt(x, y, layer);
                        }
                    }, FontSizes.eventPickerEntryTextHeight);
                    svl.panels.add(button);
                }
        }
        svl.panels.add(new UITextButton(FontSizes.eventPickerEntryTextHeight, TXDB.get("+ Add Event"), new Runnable() {
            @Override
            public void run() {
                RubyIO evtHash = mapView.map.object.getInstVarBySymbol("@events");
                int unusedIndex = getFreeIndex(evtHash);

                RubyIO k = new RubyIO().setFX(unusedIndex);
                RubyIO newEvent = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry(mapView.mapTable.renderer.eventSchema), k);
                RubyIO evName = newEvent.getInstVarBySymbol("@name");
                if (evName != null) {
                    String n = Integer.toString(unusedIndex);
                    while (n.length() < 4)
                        n = "0" + n;
                    evName.encString("EV" + n);
                }
                newEvent.getInstVarBySymbol("@x").fixnumVal = x;
                newEvent.getInstVarBySymbol("@y").fixnumVal = y;
                evtHash.hashVal.put(k, newEvent);
                mapView.passModificationNotification();
                showEvent(unusedIndex, mapView, newEvent);
            }
        }));
        svl.runLayout();
    }

    private int getFreeIndex(RubyIO evtHash) {
        int unusedIndex = mapView.mapTable.renderer.eventRenderer.eventIdBase();
        while (evtHash.getHashVal(new RubyIO().setFX(unusedIndex)) != null)
            unusedIndex++;
        return unusedIndex;
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return false;
    }

    @Override
    public String toString() {
        return FormatSyntax.formatExtended(TXDB.get("Ev.Pick #[#A total#]"), new RubyIO().setFX(eventCache.size()));
    }

    public static void showEvent(long fixnumVal, UIMapView map, RubyIO event) {
        AppMain.launchNonRootSchema(map.map.object, map.map.objectSchema, new RubyIO().setFX(fixnumVal), event, map.mapTable.renderer.eventSchema, "E" + fixnumVal, map);
    }

    public static void showEventDivorced(long fixnumVal, RubyIO map, String mapSchema, RubyIO event) {
        AppMain.launchNonRootSchema(map, mapSchema, new RubyIO().setFX(fixnumVal), event, AppMain.stuffRendererIndependent.eventSchema, "E" + fixnumVal, null);
    }
}

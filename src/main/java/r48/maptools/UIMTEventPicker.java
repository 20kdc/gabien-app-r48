/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
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
public class UIMTEventPicker extends UIPanel implements IMapViewCallbacks {
    public IConsumer<UIElement> windowView;
    public UIMapView mapView;
    public UIScrollLayout svl = new UIScrollLayout(true);
    public HashMap<String, RubyIO> eventCache = new HashMap<String, RubyIO>();

    public UIMTEventPicker(IConsumer<UIElement> wv, UIMapView mv) {
        windowView = wv;
        mapView = mv;
        allElements.add(svl);
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
        for (RubyIO evI : mapView.map.getInstVarBySymbol("@events").hashVal.values())
            eventCache.put((evI.getInstVarBySymbol("@x").fixnumVal) + ";" + (evI.getInstVarBySymbol("@y").fixnumVal), evI);
        return 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        if (eventCache.containsKey(tx + ";" + ty))
            Art.drawTarget(px, py, mapView.tileSize, igd);
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap) {

    }

    @Override
    public void confirmAt(final int x, final int y, final int layer) {
        svl.panels.clear();
        for (Map.Entry<RubyIO, RubyIO> evE : mapView.map.getInstVarBySymbol("@events").hashVal.entrySet()) {
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
                            // In practice I have seen that this should always go away after selection.
                            showEvent(evK.fixnumVal, mapView, evI);
                            AppMain.nextMapTool = null;
                        }
                    });
                    button = new UIAppendButton(TXDB.get("MOV"), button, new Runnable() {
                        @Override
                        public void run() {
                            AppMain.nextMapTool = new UIMTEventMover(evI, mapView);
                        }
                    }, FontSizes.eventPickerEntryTextHeight);
                    button = new UIAppendButton(TXDB.get("DEL"), button, new Runnable() {
                        @Override
                        public void run() {
                            mapView.map.getInstVarBySymbol("@events").hashVal.remove(evK);
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
                int unusedIndex = mapView.renderer.eventRenderer.eventIdBase();
                RubyIO evtHash = mapView.map.getInstVarBySymbol("@events");
                while (evtHash.getHashVal(new RubyIO().setFX(unusedIndex)) != null)
                    unusedIndex++;

                RubyIO k = new RubyIO().setFX(unusedIndex);
                RubyIO newEvent = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Event"), k);
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

    @Override
    public boolean shouldIgnoreDrag() {
        return false;
    }

    @Override
    public String toString() {
        return FormatSyntax.formatExtended(TXDB.get("Ev.Pick #[#A total#]"), new RubyIO().setFX(eventCache.size()));
    }

    public static void showEvent(long fixnumVal, UIMapView map, RubyIO event) {
        AppMain.launchNonRootSchema(map.map, "RPG::Map", new RubyIO().setFX(fixnumVal), event, "RPG::Event", "E" + fixnumVal, map);
    }

    public static void showEventDivorced(long fixnumVal, RubyIO map, RubyIO event) {
        AppMain.launchNonRootSchema(map, "RPG::Map", new RubyIO().setFX(fixnumVal), event, "RPG::Event", "E" + fixnumVal, null);
    }
}

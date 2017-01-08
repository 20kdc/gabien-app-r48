/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.maptools;

import gabien.IGrInDriver;
import gabien.ui.*;
import gabienapp.*;
import gabienapp.map.IMapViewCallbacks;
import gabienapp.map.UIMapView;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.schema.util.SchemaPath;
import gabienapp.ui.UIAppendButton;
import gabienapp.ui.UIScrollVertLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 12/29/16.
 */
public class UIMTEventPicker extends UIPanel implements IMapViewCallbacks {
    public IConsumer<UIElement> windowView;
    public UIMapView mapView;
    public UIScrollVertLayout svl = new UIScrollVertLayout();
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
    public short shouldDrawAtCursor(short there, int layer, int currentLayer) {
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
    public void performOverlay(int tx, int ty, IGrInDriver igd, int px, int py, int ol, boolean minimap) {
        if (eventCache.containsKey(tx + ";" + ty))
            igd.blitBCKImage(0, 36, 16, 16, px + 8, py + 8, Application.layerTabs);
    }

    @Override
    public void confirmAt(final int x, final int y, final int layer) {
        svl.panels.clear();
        for (Map.Entry<RubyIO, RubyIO> evE : mapView.map.getInstVarBySymbol("@events").hashVal.entrySet()) {
            final RubyIO evK = evE.getKey();
            final RubyIO evI = evE.getValue();
            if (evI.getInstVarBySymbol("@x").fixnumVal == x)
                if (evI.getInstVarBySymbol("@y").fixnumVal == y) {
                    UIElement button = new UITextButton(true, evI.getInstVarBySymbol("@name").decString(), new Runnable() {
                        @Override
                        public void run() {
                            showEvent(evK.fixnumVal, mapView.map, evI);
                        }
                    });
                    button = new UIAppendButton("MOV", button, new Runnable() {
                        @Override
                        public void run() {
                            Application.nextMapTool = new UIMTEventMover(evI, mapView);
                        }
                    }, true);
                    button = new UIAppendButton("DEL", button, new Runnable() {
                        @Override
                        public void run() {
                            mapView.map.getInstVarBySymbol("@events").hashVal.remove(evK);
                            mapView.passModificationNotification();
                            confirmAt(x, y, layer);
                        }
                    }, true);
                    svl.panels.add(button);
                }
        }
        svl.panels.add(new UITextButton(true, "+ Add Event", new Runnable() {
            @Override
            public void run() {
                int unusedIndex = 1;
                RubyIO evtHash = mapView.map.getInstVarBySymbol("@events");
                while (evtHash.getHashVal(new RubyIO().setFX(unusedIndex)) != null)
                    unusedIndex++;

                RubyIO k = new RubyIO().setFX(unusedIndex);
                RubyIO newEvent = SchemaPath.createDefaultValue(Application.schemas.getSDBEntry("RPG::Event"), k);

                newEvent.getInstVarBySymbol("@x").fixnumVal = x;
                newEvent.getInstVarBySymbol("@y").fixnumVal = y;
                evtHash.hashVal.put(k, newEvent);
                showEvent(unusedIndex, mapView.map, newEvent);
            }
        }));
        svl.runLayout();
    }

    @Override
    public String toString() {
        return "Ev.Pick [" + eventCache.size() + " total]";
    }

    public static void showEvent(long fixnumVal, RubyIO map, RubyIO event) {
        Application.launchNonRootSchema(map, "RPG::Map", map.getInstVarBySymbol("@events"), "mapEvents", new RubyIO().setFX(fixnumVal), event, "RPG::Event", "E" + fixnumVal);
    }
}

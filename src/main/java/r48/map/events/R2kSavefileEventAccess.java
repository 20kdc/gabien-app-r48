/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import r48.AppMain;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created in the early hours of December 18th, 2017
 * Most functionality added (ghhhoooossstttsss!!!) in the late hours of that day.
 * BTW: A "ghost" refers to an event in the map not described in the save.
 * For maximum compatibility, the save creator deliberately tries not to add events,
 *  since the converter isn't going to be as accurate as 2k[3].
 * In essence: 2k[3]'s save compatibility system is awesome! *Let's stress-test it to simplify this code.*
 * An additional benefit of this is that it's bascally behavior-accurate,
 *  because the ghosts & map events getting merged by the game has similar results to what happens here.
 */
public class R2kSavefileEventAccess implements IEventAccess {
    public final RubyIO saveFileRoot;

    // This only contains the living.
    // The ghosts are added dynamically by getEventKeys & getEvent
    public final RubyIO eventsHash = new RubyIO().setHash();

    public R2kSavefileEventAccess(RubyIO root) {
        saveFileRoot = root;
        int mapId = (int) saveFileRoot.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map").fixnumVal;
        // Inject 'events'
        sfveInjectEvent("Party", mapId, saveFileRoot.getInstVarBySymbol("@party_pos"));
        sfveInjectEvent("Boat", mapId, saveFileRoot.getInstVarBySymbol("@boat_pos"));
        sfveInjectEvent("Ship", mapId, saveFileRoot.getInstVarBySymbol("@ship_pos"));
        sfveInjectEvent("Airship", mapId, saveFileRoot.getInstVarBySymbol("@airship_pos"));
        // Inject actual events
        for (Map.Entry<RubyIO, RubyIO> kv : saveFileRoot.getInstVarBySymbol("@map_info").getInstVarBySymbol("@events").hashVal.entrySet())
            if (eventsHash.getHashVal(kv.getKey()) == null)
                eventsHash.hashVal.put(kv.getKey(), kv.getValue());
    }

    private void sfveInjectEvent(String s, int mapId, RubyIO instVarBySymbol) {
        if (instVarBySymbol.getInstVarBySymbol("@map").fixnumVal != mapId)
            return;
        eventsHash.hashVal.put(new RubyIO().setString(s, true), instVarBySymbol);
    }

    @Override
    public LinkedList<RubyIO> getEventKeys() {
        LinkedList<RubyIO> keys = new LinkedList<RubyIO>(eventsHash.hashVal.keySet());
        RubyIO map = getMap();
        for (RubyIO rio : map.getInstVarBySymbol("@events").hashVal.keySet()) {
            if (eventsHash.getHashVal(rio) == null) {
                // Add ghost
                keys.add(rio);
            }
        }
        return keys;
    }

    @Override
    public RubyIO getEvent(RubyIO key) {
        RubyIO v = eventsHash.getHashVal(key);
        if (v != null)
            return v;
        // Ghost! (?)
        RubyIO map = getMap();
        if (map == null)
            return null; // shouldn't happen
        return getMapEvent(map, key);
    }

    private RubyIO getMap() {
        int mapId = (int) saveFileRoot.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map").fixnumVal;
        return AppMain.objectDB.getObject(R2kRMLikeMapInfoBackend.sNameFromInt(mapId), null);
    }

    private RubyIO getMapEvent(RubyIO map, RubyIO key) {
        return map.getInstVarBySymbol("@events").getHashVal(key);
    }

    @Override
    public void delEvent(RubyIO key) {
        if (key.type == '"') {
            AppMain.launchDialog(TXDB.get("You can't delete that. Open it up and move it to map 0 if need be."));
        } else {
            if (eventsHash.getHashVal(key) == null) {
                AppMain.launchDialog(TXDB.get("You can't kill a ghost, silly! Also, how are you reading this? The UI shouldn't let you run this right now."));
            } else {
                eventsHash.removeHashVal(key);
                AppMain.launchDialog(TXDB.get("Transformed to ghost. If you want the event *gone*, re-Sync it and set @active to false."));
            }
        }
    }

    @Override
    public String[] eventTypes() {
        return new String[] {
        };
    }

    @Override
    public RubyIO addEvent(RubyIO eve, int type) {
        AppMain.launchDialog(TXDB.get("Couldn't add the event."));
        return null;
    }

    private RubyIO eventAsSaveEvent(long mapId, RubyIO key, RubyIO event) {
        RubyIO rio = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::SaveMapEvent"), key);
        rio.getInstVarBySymbol("@map").setFX(mapId);
        rio.getInstVarBySymbol("@x").setDeepClone(event.getInstVarBySymbol("@x"));
        rio.getInstVarBySymbol("@y").setDeepClone(event.getInstVarBySymbol("@y"));

        RubyIO pages = event.getInstVarBySymbol("@pages");
        RubyIO eventPage = null;
        if (pages.arrVal.length >= 2)
            eventPage = pages.arrVal[1];
        if (eventPage != null) {
            rio.getInstVarBySymbol("@character_name").setDeepClone(eventPage.getInstVarBySymbol("@character_name"));
            rio.getInstVarBySymbol("@character_index").setDeepClone(eventPage.getInstVarBySymbol("@character_index"));
            rio.getInstVarBySymbol("@character_direction").setDeepClone(eventPage.getInstVarBySymbol("@character_direction"));

            rio.getInstVarBySymbol("@dir").setDeepClone(eventPage.getInstVarBySymbol("@character_direction"));
            rio.getInstVarBySymbol("@transparency").setFX((eventPage.getInstVarBySymbol("@character_blend_mode").type == 'T') ? 3 : 0);

            rio.getInstVarBySymbol("@move_freq").setDeepClone(eventPage.getInstVarBySymbol("@move_freq"));
            rio.getInstVarBySymbol("@move_speed").setDeepClone(eventPage.getInstVarBySymbol("@move_speed"));
        }
        return rio;
    }

    @Override
    public String getEventSchema(RubyIO key) {
        if (key.type == '"') {
            if (key.decString().equals("Party"))
                return "RPG::SavePartyLocation";
            if (key.decString().equals("Boat"))
                return "RPG::SaveVehicleLocation";
            if (key.decString().equals("Ship"))
                return "RPG::SaveVehicleLocation";
            if (key.decString().equals("Airship"))
                return "RPG::SaveVehicleLocation";
        }
        // Used for ghosts
        if (eventsHash.getHashVal(key) == null)
            return "OPAQUE";
        return "RPG::SaveMapEvent";
    }

    @Override
    public int getEventType(RubyIO evK) {
        // for cloning
        return 1;
    }

    @Override
    public Runnable hasSync(RubyIO evK) {
        // Ghost...!
        if (eventsHash.getHashVal(evK) == null)
            return new Runnable() {
                @Override
                public void run() {
                    AppMain.launchDialog(TXDB.get("Naw! Ghostie want biscuits!"));
                }
            };
        return null;
    }
}

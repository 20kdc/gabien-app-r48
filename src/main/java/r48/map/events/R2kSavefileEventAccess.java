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
import r48.dbs.ValueSyntax;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;
import java.util.Map;

import static r48.schema.specialized.R2kSystemDefaultsInstallerSchemaElement.getSaveCount;

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
    public final String saveFileRootId;
    public final String saveFileRootSchema;

    // This only contains the living.
    // The ghosts are added dynamically by getEventKeys & getEvent
    public final RubyIO eventsHash = new RubyIO().setHash();

    public R2kSavefileEventAccess(String rootId, RubyIO root, String rootSchema) {
        saveFileRoot = root;
        saveFileRootId = rootId;
        saveFileRootSchema = rootSchema;
        int mapId = (int) getMapId();
        // Inject 'events'
        sfveInjectEvent("Party", mapId, saveFileRoot.getInstVarBySymbol("@party_pos"));
        sfveInjectEvent("Boat", mapId, saveFileRoot.getInstVarBySymbol("@boat_pos"));
        sfveInjectEvent("Ship", mapId, saveFileRoot.getInstVarBySymbol("@ship_pos"));
        sfveInjectEvent("Airship", mapId, saveFileRoot.getInstVarBySymbol("@airship_pos"));
        // Inject actual events
        for (Map.Entry<RubyIO, RubyIO> kv : getSaveEvents().hashVal.entrySet())
            if (eventsHash.getHashVal(kv.getKey()) == null)
                eventsHash.hashVal.put(kv.getKey(), kv.getValue());
    }

    private long getMapId() {
        return saveFileRoot.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map").fixnumVal;
    }

    private RubyIO getSaveEvents() {
        return saveFileRoot.getInstVarBySymbol("@map_info").getInstVarBySymbol("@events");
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
        if (map != null)
            if (getSaveCount(map).fixnumVal != saveFileRoot.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map_save_count").fixnumVal)
                for (RubyIO rio : map.getInstVarBySymbol("@events").hashVal.keySet())
                    if (eventsHash.getHashVal(rio) == null)
                        keys.add(rio); // Add ghost
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
            if (key.decString().equals("Player")) {
                AppMain.launchDialog(TXDB.get("You can't do THAT! ...Who would clean up the mess?"));
            } else {
                RubyIO rio = getEvent(key);
                if (rio == null) {
                    AppMain.launchDialog(TXDB.get("That's already gone."));
                } else {
                    rio.getInstVarBySymbol("@map").fixnumVal = 0;
                    AppMain.launchDialog(TXDB.get("Can't be deleted, but was moved to @map 0 (as close as you can get to deleted)"));
                    pokeHive();
                }
            }
        } else {
            RubyIO se = getSaveEvents();
            if (se.getHashVal(key) == null) {
                AppMain.launchDialog(TXDB.get("You're trying to delete a ghost. Yes, I know the Event Picker is slightly unreliable. Poor ghost."));
            } else {
                se.removeHashVal(key);
                RubyIO map = getMap();
                boolean ghost = false;
                if (map != null)
                    if (getSaveCount(map).fixnumVal != saveFileRoot.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map_save_count").fixnumVal)
                        ghost = true;
                if (ghost) {
                    AppMain.launchDialog(TXDB.get("Transformed to ghost. Re-Syncing it and setting @active to false might get rid of it."));
                } else {
                    AppMain.launchDialog(TXDB.get("As the version numbers are in sync, this worked."));
                }
                pokeHive();
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

    public static RubyIO eventAsSaveEvent(long mapId, RubyIO key, RubyIO event) {
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

            rio.getInstVarBySymbol("@layer").setDeepClone(eventPage.getInstVarBySymbol("@layer"));
            rio.getInstVarBySymbol("@block_other_events").setDeepClone(eventPage.getInstVarBySymbol("@block_other_events"));
            // with any luck the moveroute issue will solve itself. with luck.
        }
        return rio;
    }

    @Override
    public String[] getEventSchema(RubyIO key) {
        if (key.type == '"') {
            if (key.decString().equals("Party"))
                return new String[] {"RPG::SavePartyLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key, true)};
            if (key.decString().equals("Boat"))
                return new String[] {"RPG::SaveVehicleLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key, true)};
            if (key.decString().equals("Ship"))
                return new String[] {"RPG::SaveVehicleLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key, true)};
            if (key.decString().equals("Airship"))
                return new String[] {"RPG::SaveVehicleLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key, true)};
        }
        // Used for ghosts
        if (eventsHash.getHashVal(key) == null)
            return new String[] {"OPAQUE", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key, true)};
        return new String[] {"RPG::SaveMapEvent", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key, true)};
    }

    @Override
    public int getEventType(RubyIO evK) {
        // for cloning
        return 1;
    }

    @Override
    public Runnable hasSync(final RubyIO evK) {
        // Ghost...!
        if (eventsHash.getHashVal(evK) == null)
            return new Runnable() {
                @Override
                public void run() {
                    // "Naw! Ghostie want biscuits!"
                    if (eventsHash.getHashVal(evK) != null) {
                        // "Dere's already a ghostie here, and 'e's nomming on biscuits!"
                        AppMain.launchDialog(TXDB.get("The event was already added somehow (but perhaps not synced). The button should now have disappeared."));
                    } else {
                        RubyIO map = getMap();
                        if (map == null) {
                            AppMain.launchDialog(TXDB.get("There's no map to get the event from!"));
                            return;
                        }
                        RubyIO ev = map.getInstVarBySymbol("@events").getHashVal(evK);
                        if (ev == null) {
                            AppMain.launchDialog(TXDB.get("So, you saw the ghost, got the Map's properties window via System Tools (or you left it up) to delete the event, then came back and pressed Sync? Or has the software just completely broken?!?!?"));
                            return;
                        }
                        getSaveEvents().hashVal.put(evK, eventAsSaveEvent(getMapId(), evK, ev));
                        pokeHive();
                    }
                }
            };
        return null;
    }

    @Override
    public String customEventsName() {
        return TXDB.get("Player/Vehicles/Events");
    }

    @Override
    public long getEventX(RubyIO a) {
        return getEvent(a).getInstVarBySymbol("@x").fixnumVal;
    }

    @Override
    public long getEventY(RubyIO a) {
        return getEvent(a).getInstVarBySymbol("@y").fixnumVal;
    }

    @Override
    public String getEventName(RubyIO a) {
        RubyIO rio = getEvent(a).getInstVarBySymbol("@name");
        if (rio == null)
            return null;
        return rio.decString();
    }

    @Override
    public void setEventXY(RubyIO a, long x, long y) {
        RubyIO se = getSaveEvents();
        a = se.getHashVal(a);
        if (a == null) {
            AppMain.launchDialog(TXDB.get("The ghost refuses to budge."));
            return;
        }
        a.getInstVarBySymbol("@x").fixnumVal = x;
        a.getInstVarBySymbol("@y").fixnumVal = y;
        pokeHive();
    }

    public void pokeHive() {
        AppMain.objectDB.objectRootModified(saveFileRoot, new SchemaPath(AppMain.schemas.getSDBEntry(saveFileRootSchema), saveFileRoot));
    }
}

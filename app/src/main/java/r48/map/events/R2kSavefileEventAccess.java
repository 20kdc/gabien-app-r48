/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import r48.App;
import r48.dbs.ValueSyntax;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.schema.util.SchemaPath;

import java.util.HashMap;
import java.util.LinkedList;

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
public class R2kSavefileEventAccess extends App.Svc implements IEventAccess {
    public final IObjectBackend.ILoadedObject saveFileRoot;
    public final String saveFileRootId;
    public final String saveFileRootSchema;

    // This only contains the living.
    // The ghosts are added dynamically by getEventKeys & getEvent
    public final HashMap<DMKey, IRIO> eventsHash = new HashMap<>();

    public R2kSavefileEventAccess(App app, String rootId, IObjectBackend.ILoadedObject root, String rootSchema) {
        super(app);
        saveFileRoot = root;
        saveFileRootId = rootId;
        saveFileRootSchema = rootSchema;
        int mapId = (int) getMapId();
        // Inject 'events'
        IRIO sfr = saveFileRoot.getObject();
        sfveInjectEvent("Party", mapId, sfr.getIVar("@party_pos"));
        sfveInjectEvent("Boat", mapId, sfr.getIVar("@boat_pos"));
        sfveInjectEvent("Ship", mapId, sfr.getIVar("@ship_pos"));
        sfveInjectEvent("Airship", mapId, sfr.getIVar("@airship_pos"));
        // Inject actual events
        IRIO se = getSaveEvents();
        for (DMKey k : se.getHashKeys())
            if (eventsHash.get(k) == null)
                eventsHash.put(k, se.getHashVal(k));
    }

    private long getMapId() {
        return saveFileRoot.getObject().getIVar("@party_pos").getIVar("@map").getFX();
    }

    private IRIO getSaveEvents() {
        return saveFileRoot.getObject().getIVar("@map_info").getIVar("@events");
    }

    private void sfveInjectEvent(String s, int mapId, IRIO instVarBySymbol) {
        if (instVarBySymbol.getIVar("@map").getFX() != mapId)
            return;
        eventsHash.put(DMKey.ofStr(s), instVarBySymbol);
    }

    @Override
    public LinkedList<DMKey> getEventKeys() {
        LinkedList<DMKey> keys = new LinkedList<DMKey>(eventsHash.keySet());
        IRIO map = getMap();
        if (map != null)
            if (getSaveCount(map).getFX() != saveFileRoot.getObject().getIVar("@party_pos").getIVar("@map_save_count").getFX())
                for (DMKey k : map.getIVar("@events").getHashKeys())
                    if (eventsHash.get(k) == null)
                        keys.add(k); // Add ghost
        return keys;
    }

    @Override
    public IRIO getEvent(DMKey key) {
        IRIO v = eventsHash.get(key);
        if (v != null)
            return v;
        // Ghost! (?)
        IRIO map = getMap();
        if (map == null)
            return null; // shouldn't happen
        return getMapEvent(map, key);
    }

    private IRIO getMap() {
        int mapId = (int) saveFileRoot.getObject().getIVar("@party_pos").getIVar("@map").getFX();
        IObjectBackend.ILoadedObject ilo = app.odb.getObject(R2kRMLikeMapInfoBackend.sNameFromInt(mapId), null);
        if (ilo == null)
            return null;
        return ilo.getObject();
    }

    private IRIO getMapEvent(IRIO map, DMKey key) {
        return map.getIVar("@events").getHashVal(key);
    }

    @Override
    public void delEvent(DMKey key) {
        if (key.getType() == '"') {
            if (key.decString().equals("Player")) {
                app.ui.launchDialog(T.z.r2kSavefile_errPlyDel);
            } else {
                IRIO rio = getEvent(key);
                if (rio == null) {
                    app.ui.launchDialog(T.z.r2kSavefile_errGone);
                } else {
                    rio.getIVar("@map").setFX(0);
                    app.ui.launchDialog(T.z.r2kSavefile_plyMap0);
                    pokeHive();
                }
            }
        } else {
            IRIO se = getSaveEvents();
            if (se.getHashVal(key) == null) {
                app.ui.launchDialog(T.z.r2kSavefile_errAlreadyGhost);
            } else {
                se.removeHashVal(key);
                IRIO map = getMap();
                boolean ghost = false;
                if (map != null)
                    if (getSaveCount(map).getFX() != saveFileRoot.getObject().getIVar("@party_pos").getIVar("@map_save_count").getFX())
                        ghost = true;
                if (ghost) {
                    app.ui.launchDialog(T.z.r2kSavefile_evGhosted);
                } else {
                    app.ui.launchDialog(T.z.r2kSavefile_evRemovalOk);
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
    public DMKey addEvent(RORIO eve, int type) {
        app.ui.launchDialog(T.z.r2kSavefile_cantAddEvents);
        return null;
    }

    public static void eventAsSaveEvent(App app, IRIO rMap, long mapId, DMKey key, IRIO event) {
        IRIO rio = rMap.addHashVal(key);
        SchemaPath.setDefaultValue(rio, app.sdb.getSDBEntry("RPG::SaveMapEvent"), key);
        rio.getIVar("@map").setFX(mapId);
        rio.getIVar("@x").setDeepClone(event.getIVar("@x"));
        rio.getIVar("@y").setDeepClone(event.getIVar("@y"));

        IRIO pages = event.getIVar("@pages");
        IRIO eventPage = null;
        if (pages.getALen() >= 2)
            eventPage = pages.getAElem(1);
        if (eventPage != null) {
            rio.getIVar("@character_name").setDeepClone(eventPage.getIVar("@character_name"));
            rio.getIVar("@character_index").setDeepClone(eventPage.getIVar("@character_index"));
            rio.getIVar("@character_direction").setDeepClone(eventPage.getIVar("@character_direction"));

            rio.getIVar("@dir").setDeepClone(eventPage.getIVar("@character_direction"));
            rio.getIVar("@transparency").setFX((eventPage.getIVar("@character_blend_mode").getType() == 'T') ? 3 : 0);

            rio.getIVar("@move_freq").setDeepClone(eventPage.getIVar("@move_freq"));
            rio.getIVar("@move_speed").setDeepClone(eventPage.getIVar("@move_speed"));

            rio.getIVar("@layer").setDeepClone(eventPage.getIVar("@layer"));
            rio.getIVar("@block_other_events").setDeepClone(eventPage.getIVar("@block_other_events"));
            // with any luck the moveroute issue will solve itself. with luck.
        }
    }

    @Override
    public String[] getEventSchema(DMKey key) {
        if (key.getType() == '"') {
            if (key.decString().equals("Party"))
                return new String[] {"RPG::SavePartyLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key)};
            if (key.decString().equals("Boat"))
                return new String[] {"RPG::SaveVehicleLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key)};
            if (key.decString().equals("Ship"))
                return new String[] {"RPG::SaveVehicleLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key)};
            if (key.decString().equals("Airship"))
                return new String[] {"RPG::SaveVehicleLocation", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key)};
        }
        // Used for ghosts
        if (eventsHash.get(key) == null)
            return new String[] {"OPAQUE", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key)};
        return new String[] {"RPG::SaveMapEvent", saveFileRootId, saveFileRootSchema, ValueSyntax.encode(key)};
    }

    @Override
    public int getEventType(DMKey evK) {
        // for cloning
        return 1;
    }

    @Override
    public Runnable hasSync(final DMKey evK) {
        // Ghost...!
        if (eventsHash.get(evK) == null)
            return () -> {
                // "Naw! Ghostie want biscuits!"
                if (eventsHash.get(evK) != null) {
                    // "Dere's already a ghostie here, and 'e's nomming on biscuits!"
                    app.ui.launchDialog(T.z.r2kSavefile_evAppearedInCB);
                } else {
                    IRIO map = getMap();
                    if (map == null) {
                        app.ui.launchDialog(T.z.r2kSavefile_noEvMap);
                        return;
                    }
                    IRIO ev = map.getIVar("@events").getHashVal(evK);
                    if (ev == null) {
                        app.ui.launchDialog(T.z.r2kSavefile_errUserIsAToaster);
                        return;
                    }
                    eventAsSaveEvent(app, getSaveEvents(), getMapId(), evK, ev);
                    pokeHive();
                }
            };
        return null;
    }

    @Override
    public String customEventsName() {
        return T.z.r2kSavefile_name;
    }

    @Override
    public long getEventX(DMKey a) {
        return getEvent(a).getIVar("@x").getFX();
    }

    @Override
    public long getEventY(DMKey a) {
        return getEvent(a).getIVar("@y").getFX();
    }

    @Override
    public String getEventName(DMKey a) {
        IRIO rio = getEvent(a).getIVar("@name");
        if (rio == null)
            return null;
        return rio.decString();
    }

    @Override
    public void setEventXY(DMKey a, long x, long y) {
        IRIO se = getSaveEvents();
        IRIO ev = se.getHashVal(a);
        if (ev == null) {
            app.ui.launchDialog(T.z.r2kSavefile_errGhostUnmovable);
            return;
        }
        ev.getIVar("@x").setFX(x);
        ev.getIVar("@y").setFX(y);
        pokeHive();
    }

    public void pokeHive() {
        app.odb.objectRootModified(saveFileRoot, new SchemaPath(app.sdb.getSDBEntry(saveFileRootSchema), saveFileRoot));
    }
}

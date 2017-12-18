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

import java.util.LinkedList;
import java.util.Map;

/**
 * Created in the early hours of December 18th, 2017
 */
public class R2kSavefileEventAccess implements IEventAccess {
    public final RubyIO saveFileRoot;

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
        return new LinkedList<RubyIO>(eventsHash.hashVal.keySet());
    }

    @Override
    public RubyIO getEvent(RubyIO key) {
        return eventsHash.getHashVal(key);
    }

    @Override
    public void delEvent(RubyIO key) {
        AppMain.launchDialog(TXDB.get("You can't delete events in saves. You can set their @active to false, though."));
    }

    @Override
    public String[] eventTypes() {
        return new String[] {};
    }

    @Override
    public RubyIO addEvent(RubyIO eve, int type) {
        return null;
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
        return "RPG::SaveMapEvent";
    }

    @Override
    public int getEventType(RubyIO evK) {
        return 0;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import r48.AppMain;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.dbs.TXDB;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * EventAccess implementation for the general case.
 * This instance can be reused.
 * Created on December 15th 2017
 */
public class TraditionalEventAccess implements IEventAccess {
    private final RubyIO mapRoot;
    private final String eventsPath;
    private final int eventIdBase;
    private final String eventSchema;
    private final String eventsName, eventName;

    public TraditionalEventAccess(RubyIO base, String path, int b, String schema) {
        mapRoot = base;
        eventsPath = path;
        eventIdBase = b;
        eventSchema = schema;
        eventsName = TXDB.get("Events");
        eventName = TXDB.get("+ Add Event");
    }

    public TraditionalEventAccess(RubyIO base, String path, int b, String schema, String en, String en2) {
        mapRoot = base;
        eventsPath = path;
        eventIdBase = b;
        eventSchema = schema;
        eventsName = en;
        eventName = en2;
    }

    @Override
    public LinkedList<RubyIO> getEventKeys() {
        LinkedList<RubyIO> contents = new LinkedList<RubyIO>();
        RubyIO mapEvents = getMapEvents();
        contents.addAll(mapEvents.hashVal.keySet());
        return contents;
    }

    @Override
    public RubyIO getEvent(RubyIO key) {
        RubyIO mapEvents = getMapEvents();
        return mapEvents.getHashVal(key);
    }

    @Override
    public void delEvent(RubyIO key) {
        RubyIO mapEvents = getMapEvents();
        mapEvents.removeHashVal(key);
    }

    @Override
    public String[] eventTypes() {
        return new String[] {
                eventName
        };
    }

    @Override
    public RubyIO addEvent(RubyIO eve, int type) {
        RubyIO key = new RubyIO().setFX(getFreeIndex());
        if (eve == null)
            eve = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry(eventSchema), key);
        RubyIO mapEvents = getMapEvents();
        mapEvents.hashVal.put(key, eve);
        return key;
    }

    @Override
    public String getEventSchema(RubyIO key) {
        return eventSchema;
    }

    @Override
    public int getEventType(RubyIO evK) {
        return 0;
    }

    @Override
    public Runnable hasSync(RubyIO evK) {
        return null;
    }

    @Override
    public String customEventsName() {
        return eventsName;
    }

    private int getFreeIndex() {
        int unusedIndex = eventIdBase;
        RubyIO mapEvents = getMapEvents();
        while (mapEvents.getHashVal(new RubyIO().setFX(unusedIndex)) != null)
            unusedIndex++;
        return unusedIndex;
    }

    public RubyIO getMapEvents() {
        return PathSyntax.parse(mapRoot, eventsPath, true);
    }
}

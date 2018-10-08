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
import r48.dbs.ValueSyntax;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * EventAccess implementation for the general case.
 * This instance can be reused.
 * Created on December 15th 2017
 */
public class TraditionalEventAccess implements IEventAccess {
    private final String mapRootId, mapRootSchema, eventsPath;
    private final RubyIO mapRoot;
    private final int eventIdBase;
    private final String eventSchema;
    private final String eventsName, eventName;
    private final String propPathX, propPathY, propPathName;

    public TraditionalEventAccess(String baseOId, String baseSchema, String path, int b, String schema) {
        this(baseOId, baseSchema, path, b, schema, "@x", "@y", "@name");
    }

    public TraditionalEventAccess(String baseOId, String baseSchema, String path, int b, String schema, String pathX, String pathY, String pathName) {
        this(baseOId, baseSchema, path, b, schema, pathX, pathY, pathName, TXDB.get("Events"), TXDB.get("+ Add Event"));
    }

    public TraditionalEventAccess(String baseOId, String baseSchema, String path, int b, String schema, String pathX, String pathY, String pathName, String en, String en2) {
        mapRootId = baseOId;
        mapRootSchema = baseSchema;
        mapRoot = AppMain.objectDB.getObject(mapRootId, baseSchema);
        eventsPath = path;
        eventIdBase = b;
        eventSchema = schema;
        eventsName = en;
        eventName = en2;
        propPathX = pathX;
        propPathY = pathY;
        propPathName = pathName;
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
        pokeHive();
    }

    @Override
    public String[] eventTypes() {
        return new String[] {
                eventName
        };
    }

    @Override
    public RubyIO addEvent(RubyIO eve, int type) {
        RubyIO key = getFreeIndex();
        if (eve == null)
            eve = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry(eventSchema), key);
        RubyIO mapEvents = getMapEvents();
        mapEvents.hashVal.put(key, eve);
        pokeHive();
        return key;
    }

    @Override
    public String[] getEventSchema(RubyIO key) {
        return new String[] {
                eventSchema,
                mapRootId,
                mapRootSchema,
                ValueSyntax.encode(key)
        };
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

    @Override
    public long getEventX(RubyIO a) {
        return PathSyntax.parse(getEvent(a), propPathX).fixnumVal;
    }

    @Override
    public long getEventY(RubyIO a) {
        return PathSyntax.parse(getEvent(a), propPathY).fixnumVal;
    }

    @Override
    public void setEventXY(RubyIO a, long x, long y) {
        RubyIO ev = getEvent(a);
        if (ev == null)
            return;
        PathSyntax.parse(getEvent(a), propPathX).setFX(x);
        PathSyntax.parse(getEvent(a), propPathY).setFX(y);
        pokeHive();
    }

    @Override
    public String getEventName(RubyIO a) {
        RubyIO iv = PathSyntax.parse(getEvent(a), propPathName);
        if (iv == null)
            return null;
        return iv.decString();
    }

    private RubyIO getFreeIndex() {
        long unusedIndex = eventIdBase;
        RubyIO mapEvents = getMapEvents();
        while (mapEvents.getHashVal(convIndex(unusedIndex)) != null)
            unusedIndex++;
        return convIndex(unusedIndex);
    }

    protected RubyIO convIndex(long unusedIndex) {
        return new RubyIO().setFX(unusedIndex);
    }

    public RubyIO getMapEvents() {
        return PathSyntax.parse(mapRoot, eventsPath);
    }

    private void pokeHive() {
        AppMain.objectDB.objectRootModified(mapRoot, new SchemaPath(AppMain.schemas.getSDBEntry(mapRootSchema), mapRoot));
    }
}

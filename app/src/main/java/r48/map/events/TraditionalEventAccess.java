/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import r48.App;
import r48.dbs.PathSyntax;
import r48.dbs.ValueSyntax;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.util.SchemaPath;

import java.util.Collections;
import java.util.LinkedList;

/**
 * EventAccess implementation for the general case.
 * This instance can be reused.
 * Created on December 15th 2017
 */
public class TraditionalEventAccess extends App.Svc implements IEventAccess {
    private final String mapRootId, mapRootSchema;
    private final IObjectBackend.ILoadedObject mapRoot;
    private final int eventIdBase;
    private final String eventSchema;
    private final String eventsName, eventName;
    private final PathSyntax propPathX, propPathY, propPathName, eventsPath;

    public TraditionalEventAccess(App app, String baseOId, String baseSchema, String path, int b, String schema) {
        this(app, baseOId, baseSchema, path, b, schema, "@x", "@y", "@name");
    }

    public TraditionalEventAccess(App app, String baseOId, String baseSchema, String path, int b, String schema, String pathX, String pathY, String pathName) {
        this(app, baseOId, baseSchema, path, b, schema, pathX, pathY, pathName, app.t.z.l222, app.t.z.l223);
    }

    public TraditionalEventAccess(App app, String baseOId, String baseSchema, String path, int b, String schema, String pathX, String pathY, String pathName, String en, String en2) {
        super(app);
        mapRootId = baseOId;
        mapRootSchema = baseSchema;
        mapRoot = app.odb.getObject(mapRootId, baseSchema);
        eventsPath = PathSyntax.compile(app, path);
        eventIdBase = b;
        eventSchema = schema;
        eventsName = en;
        eventName = en2;
        propPathX = PathSyntax.compile(app, pathX);
        propPathY = PathSyntax.compile(app, pathY);
        propPathName = PathSyntax.compile(app, pathName);
    }

    @Override
    public LinkedList<DMKey> getEventKeys() {
        LinkedList<DMKey> contents = new LinkedList<>();
        IRIO mapEvents = getMapEvents();
        Collections.addAll(contents, mapEvents.getHashKeys());
        return contents;
    }

    @Override
    public IRIO getEvent(DMKey key) {
        IRIO mapEvents = getMapEvents();
        return mapEvents.getHashVal(key);
    }

    @Override
    public void delEvent(DMKey key) {
        IRIO mapEvents = getMapEvents();
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
    public DMKey addEvent(RORIO eve, int type) {
        DMKey key = getFreeIndex();
        IRIO mapEvents = getMapEvents();
        IRIO eveTarget = mapEvents.addHashVal(key);
        if (eve == null) {
            SchemaPath.setDefaultValue(eveTarget, app.sdb.getSDBEntry(eventSchema), key);
        } else {
            eveTarget.setDeepClone(eve);
        }
        pokeHive();
        return key;
    }

    @Override
    public String[] getEventSchema(DMKey key) {
        return new String[] {
                eventSchema,
                mapRootId,
                mapRootSchema,
                ValueSyntax.encode(key)
        };
    }

    @Override
    public int getEventType(DMKey evK) {
        return 0;
    }

    @Override
    public Runnable hasSync(DMKey evK) {
        return null;
    }

    @Override
    public String customEventsName() {
        return eventsName;
    }

    @Override
    public long getEventX(DMKey a) {
        return propPathX.get(getEvent(a)).getFX();
    }

    @Override
    public long getEventY(DMKey a) {
        return propPathY.get(getEvent(a)).getFX();
    }

    @Override
    public void setEventXY(DMKey a, long x, long y) {
        IRIO ev = getEvent(a);
        if (ev == null)
            return;
        propPathX.get(getEvent(a)).setFX(x);
        propPathY.get(getEvent(a)).setFX(y);
        pokeHive();
    }

    @Override
    public String getEventName(DMKey a) {
        IRIO iv = propPathName.get(getEvent(a));
        if (iv == null)
            return null;
        return iv.decString();
    }

    private DMKey getFreeIndex() {
        long unusedIndex = eventIdBase;
        IRIO mapEvents = getMapEvents();
        while (mapEvents.getHashVal(convIndex(unusedIndex)) != null)
            unusedIndex++;
        return convIndex(unusedIndex);
    }

    protected DMKey convIndex(long unusedIndex) {
        return DMKey.of(unusedIndex);
    }

    public IRIO getMapEvents() {
        return eventsPath.get(mapRoot.getObject());
    }

    private void pokeHive() {
        app.odb.objectRootModified(mapRoot, new SchemaPath(app.sdb.getSDBEntry(mapRootSchema), mapRoot));
    }
}

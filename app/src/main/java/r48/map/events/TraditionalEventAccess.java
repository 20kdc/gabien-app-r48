/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import r48.App;
import r48.dbs.PathSyntax;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.MockLoadedObject;
import r48.io.data.DMKey;
import r48.io.data.IDM3Context;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * EventAccess implementation for the general case.
 * This instance can be reused.
 * Created on December 15th 2017
 */
public class TraditionalEventAccess extends App.Svc implements IEventAccess {
    private final IObjectBackend.ILoadedObject mapRoot;
    private final int eventIdBase;
    private final SchemaElement mapRootSchema, eventSchema;
    private final String eventsName, eventName;
    private final PathSyntax propPathX, propPathY, propPathName, eventsPath;

    public TraditionalEventAccess(App app, String mapRootId, String baseSchema, String path, int b, String schema) {
        this(app, mapRootId, baseSchema, path, b, schema, "@x", "@y", "@name");
    }

    public TraditionalEventAccess(App app, String mapRootId, String baseSchema, String path, int b, String schema, String pathX, String pathY, String pathName) {
        this(app, mapRootId, baseSchema, path, b, schema, pathX, pathY, pathName, app.t.m.events, app.t.m.addEvent);
    }

    public TraditionalEventAccess(App app, String mapRootId, String baseSchema, String path, int b, String schema, String pathX, String pathY, String pathName, String en, String en2) {
        super(app);
        mapRootSchema = app.sdb.getSDBEntry(baseSchema);
        mapRoot = app.odb.getObject(mapRootId, baseSchema);
        eventsPath = PathSyntax.compile(app, path);
        eventIdBase = b;
        eventSchema = app.sdb.getSDBEntry(schema);
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
    public @Nullable DMKey addEvent(@Nullable RORIO eve, int type) {
        DMKey key = getFreeIndex();
        IRIO mapEvents = getMapEvents();
        IRIO eveTarget = mapEvents.addHashVal(key);
        if (eve == null) {
            SchemaPath.setDefaultValue(eveTarget, eventSchema, key);
            pokeHive();
        } else {
            // we don't trust this value at all, hold in an intermediary and bash it around a bit
            IRIOGeneric ig = new IRIOGeneric(IDM3Context.Null.INSTANCE, app.encoding);
            ig.setDeepClone(eve);
            new SchemaPath(eventSchema, new MockLoadedObject(ig)).changeOccurred(false);
            // now we're sure it's safe...
            eveTarget.setDeepClone(ig);
            // and just to be sure
            makeEventSchemaPath(key).changeOccurred(false);
        }
        return key;
    }

    @Override
    public @Nullable EventSchema getEventSchema(DMKey key) {
        return new EventSchema(mapRootSchema, eventSchema, mapRoot, key);
    }

    @Override
    public int getEventTypeFromKey(DMKey evK) {
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
        return propPathX.getRO(getEvent(a)).getFX();
    }

    @Override
    public long getEventY(DMKey a) {
        return propPathY.getRO(getEvent(a)).getFX();
    }

    @Override
    public void setEventXY(DMKey a, long x, long y) {
        IRIO ev = getEvent(a);
        if (ev == null)
            return;
        propPathX.getRW(getEvent(a)).setFX(x);
        propPathY.getRW(getEvent(a)).setFX(y);
        pokeHive();
    }

    @Override
    public String getEventName(DMKey a) {
        RORIO iv = propPathName.getRO(getEvent(a));
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
        return eventsPath.getRW(mapRoot.getObject());
    }

    private SchemaPath makeMapRootSchemaPath() {
        return new SchemaPath(mapRootSchema, mapRoot);
    }
    private SchemaPath makeEventSchemaPath(DMKey key) {
        SchemaPath base = makeMapRootSchemaPath();
        base = base.arrayHashIndex(key, "E" + key);
        IRIO res = getEvent(key);
        return base.newWindow(eventSchema, res);
    }

    private void pokeHive() {
        app.odb.objectRootModified(mapRoot, makeMapRootSchemaPath());
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import r48.AppMain;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * EventAccess implementation for the general case.
 * Created on December 15th 2017
 */
public class TraditionalEventAccess implements IEventAccess {
    private final RubyIO mapEvents;
    private final int eventIdBase;
    private final String eventSchema;

    public TraditionalEventAccess(RubyIO e, int b, String schema) {
        mapEvents = e;
        eventIdBase = b;
        eventSchema = schema;
    }

    @Override
    public LinkedList<RubyIO> getEventKeys() {
        LinkedList<RubyIO> contents = new LinkedList<RubyIO>();
        if (mapEvents != null)
            contents.addAll(mapEvents.hashVal.keySet());
        return contents;
    }

    @Override
    public RubyIO getEvent(RubyIO key) {
        return mapEvents.getHashVal(key);
    }

    @Override
    public void delEvent(RubyIO key) {
        mapEvents.removeHashVal(key);
    }

    @Override
    public RubyIO addEvent(RubyIO eve) {
        RubyIO key = new RubyIO().setFX(getFreeIndex());
        if (eve == null)
            eve = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry(eventSchema), key);
        mapEvents.hashVal.put(key, eve);
        return key;
    }

    @Override
    public String getEventSchema(RubyIO key) {
        return eventSchema;
    }

    private int getFreeIndex() {
        int unusedIndex = eventIdBase;
        while (mapEvents.getHashVal(new RubyIO().setFX(unusedIndex)) != null)
            unusedIndex++;
        return unusedIndex;
    }
}

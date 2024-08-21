/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to abstract @events
 * Note that this is now part of the state as of 2:20 AM Dec 18th 2017, because of LUDICROUS EFFICIENCY BOOSTS,
 *  because it can rely on knowing when there's been a change via the hair-sensitive modification listeners...
 * ...rather than recreating getEventKeys and such every single time.
 * Created on December 15th 2017
 */
public interface IEventAccess {
    // Note that you should call the relevant modification trigger after all modifications.
    // This should be empty for non-Map event renderers.
    LinkedList<DMKey> getEventKeys();

    // Should return null on event not available
    IRIO getEvent(DMKey key);

    // Should do nothing on event not available
    // *EXPECTED TO RUN MODIFICATION ALERTER BY ITSELF*
    void delEvent(DMKey key);

    // Returns the "add event" strings (strings may or may not be null, but the array must not be null)
    String[] eventTypes();

    /**
     * Creates an event of the given type, with a possible template.
     * Returns the new key, or null on failure.
     * The implementation of this function is expected to run the modification alerter by itself.
     * The implementation is also expected to deep clone the event itself.
     * The implementation is also expected to check value compatibility itself.
     * Note that implementations are expected to launch dialogs to report errors.
     */
    @Nullable DMKey addEvent(@Nullable RORIO eve, int type);

    /**
     * Event schema details.
     * Returns null if the event isn't available.
     */
    @Nullable EventSchema getEventSchema(DMKey key);

    public static class EventSchema {
        public final SchemaElement rootSchema, eventSchema;
        public final ObjectRootHandle root;
        public final DMKey key;
        public EventSchema(SchemaElement rs, SchemaElement es, ObjectRootHandle r, DMKey key) {
            rootSchema = rs;
            eventSchema = es;
            root = r;
            this.key = key;
        }
    }

    /**
     * Gets an event type from the event key.
     */
    int getEventTypeFromKey(DMKey evK);

    // If this returns something, then the event is read-only, but has a button marked "Sync" which is expected to cause modifications
    // Yes, this is a cop-out because I can't think of a better design r/n
    // everything else I thought up was just hacky or overabstracting
    Runnable hasSync(DMKey evK);

    // Name of "Events" panel. Cannot be null
    String customEventsName();

    // Given an event key, return X.
    // Can error if the event does not exist.
    long getEventX(DMKey key);

    // Given an event key, return Y.
    // Can error if the event does not exist.
    long getEventY(DMKey key);

    // Given an event key, set XY.
    // Does nothing if the event does not exist.
    // *EXPECTED TO RUN MODIFICATION ALERTER BY ITSELF*
    void setEventXY(DMKey key, long x, long y);

    // Given an event key, return a name or null.
    // Can error if the event does not exist.
    String getEventName(DMKey key);
}

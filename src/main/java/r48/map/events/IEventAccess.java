/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import r48.RubyIO;
import r48.io.data.IRIO;

import java.util.LinkedList;

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
    LinkedList<IRIO> getEventKeys();

    // Should return null on event not available
    IRIO getEvent(IRIO key);

    // Should do nothing on event not available
    // *EXPECTED TO RUN MODIFICATION ALERTER BY ITSELF*
    void delEvent(IRIO key);

    // Returns the "add event" strings (strings may or may not be null, but the array must not be null)
    String[] eventTypes();

    // returns one of:
    // null for fail & do nothing
    // the key
    // *EXPECTED TO RUN MODIFICATION ALERTER BY ITSELF*
    IRIO addEvent(RubyIO eve, int type);

    // {eventSchema, root, rootSchema, keyValueSyntax}
    // Should return null on event not available
    String[] getEventSchema(IRIO key);

    int getEventType(IRIO evK);

    // If this returns something, then the event is read-only, but has a button marked "Sync" which is expected to cause modifications
    // Yes, this is a cop-out because I can't think of a better design r/n
    // everything else I thought up was just hacky or overabstracting
    Runnable hasSync(IRIO evK);

    // Name of "Events" panel. Cannot be null
    String customEventsName();

    // Given an event key, return X.
    // Can error if the event does not exist.
    long getEventX(IRIO key);

    // Given an event key, return Y.
    // Can error if the event does not exist.
    long getEventY(IRIO key);

    // Given an event key, set XY.
    // Does nothing if the event does not exist.
    // *EXPECTED TO RUN MODIFICATION ALERTER BY ITSELF*
    void setEventXY(IRIO key, long x, long y);

    // Given an event key, return a name or null.
    // Can error if the event does not exist.
    String getEventName(IRIO key);
}

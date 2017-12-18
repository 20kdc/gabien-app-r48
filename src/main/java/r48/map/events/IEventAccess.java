/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import r48.RubyIO;

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
    LinkedList<RubyIO> getEventKeys();

    RubyIO getEvent(RubyIO key);

    void delEvent(RubyIO key);

    // Returns the "add event" strings (strings may or may not be null, but the array must not be null)
    String[] eventTypes();

    // returns the key, or null for failure
    RubyIO addEvent(RubyIO eve, int type);

    String getEventSchema(RubyIO key);

    int getEventType(RubyIO evK);
}

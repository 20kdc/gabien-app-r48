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
 * Created on December 15th 2017
 */
public interface IEventAccess {
    // Note that you should call the relevant modification trigger after all modifications.
    // This should be empty for non-Map event renderers.
    LinkedList<RubyIO> getEventKeys();

    RubyIO getEvent(RubyIO key);

    void delEvent(RubyIO key);

    // returns the key
    RubyIO addEvent(RubyIO eve);
}

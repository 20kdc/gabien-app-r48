/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import java.util.LinkedList;

/**
 * A Map System that has additional dynamic objects.
 * Created on September 04, 2018.
 */
public interface IDynobjMapSystem {
    // Returns all dynamic object IDs that are part of the game.
    // Object IDs that are not part of the game (save files) do not count.
    LinkedList<String> getDynamicObjects();
}

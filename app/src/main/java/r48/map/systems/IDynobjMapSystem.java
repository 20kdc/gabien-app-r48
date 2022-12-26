/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import r48.dbs.ObjectInfo;

/**
 * A Map System that has additional dynamic objects.
 * Created on September 04, 2018.
 */
public interface IDynobjMapSystem {
    /**
     * Returns all dynamic object IDs that are part of the game.
     * Object IDs that are not part of the game (save files) do not count.
     */
    ObjectInfo[] getDynamicObjects();
}

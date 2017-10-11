/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

/**
 * Acts as a getter/setter for the current map for dictionaries & r2ksystemdefaults game bootstrap code.
 * Also serves as a way for a certain schema element to get ahold of a UIMapView portably.
 * Created on 08/06/17.
 */
public interface IMapContext {
    // Returns null if none loaded.
    String getCurrentMap();

    // Loads a map by it's referent.
    void loadMap(RubyIO reference);

    // Shuts down internal caching as R48 reverts to the launcher.
    void freeOsbResources();
}

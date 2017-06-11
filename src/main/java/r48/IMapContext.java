/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48;

import r48.map.UIMapView;

/**
 * Acts as a getter/setter for the current map for dictionaries & r2ksystemdefaults game bootstrap code.
 * Also serves as a way for a certain schema element to get ahold of a UIMapView portably.
 * Created on 08/06/17.
 */
public interface IMapContext {
    // Returns null if none loaded.
    String getCurrentMap();
    void loadMap(String s);
    // Shuts down internal caching as R48 reverts to the launcher.
    void freeOsbResources();
}

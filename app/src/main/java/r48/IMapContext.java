/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

/**
 * Ties together the Map tab and everything related.
 * Here's how it'll work from now on.
 * Something loadable in the Map panel is referred to by a MapSystem implementation defined string (a GUM).
 * The format must be documented in the MapSystem comment header.
 * The GUM is passed to the MapContext, which starts up the UIMapViewContainer,
 *  which in turn passes the GUM to the UIMapView and grabs the toolbar via the MapSystem.
 * The UIMapView does it's own setup via the MapSystem, and that's the basics initialized.
 * Created on 08/06/17.
 */
public interface IMapContext {
    // NOTE: This is for __MAP__ purposes. The object should not be written to.
    // Returns null if none loaded or if no __MAP__ mapping is available.
    String getCurrentMapObject();

    // Loads a map by it's GUM, or null to revert to No Map Selected.
    void loadMap(String gum);

    // Shuts down internal caching as R48 reverts to the launcher.
    void freeOsbResources();

    // Used indirectly by the image editor when a file is saved.
    void performCacheFlush();

    // Flushes IRIO references, while trying to preserve as much as possible.
    void performIRIOFlush();

    // Helps prevent an awful lot of object pipelining.
    App getApp();
}

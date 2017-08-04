/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map.drawlayers;

import gabien.IGrDriver;
import r48.map.IMapViewCallbacks;

/**
 * Responsible for handling the general structure of rendering.
 * Most things can use the Traditional Layer Controller.
 * Created on 08/06/17.
 */
public interface IMapViewDrawLayer {
    String getName();

    void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd);
}

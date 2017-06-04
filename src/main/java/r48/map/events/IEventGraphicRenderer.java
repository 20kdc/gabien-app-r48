/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.events;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import r48.RubyIO;

/**
 * Events need to have a @x, and a @y, (@name is optional) but anything else about them is determined by this and the Schema.
 * Created on 1/27/17.
 */
public interface IEventGraphicRenderer {
    // The number returned is NOT a map layer, but a position in the map draw order.
    // The event is drawn after the map layer is.
    // -1 is before any map layers.
    int determineEventLayer(RubyIO event);

    // This adds layers after the tile layers. tileLayerCount == first extra layer.
    int extraEventLayers();

    RubyIO extractEventGraphic(RubyIO event);

    // while this handles event graphics
    void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd);

    int eventIdBase();
}

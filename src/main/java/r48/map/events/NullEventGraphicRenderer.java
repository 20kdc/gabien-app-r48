/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.events;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import r48.RubyIO;

/**
 * For WIP/no-event handlers.
 * Created on 1/27/17.
 */
public class NullEventGraphicRenderer implements IEventGraphicRenderer {
    @Override
    public int determineEventLayer(RubyIO event) {
        return 0;
    }

    @Override
    public RubyIO extractEventGraphic(RubyIO event) {
        return event;
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd) {

    }

    @Override
    public int eventIdBase() {
        return 0;
    }
}

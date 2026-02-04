/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map2d.events;

import org.eclipse.jdt.annotation.Nullable;

import gabien.render.IGrDriver;
import r48.io.data.RORIO;

/**
 * Events need to have a @x, and a @y, (@name is optional) but anything else about them is determined by this and the Schema.
 * Created on 1/27/17.
 */
public interface IEventGraphicRenderer {
    // Only used for EventMapViewDrawLayer
    int determineEventLayer(RORIO event);

    RORIO extractEventGraphic(RORIO event);

    // while this handles event graphics
    // original event is passed for features like @step_anime
    void drawEventGraphic(RORIO target, int ox, int oy, IGrDriver igd, int sprScale, @Nullable RORIO originalEvent);
}

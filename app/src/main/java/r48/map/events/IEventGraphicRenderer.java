/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import gabien.IGrDriver;
import r48.io.data.IRIO;

/**
 * Events need to have a @x, and a @y, (@name is optional) but anything else about them is determined by this and the Schema.
 * Created on 1/27/17.
 */
public interface IEventGraphicRenderer {
    // Only used for EventMapViewDrawLayer
    int determineEventLayer(IRIO event);

    IRIO extractEventGraphic(IRIO event);

    // while this handles event graphics
    void drawEventGraphic(IRIO target, int ox, int oy, IGrDriver igd, int sprScale);
}

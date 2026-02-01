/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.render2d.MapViewDrawContext;

/**
 * Responsible for handling the general structure of rendering.
 * Most things can use the Traditional Layer Controller.
 * Created on 08/06/17.
 */
public interface IMapViewDrawLayer {
    String getName();

    void draw(MapViewDrawContext mvdc);
}

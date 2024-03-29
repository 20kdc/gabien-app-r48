/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import org.eclipse.jdt.annotation.Nullable;

import gabien.render.IGrDriver;
import r48.io.data.RORIO;

/**
 * For WIP/no-event handlers.
 * Created on 1/27/17.
 */
public class NullEventGraphicRenderer implements IEventGraphicRenderer {
    @Override
    public int determineEventLayer(RORIO event) {
        return 0;
    }

    @Override
    public RORIO extractEventGraphic(RORIO event) {
        return event;
    }

    @Override
    public void drawEventGraphic(RORIO target, int ox, int oy, IGrDriver igd, int ss, @Nullable RORIO originalEvent) {

    }
}

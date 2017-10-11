/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import gabien.IGrDriver;
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
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd, int ss) {

    }

    @Override
    public int eventIdBase() {
        return 0;
    }
}

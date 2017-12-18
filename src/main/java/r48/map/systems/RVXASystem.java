/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.VXATileRenderer;

/**
 * Created on 03/06/17.
 */
public class RVXASystem extends RXPSystem {
    @Override
    public StuffRenderer rendererFromMap(RubyIO map, IEventAccess events) {
        String vxaPano = "";
        if (map != null) {
            vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            if (map.getInstVarBySymbol("@parallax_show").type != 'T')
                vxaPano = "";
        }
        if (!vxaPano.equals(""))
            vxaPano = "Parallaxes/" + vxaPano;

        ITileRenderer tileRenderer = new VXATileRenderer(imageLoader, tsoFromMap(map));
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, StuffRenderer.prepareTraditional(tileRenderer, new int[] {0, 1, 3, 2}, eventRenderer, imageLoader, map, events, vxaPano, false, false, 0, 0, -1, -1, 1));
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
        ITileRenderer tileRenderer = new VXATileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }
}

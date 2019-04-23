/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.ui.IFunction;
import gabien.ui.Rect;
import gabien.ui.Size;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.VXATileRenderer;
import r48.maptools.UIMTBase;
import r48.maptools.UIMTShadowLayer;

/**
 * Created on 03/06/17.
 */
public class RVXASystem extends RXPSystem {
    @Override
    protected Rect getIdealGridForCharacter(String basename, Size img) {
        if (basename.startsWith("$") || basename.startsWith("!$"))
            return new Rect(0, 0, img.width / 3, img.height / 4);
        return new Rect(0, 0, img.width / 12, img.height / 8);
    }

    @Override
    public StuffRenderer rendererFromMapAndTso(IRIO map, IRIO tso, IEventAccess events) {
        String vxaPano = "";
        if (map != null) {
            vxaPano = map.getIVar("@parallax_name").decString();
            if (map.getIVar("@parallax_show").getType() != 'T')
                vxaPano = "";
        }
        if (!vxaPano.equals(""))
            vxaPano = "Parallaxes/" + vxaPano;

        ITileRenderer tileRenderer = new VXATileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, StuffRenderer.prepareTraditional(tileRenderer, new int[] {0, 1, 3, 2}, eventRenderer, imageLoader, map, events, vxaPano, false, false, 0, 0, -1, -1, 1));
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO tso) {
        ITileRenderer tileRenderer = new VXATileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    protected IEditingToolbarController mapEditingToolbar(IMapToolContext iMapToolContext) {
        return new MapEditingToolbarController(iMapToolContext, false, new String[] {
                TXDB.get("Shadow/Region")
        }, new IFunction[] {
                new IFunction<IMapToolContext, UIMTBase>() {
                    @Override
                    public UIMTBase apply(IMapToolContext o) {
                        return new UIMTShadowLayer(o);
                    }
                }
        });
    }

    @Override
    public boolean engineUsesPal0Colourkeys() {
        return true;
    }
}

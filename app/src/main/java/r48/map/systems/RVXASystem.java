/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.IImage;
import gabien.ui.Rect;
import gabien.ui.Size;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.StuffRenderer;
import r48.map.drawlayers.*;
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
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        VXATileRenderer tileRenderer = new VXATileRenderer(imageLoader, tso);
        RMEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        if (map != null) {
            String vxaPano = map.getIVar("@parallax_name").decString();
            if (map.getIVar("@parallax_show").getType() != 'T')
                vxaPano = "";
            IImage panoImg = null;
            if (!vxaPano.equals(""))
                panoImg = imageLoader.getImage("Parallaxes/" + vxaPano, true);
            RubyTable rt = new RubyTable(map.getIVar("@data").getBuffer());
            RVXAAccurateDrawLayer accurate = new RVXAAccurateDrawLayer(rt, events, tileRenderer, eventRenderer);
            layers = new IMapViewDrawLayer[] {
                    // works for green docks
                    new PanoramaMapViewDrawLayer(panoImg, true, true, 0, 0, rt.width, rt.height, -1, -1, 2, 1, 0),
                    // Signal layers (controls Z-Emulation)
                    accurate.tileSignalLayers[0],
                    accurate.tileSignalLayers[1],
                    accurate.tileSignalLayers[2],
                    accurate.tileSignalLayers[3],
                    accurate.signalLayerEvA,
                    // Z-Emulation
                    accurate,
                    // selection
                    new EventMapViewDrawLayer(0x7FFFFFFF, events, eventRenderer, ""),
                    new GridMapViewDrawLayer(),
                    new BorderMapViewDrawLayer(rt.width, rt.height)
            };
        }
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO tso) {
        ITileRenderer tileRenderer = new VXATileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, true);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    protected IEditingToolbarController mapEditingToolbar(IMapToolContext iMapToolContext) {
        return new MapEditingToolbarController(iMapToolContext, false, new ToolButton[] {
                new ToolButton(TXDB.get("Shadow/Region")) {
                    @Override
                    public UIMTBase apply(IMapToolContext o) {
                        return new UIMTShadowLayer(o);
                    }
                }
        }, new ToolButton[] {
                new FindTranslatablesToolButton("RPG::Event::Page")
        });
    }

    @Override
    public boolean engineUsesPal0Colourkeys() {
        return true;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import gabien.render.IImage;
import gabien.ui.Rect;
import gabien.ui.Size;
import r48.App;
import r48.RubyTable;
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
    public RVXASystem(App app) {
        super(app);
    }

    @Override
    protected Rect getIdealGridForCharacter(String basename, Size img) {
        if (basename.startsWith("$") || basename.startsWith("!$"))
            return new Rect(0, 0, img.width / 3, img.height / 4);
        return new Rect(0, 0, img.width / 12, img.height / 8);
    }

    @Override
    public StuffRenderer rendererFromMapAndTso(IRIO map, IRIO tso, IEventAccess events) {
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        VXATileRenderer tileRenderer = new VXATileRenderer(app, imageLoader, tso);
        RMEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(app, imageLoader, tileRenderer, true);
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
                    new PanoramaMapViewDrawLayer(app, panoImg, true, true, 0, 0, rt.width, rt.height, -1, -1, 2, 1, 0),
                    // Signal layers (controls Z-Emulation)
                    accurate.tileSignalLayers[0],
                    accurate.tileSignalLayers[1],
                    accurate.tileSignalLayers[2],
                    accurate.tileSignalLayers[3],
                    accurate.signalLayerEvA,
                    // Z-Emulation
                    accurate,
                    // selection
                    new EventMapViewDrawLayer(app, 0x7FFFFFFF, events, eventRenderer, ""),
                    new GridMapViewDrawLayer(app),
                    new BorderMapViewDrawLayer(app, rt.width, rt.height)
            };
        }
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO tso) {
        ITileRenderer tileRenderer = new VXATileRenderer(app, imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(app, imageLoader, tileRenderer, true);
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    protected IEditingToolbarController mapEditingToolbar(IMapToolContext iMapToolContext) {
        return new MapEditingToolbarController(iMapToolContext, false, new ToolButton[] {
                new ToolButton(T.m.l262) {
                    @Override
                    public UIMTBase apply(IMapToolContext o) {
                        return new UIMTShadowLayer(o);
                    }
                }
        }, new ToolButton[] {
                new FindTranslatablesToolButton(app, "RPG::Event::Page")
        });
    }

    @Override
    public boolean engineUsesPal0Colourkeys() {
        return true;
    }
}

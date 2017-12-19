/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.IImage;
import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.IMapContext;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.drawlayers.EventMapViewDrawLayer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.drawlayers.PanoramaMapViewDrawLayer;
import r48.map.drawlayers.TileMapViewDrawLayer;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.events.TraditionalEventAccess;
import r48.map.imaging.*;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.XPTileRenderer;
import r48.toolsets.RMTranscriptDumper;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created on 03/06/17.
 */
public class RXPSystem extends MapSystem implements IRMMapSystem {
    public RXPSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("Graphics/", "", new ChainedImageLoader(new IImageLoader[] {
                new GabienImageLoader(".png"),
                new GabienImageLoader(".jpg"),
        }))), true);
    }

    protected static RubyIO tsoFromMap(RubyIO map) {
        if (map == null)
            return null;
        RubyIO tileset = null;
        int tid = (int) map.getInstVarBySymbol("@tileset_id").fixnumVal;
        RubyIO tilesets = AppMain.objectDB.getObject("Tilesets");
        if ((tid >= 0) && (tid < tilesets.arrVal.length))
            tileset = tilesets.arrVal[tid];
        if (tileset != null)
            if (tileset.type == '0')
                tileset = null;
        return tileset;
    }

    @Override
    public UIElement createMapExplorer(ISupplier<IConsumer<UIElement>> windowMaker, IMapContext mapBox, String mapInfos) {
        return new UIGRMMapInfos(windowMaker, new RXPRMLikeMapInfoBackend(), mapBox, mapInfos);
    }

    public StuffRenderer rendererFromMap(RubyIO map, IEventAccess events) {
        RubyIO tileset = tsoFromMap(map);
        ITileRenderer tileRenderer = new XPTileRenderer(imageLoader, tileset);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        String pano = "";
        if (tileset != null) {
            RubyIO rio = tileset.getInstVarBySymbol("@panorama_name");
            if (rio != null)
                if (rio.strVal.length > 0)
                    pano = "Panoramas/" + rio.decString();
        }
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        if (map != null) {
            RubyTable rt = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            IImage panoImg = null;
            if (!pano.equals(""))
                panoImg = imageLoader.getImage(pano, true);
            layers = new IMapViewDrawLayer[] {
                    // works for green docks
                    new PanoramaMapViewDrawLayer(panoImg, true, true, 0, 0, rt.width, rt.height, -1, -1, 2, 1, 0),
                    new TileMapViewDrawLayer(rt, 0, tileRenderer),
                    new TileMapViewDrawLayer(rt, 1, tileRenderer),
                    new TileMapViewDrawLayer(rt, 2, tileRenderer),
                    new EventMapViewDrawLayer(0, events, eventRenderer, tileRenderer.getTileSize()),
                    new EventMapViewDrawLayer(1, events, eventRenderer, tileRenderer.getTileSize()),
                    // selection
                    new EventMapViewDrawLayer(0x7FFFFFFF, events, eventRenderer, tileRenderer.getTileSize()),
            };
        }
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
        ITileRenderer tileRenderer = new XPTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    public MapViewDetails mapViewRequest(final String gum, boolean allowCreate) {
        if (!allowCreate)
            if (AppMain.objectDB.getObject(gum, null) == null)
                return null;
        final RubyIO map = AppMain.objectDB.getObject(gum);
        final IEventAccess events = new TraditionalEventAccess(map.getInstVarBySymbol("@events"), 1, "RPG::Event");
        return new MapViewDetails(gum, "RPG::Map", new ISupplier<MapViewState>() {
            @Override
            public MapViewState get() {
                return MapViewState.fromRT(rendererFromMap(map, events), map, "@data", false, events);
            }
        }, new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                return mapEditingToolbar(iMapToolContext);
            }
        });
    }

    protected IEditingToolbarController mapEditingToolbar(IMapToolContext iMapToolContext) {
        return new MapEditingToolbarController(iMapToolContext, false);
    }

    @Override
    public RMMapData[] getAllMaps() {
        LinkedList<RMMapData> rmdList = new LinkedList<RMMapData>();
        for (Map.Entry<RubyIO, RubyIO> rio : AppMain.objectDB.getObject("MapInfos").hashVal.entrySet()) {
            int id = (int) rio.getKey().fixnumVal;
            RMMapData rmd = new RMMapData(rio.getValue().getInstVarBySymbol("@name").decString(), AppMain.objectDB.getObject(RXPRMLikeMapInfoBackend.sNameFromInt(id)), id, RXPRMLikeMapInfoBackend.sNameFromInt(id), "RPG::Map");
            rmdList.add(rmd);
        }
        return rmdList.toArray(new RMMapData[0]);
    }

    @Override
    public RubyIO[] getAllCommonEvents() {
        LinkedList<RubyIO> rmdList = new LinkedList<RubyIO>();
        for (RubyIO rio : AppMain.objectDB.getObject("CommonEvents").arrVal)
            if (rio.type != '0')
                rmdList.add(rio);
        return rmdList.toArray(new RubyIO[0]);
    }

    @Override
    public void dumpCustomData(RMTranscriptDumper dumper) {
        dumper.startFile("Items", TXDB.get("The list of items in the game."));
        LinkedList<String> lls = new LinkedList<String>();
        for (RubyIO page : AppMain.objectDB.getObject("Items").arrVal) {
            if (page.type != '0') {
                lls.add(page.getInstVarBySymbol("@name").decString());
            } else {
                lls.add("<NULL>");
            }
        }
        dumper.dumpBasicList("Names", lls.toArray(new String[0]), 0);
        dumper.endFile();

        dumper.startFile("System", TXDB.get("System data (of any importance, anyway)."));
        RubyIO sys = AppMain.objectDB.getObject("System");

        dumper.dumpHTML(TXDB.get("Notably, switch and variable lists have a 0th index, but only indexes starting from 1 are actually allowed to be used.") + "<br/>");
        dumper.dumpHTML(TXDB.get("Magic number:") + sys.getInstVarBySymbol("@magic_number").toString() + "<br/>");
        dumper.dumpHTML(TXDB.get("Magic number II:") + sys.getInstVarBySymbol("@_").toString() + "<br/>");

        dumper.dumpSVList("@switches", sys.getInstVarBySymbol("@switches").arrVal, 0);
        dumper.dumpSVList("@variables", sys.getInstVarBySymbol("@variables").arrVal, 0);
        dumper.endFile();
    }

    @Override
    public String mapReferentToGUM(RubyIO mapReferent) {
        return RXPRMLikeMapInfoBackend.sNameFromInt((int) mapReferent.fixnumVal);
    }
}

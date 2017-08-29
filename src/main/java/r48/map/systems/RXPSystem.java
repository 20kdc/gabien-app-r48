/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.systems;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.IMapContext;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.StuffRenderer;
import r48.map.UIMapViewContainer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.ChainedImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.imaging.IImageLoader;
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
        super(new CacheImageLoader(new ChainedImageLoader(new IImageLoader[] {
                new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png"),
                new GabienImageLoader(AppMain.rootPath + "Graphics/", ".jpg"),
        })));
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
    public UIElement createMapExplorer(ISupplier<IConsumer<UIElement>> windowMaker, IMapContext mapBox) {
        return new UIGRMMapInfos(windowMaker, new RXPRMLikeMapInfoBackend(), mapBox);
    }

    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
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
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, StuffRenderer.prepareTraditional(tileRenderer, new int[] {0, 1, 2}, eventRenderer, imageLoader, map, pano, false, false, 0, 0, 640, 480, 2));
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
        IImageLoader imageLoader = new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png");
        ITileRenderer tileRenderer = new XPTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    public RMMapData[] getAllMaps() {
        LinkedList<RMMapData> rmdList = new LinkedList<RMMapData>();
        for (Map.Entry<RubyIO, RubyIO> rio : AppMain.objectDB.getObject("MapInfos").hashVal.entrySet()) {
            int id = (int) rio.getKey().fixnumVal;
            RMMapData rmd = new RMMapData(rio.getValue().getInstVarBySymbol("@name").decString(), AppMain.objectDB.getObject(RXPRMLikeMapInfoBackend.sNameFromInt(id)), id, RXPRMLikeMapInfoBackend.sNameFromInt(id));
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
    public String mapReferentToId(RubyIO mapReferent) {
        return RXPRMLikeMapInfoBackend.sNameFromInt((int) mapReferent.fixnumVal);
    }
}

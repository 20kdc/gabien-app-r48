/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.systems;

import gabien.IGrInDriver;
import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.RubyTable;
import r48.map.StuffRenderer;
import r48.map.UIMapViewContainer;
import r48.map.drawlayers.*;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.R2kEventGraphicRenderer;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.IImageLoader;
import r48.map.imaging.XYZOrPNGImageLoader;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.pass.R2kPassabilitySource;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.LcfTileRenderer;
import r48.toolsets.RMTranscriptDumper;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * ...
 * Created on 03/06/17.
 */
public class R2kSystem extends MapSystem implements IRMMapSystem {
    @Override
    public UIElement createMapExplorer(ISupplier<IConsumer<UIElement>> windowMaker, UIMapViewContainer mapBox) {
        return new UIGRMMapInfos(windowMaker, mapBox, new R2kRMLikeMapInfoBackend());
    }

    private RubyIO tsoFromMap2000(RubyIO map) {
        if (map == null)
            return null;
        return AppMain.objectDB.getObject("RPG_RT.ldb").getInstVarBySymbol("@tilesets").getHashVal(map.getInstVarBySymbol("@tileset_id"));
    }

    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
        IImageLoader imageLoader = new CacheImageLoader(new XYZOrPNGImageLoader(AppMain.rootPath));
        RubyIO tileset = tsoFromMap2000(map);
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tileset);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        // Cannot get enough information without map & tileset
        if ((map != null) && (tileset != null)) {
            RubyIO events = map.getInstVarBySymbol("@events");
            RubyTable tbl = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            String vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            if (map.getInstVarBySymbol("@parallax_flag").type != 'T')
                vxaPano = "";
            layers = new IMapViewDrawLayer[9];
            IGrInDriver.IImage img = null;
            if (!vxaPano.equals(""))
                img = imageLoader.getImage("Panorama/" + vxaPano, true);
            // Layer order seems to be this:
            // layer 1 lower
            // layer 2 lower
            // <events>
            // layer 1 upper
            // layer 2 upper
            layers[0] = new PanoramaMapViewDrawLayer(img);
            layers[1] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, false, tileset); // TSBelow
            layers[2] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, false, tileset); // ...
            layers[3] = new EventMapViewDrawLayer(0, events, eventRenderer, 16);
            layers[4] = new EventMapViewDrawLayer(1, events, eventRenderer, 16); // Player/Same
            layers[5] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, true, tileset);
            layers[6] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, true, tileset);
            layers[7] = new EventMapViewDrawLayer(2, events, eventRenderer, 16);
            layers[8] = new PassabilityMapViewDrawLayer(new R2kPassabilitySource(tbl, tileset), 16);
        }
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
        IImageLoader imageLoader = new CacheImageLoader(new XYZOrPNGImageLoader(AppMain.rootPath));
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    public RMMapData[] getAllMaps() {
        LinkedList<RMMapData> rmdList = new LinkedList<RMMapData>();
        for (Map.Entry<RubyIO, RubyIO> rio : AppMain.objectDB.getObject("RPG_RT.lmt").getInstVarBySymbol("@map_infos").hashVal.entrySet()) {
            int id = (int) rio.getKey().fixnumVal;
            if (id == 0)
                continue;
            RubyIO obj = AppMain.objectDB.getObject(R2kRMLikeMapInfoBackend.sNameFromInt(id));
            if (obj == null)
                continue;
            RMMapData rmd = new RMMapData(rio.getValue().getInstVarBySymbol("@name").decString(), obj, id, R2kRMLikeMapInfoBackend.sNameFromInt(id));
            rmdList.add(rmd);
        }
        return rmdList.toArray(new RMMapData[0]);
    }

    @Override
    public RubyIO[] getAllCommonEvents() {
        RubyIO cev = AppMain.objectDB.getObject("RPG_RT.ldb").getInstVarBySymbol("@common_events");
        LinkedList<Integer> ints = new LinkedList<Integer>();
        for (RubyIO i : cev.hashVal.keySet())
            ints.add((int) i.fixnumVal);
        Collections.sort(ints);
        LinkedList<RubyIO> l = new LinkedList<RubyIO>();
        for (Integer i : ints)
            l.add(cev.getHashVal(new RubyIO().setFX(i)));
        return l.toArray(new RubyIO[0]);
    }

    @Override
    public void dumpCustomData(RMTranscriptDumper dumper) {

    }
}

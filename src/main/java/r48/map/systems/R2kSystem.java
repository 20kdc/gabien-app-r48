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
import r48.map.R2kAreaEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.drawlayers.*;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.R2kEventGraphicRenderer;
import r48.map.imaging.*;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.mapinfos.UISaveScanMapInfos;
import r48.map.pass.R2kPassabilitySource;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.LcfTileRenderer;
import r48.toolsets.RMTranscriptDumper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * ...
 * Created on 03/06/17.
 */
public class R2kSystem extends MapSystem implements IRMMapSystem {
    public R2kSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("", "", new ChainedImageLoader(new IImageLoader[] {
                new XYZImageLoader(),
                new PNG8IImageLoader(),
                new GabienImageLoader(".png")
        }))), true);
    }

    @Override
    public UIElement createMapExplorer(ISupplier<IConsumer<UIElement>> windowMaker, IMapContext context) {
        return new UIGRMMapInfos(windowMaker, new R2kRMLikeMapInfoBackend(), context);
    }

    @Override
    public UIElement createSaveExplorer(ISupplier<IConsumer<UIElement>> windowMaker, IMapContext mapBox) {
        return new UISaveScanMapInfos(new IFunction<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                return getSaveName(integer);
            }
        }, new IFunction<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                return "Save." + integer;
            }
        }, 1, 99, mapBox);
    }

    private String getSaveName(Integer integer) {
        String padme = integer.toString();
        if (padme.length() == 1)
            padme = "0" + padme;
        return "Save" + padme + ".lsd";
    }


    private RubyIO tsoFromMap2000(RubyIO map) {
        if (map == null)
            return null;
        return AppMain.objectDB.getObject("RPG_RT.ldb").getInstVarBySymbol("@tilesets").getHashVal(map.getInstVarBySymbol("@tileset_id"));
    }

    // saveData is optional, and replaces some things.
    private StuffRenderer rendererFromMap(int mapId, RubyIO map, RubyIO saveData) {
        RubyIO tileset = tsoFromMap2000(map);
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tileset);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        // Cannot get enough information without map & tileset
        if ((map != null) && (tileset != null)) {
            RubyIO events = map.getInstVarBySymbol("@events");

            if (saveData != null)
                events = saveFileVirtualEvents(mapId, saveData);

            long scrollFlags = map.getInstVarBySymbol("@scroll_type").fixnumVal;
            RubyTable tbl = new RubyTable(map.getInstVarBySymbol("@data").userVal);
            String vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            boolean loopX = false;
            boolean loopY = false;
            int autoLoopX = 0;
            int autoLoopY = 0;
            if (map.getInstVarBySymbol("@parallax_flag").type != 'T') {
                vxaPano = "";
            } else {
                loopX = map.getInstVarBySymbol("@parallax_loop_x").type == 'T';
                loopY = map.getInstVarBySymbol("@parallax_loop_y").type == 'T';
                boolean aloopX = map.getInstVarBySymbol("@parallax_loop_x").type == 'T';
                boolean aloopY = map.getInstVarBySymbol("@parallax_loop_y").type == 'T';
                if (aloopX)
                    autoLoopX = (int) map.getInstVarBySymbol("@parallax_sx").fixnumVal;
                if (aloopY)
                    autoLoopY = (int) map.getInstVarBySymbol("@parallax_sy").fixnumVal;
            }
            layers = new IMapViewDrawLayer[10];
            IImage img = null;
            if (!vxaPano.equals(""))
                img = imageLoader.getImage("Panorama/" + vxaPano, true);
            // Layer order seems to be this:
            // layer 1 lower
            // layer 2 lower
            // <events>
            // layer 1 upper
            // layer 2 upper
            layers[0] = new PanoramaMapViewDrawLayer(img, loopX, loopY, autoLoopX, autoLoopY, tbl.width, tbl.height, 320, 240, 1);
            layers[1] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, false, tileset); // TSBelow
            layers[2] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, false, tileset); // ...
            layers[3] = new EventMapViewDrawLayer(0, events, eventRenderer, 16);
            layers[4] = new EventMapViewDrawLayer(1, events, eventRenderer, 16); // Player/Same
            layers[5] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, true, tileset);
            layers[6] = new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, true, tileset);
            layers[7] = new EventMapViewDrawLayer(2, events, eventRenderer, 16);
            layers[8] = new PassabilityMapViewDrawLayer(new R2kPassabilitySource(tbl, tileset, (scrollFlags & 2) != 0, (scrollFlags & 1) != 0), 16);
            layers[9] = new EventMapViewDrawLayer(0x7FFFFFFF, events, eventRenderer, 16);
        }
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, layers, "RPG::Event");
    }

    // Rearranges objects in a convenient manner.
    // Note that this trick can't be used for editing, so this might be removed later
    private RubyIO saveFileVirtualEvents(int mapId, RubyIO saveData) {
        RubyIO eventsHash = new RubyIO();
        eventsHash.setHash();
        // Inject 'events'
        sfveInjectEvent(eventsHash, 10001, mapId, saveData.getInstVarBySymbol("@party_pos"));
        sfveInjectEvent(eventsHash, 10002, mapId, saveData.getInstVarBySymbol("@boat_pos"));
        sfveInjectEvent(eventsHash, 10003, mapId, saveData.getInstVarBySymbol("@ship_pos"));
        sfveInjectEvent(eventsHash, 10004, mapId, saveData.getInstVarBySymbol("@airship_pos"));
        // Inject actual events
        for (Map.Entry<RubyIO, RubyIO> kv : saveData.getInstVarBySymbol("@map_info").getInstVarBySymbol("@events").hashVal.entrySet())
            if (eventsHash.getHashVal(kv.getKey()) == null)
                eventsHash.hashVal.put(kv.getKey(), kv.getValue());
        return eventsHash;
    }

    private void sfveInjectEvent(RubyIO eventsHash, int i, int mapId, RubyIO instVarBySymbol) {
        if (instVarBySymbol.getInstVarBySymbol("@map").fixnumVal != mapId)
            return;
        eventsHash.hashVal.put(new RubyIO().setFX(i), instVarBySymbol);
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0], "RPG::Event");
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
            RMMapData rmd = new RMMapData(rio.getValue().getInstVarBySymbol("@name").decString(), obj, id, R2kRMLikeMapInfoBackend.sNameFromInt(id), "RPG::Map");
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
        dumper.startFile("RPG_RT.ldb", TXDB.get("System data (of any importance, anyway)."));
        RubyIO sys = AppMain.objectDB.getObject("RPG_RT.ldb");
        dumper.dumpSVListHash("@switches", sys.getInstVarBySymbol("@switches"));
        dumper.dumpSVListHash("@variables", sys.getInstVarBySymbol("@variables"));
        dumper.endFile();
    }

    @Override
    public String mapReferentToGUM(RubyIO mapReferent) {
        return R2kRMLikeMapInfoBackend.sTranslateToGUM((int) mapReferent.fixnumVal);
    }

    @Override
    public IFunction<IMapToolContext, IEditingToolbarController> mapLoadRequest(String gum, final ISupplier<IConsumer<UIElement>> windowMaker) {
        if (gum.startsWith("Save."))
            return super.mapLoadRequest(gum, windowMaker);
        if (gum.startsWith("Area.")) {
            final RubyIO root = AppMain.objectDB.getObject("RPG_RT.lmt");
            final RubyIO mapInfos = root.getInstVarBySymbol("@map_infos");
            final RubyIO mapInfo = mapInfos.getHashVal(new RubyIO().setFX(Long.parseLong(gum.substring(5))));
            return new IFunction<IMapToolContext, IEditingToolbarController>() {
                @Override
                public IEditingToolbarController apply(IMapToolContext uiMapView) {
                    return new R2kAreaEditingToolbarController(uiMapView, root, mapInfo);
                }
            };
        }
        return super.mapLoadRequest(gum, windowMaker);
    }

    @Override
    public MapViewDetails mapViewRequest(String gum, boolean allowCreate) {
        String[] gp = gum.split("\\.");
        final int v = Integer.parseInt(gp[1]);
        if (gp[0].equals("Save")) {
            final String obj = getSaveName(v);
            if (!allowCreate)
                if (AppMain.objectDB.getObject(obj, null) == null)
                    return null;
            return new MapViewDetails(obj, "RPG::Save", new ISupplier<MapViewState>() {
                @Override
                public MapViewState get() {
                    final RubyIO root = AppMain.objectDB.getObject(obj);
                    int mapId = (int) root.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map").fixnumVal;
                    final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(mapId);
                    RubyIO map = AppMain.objectDB.getObject(objn);
                    return MapViewState.fromRT(rendererFromMap(mapId, map, root), map, "@data", true);
                }
            }, true, false);
        }
        final RubyIO root = AppMain.objectDB.getObject("RPG_RT.lmt");
        final RubyIO mapInfos = root.getInstVarBySymbol("@map_infos");
        final RubyIO mapInfo = mapInfos.getHashVal(new RubyIO().setFX(v));
        try {
            if (mapInfo.getInstVarBySymbol("@type").fixnumVal == 2)
                return mapViewRequest("Map." + mapInfo.getInstVarBySymbol("@parent_id").fixnumVal, true);
        } catch (StackOverflowError soe) {
            // Note the implied change to an Exception, which gets caught
            throw new RuntimeException(soe);
        }
        final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(v);
        if (!allowCreate)
            if (AppMain.objectDB.getObject(objn, null) == null)
                return null;
        return new MapViewDetails(objn, "RPG::Map", new ISupplier<MapViewState>() {
            @Override
            public MapViewState get() {
                RubyIO map = AppMain.objectDB.getObject(objn);
                return MapViewState.fromRT(rendererFromMap(v, map, null), map, "@data", false);
            }
        }, false, true);
    }
}

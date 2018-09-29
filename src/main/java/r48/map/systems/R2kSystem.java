/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.*;
import r48.AppMain;
import r48.IMapContext;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.imageio.BMP8IImageIOFormat;
import r48.imageio.PNG8IImageIOFormat;
import r48.imageio.XYZImageIOFormat;
import r48.map.*;
import r48.map.drawlayers.*;
import r48.map.events.*;
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
public class R2kSystem extends MapSystem implements IRMMapSystem, IDynobjMapSystem {
    public R2kSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("", "", new ChainedImageLoader(new IImageLoader[] {
                new ImageIOImageLoader(new XYZImageIOFormat(), ".xyz", true),
                // This is actually valid, but almost nobody wanted to use BMP over one of PNG or XYZ. Who'd have guessed?
                new ImageIOImageLoader(new BMP8IImageIOFormat(8), ".bmp", true),
                new ImageIOImageLoader(new PNG8IImageIOFormat(), ".png", true),
                // EasyRPG extension: arbitrary PNGs
                new GabienImageLoader(".png")
        }))), true);
    }

    @Override
    public UIElement createMapExplorer(IConsumer<UIElement> windowMaker, IMapContext context, String mapInfos) {
        return new UIGRMMapInfos(windowMaker, new R2kRMLikeMapInfoBackend(), context, mapInfos);
    }

    @Override
    public UIElement createSaveExplorer(IConsumer<UIElement> windowMaker, IMapContext mapBox, String saves) {
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
        }, 1, 99, mapBox, saves);
    }

    @Override
    public Rect getIdealGridForImage(String path, Size img) {
        String id = GaBIEn.basename(GaBIEn.dirname(path));
        if (id.equalsIgnoreCase("ChipSet"))
            return new Rect(0, 0, 16, 16);
        if (id.equalsIgnoreCase("CharSet"))
            return new Rect(0, 0, 24, 32);
        if (id.equalsIgnoreCase("Battle"))
            return new Rect(0, 0, 96, 96);
        if (id.equalsIgnoreCase("Battle2"))
            return new Rect(0, 0, 128, 128);
        if (id.equalsIgnoreCase("BattleCharSet"))
            return new Rect(0, 0, 48, 48);
        if (id.equalsIgnoreCase("BattleWeapon"))
            return new Rect(0, 0, 64, 64);
        return null;
    }

    private String getSaveName(Integer integer) {
        String padme = integer.toString();
        if (padme.length() == 1)
            padme = "0" + padme;
        return "Save" + padme + ".lsd";
    }


    private RubyIO tsoById(long id) {
        return AppMain.objectDB.getObject("RPG_RT.ldb").getInstVarBySymbol("@tilesets").getHashVal(new RubyIO().setFX(id));
    }

    // saveData is optional, and replaces some things.
    private StuffRenderer rendererFromMapAndTso(RubyIO map, RubyIO tileset, IEventAccess events) {
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tileset);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        // Cannot get enough information without map & tileset
        if ((map != null) && (tileset != null)) {
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
            IImage img = null;
            if (!vxaPano.equals(""))
                img = imageLoader.getImage("Panorama/" + vxaPano, true);
            // Layer order seems to be this:
            // layer 1 lower
            // layer 2 lower
            // <events>
            // layer 1 upper
            // layer 2 upper
            layers = new IMapViewDrawLayer[] {
                new PanoramaMapViewDrawLayer(img, loopX, loopY, autoLoopX, autoLoopY, tbl.width, tbl.height, 320, 240, 1),
                new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, false, tileset, TXDB.get("L0 (no Upper flag)")),
                new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, false, tileset, TXDB.get("L1 (no Upper flag)")),
                new EventMapViewDrawLayer(0, events, eventRenderer, 16, TXDB.get(" (Below Player)")),
                new EventMapViewDrawLayer(1, events, eventRenderer, 16, TXDB.get(" (Player/Same)")), // Player/Same
                new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, true, tileset, TXDB.get("L0 (Upper flag)")),
                new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, true, tileset, TXDB.get("L1 (Upper flag)")),
                new EventMapViewDrawLayer(2, events, eventRenderer, 16, TXDB.get(" (Above Player)")),
                new PassabilityMapViewDrawLayer(new R2kPassabilitySource(tbl, tileset, (scrollFlags & 2) != 0, (scrollFlags & 1) != 0), 16),
                new EventMapViewDrawLayer(0x7FFFFFFF, events, eventRenderer, 16, ""),
                new GridMapViewDrawLayer(),
                new BorderMapViewDrawLayer(tbl.width, tbl.height)
            };
        }
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO tso) {
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
    public MapViewDetails mapViewRequest(String gum, boolean allowCreate) {
        String[] gp = gum.split("\\.");
        final int v = Integer.parseInt(gp[1]);
        if (gp[0].equals("Save")) {
            final String obj = getSaveName(v);
            if (!allowCreate)
                if (AppMain.objectDB.getObject(obj, null) == null)
                    return null;
            final RubyIO root = AppMain.objectDB.getObject(obj, "RPG::Save");
            return new MapViewDetails(obj, "RPG::Save", new IFunction<String, MapViewState>() {
                private RTilesetCacheHelper tilesetCache = new RTilesetCacheHelper("RPG_RT.ldb");
                @Override
                public MapViewState apply(String changed) {
                    int mapId = (int) root.getInstVarBySymbol("@party_pos").getInstVarBySymbol("@map").fixnumVal;
                    tilesetCache.updateMapId(mapId);

                    final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(mapId);
                    RubyIO map = AppMain.objectDB.getObject(objn);
                    final IEventAccess events = new R2kSavefileEventAccess(obj, root, "RPG::Save");
                    if (map == null)
                        return MapViewState.getBlank(null, new String[] {
                                objn
                        }, events);

                    // Map okay - update tileset cache & render
                    long currentTsId = map.getInstVarBySymbol("@tileset_id").fixnumVal;
                    RubyIO lastTileset = tilesetCache.receivedChanged(changed, currentTsId);
                    if (lastTileset == null) {
                        lastTileset = tsoById(currentTsId);
                        tilesetCache.insertTileset(currentTsId, lastTileset);
                    }

                    return MapViewState.fromRT(rendererFromMapAndTso(map, lastTileset, events), objn, new String[] {
                            objn,
                            "RPG_RT.ldb"
                    }, map, "@data", true, events);
                }
            }, new IFunction<IMapToolContext, IEditingToolbarController>() {
                @Override
                public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                    return new MapEditingToolbarController(iMapToolContext, true);
                }
            });
        }
        // Map, Area
        final RubyIO root = AppMain.objectDB.getObject("RPG_RT.lmt");
        final RubyIO mapInfos = root.getInstVarBySymbol("@map_infos");
        final RubyIO mapInfo = mapInfos.getHashVal(new RubyIO().setFX(v));
        try {
            if (mapInfo.getInstVarBySymbol("@type").fixnumVal == 2) {
                long parent = mapInfo.getInstVarBySymbol("@parent_id").fixnumVal;
                if (parent == v)
                    return null;
                MapViewDetails mvd = mapViewRequest("Map." + parent, false);
                if (mvd == null)
                    return null;
                return new MapViewDetails(mvd.objectId, mvd.objectSchema, mvd.rendererRetriever, new IFunction<IMapToolContext, IEditingToolbarController>() {
                    @Override
                    public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                        return new R2kAreaEditingToolbarController(iMapToolContext, root, mapInfo);
                    }
                });
            }
        } catch (StackOverflowError soe) {
            // Note the implied change to an Exception, which gets caught
            // This is still not good, though
            throw new RuntimeException(soe);
        }
        final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(v);
        if (!allowCreate)
            if (AppMain.objectDB.getObject(objn, null) == null)
                return null;
        final RubyIO map = AppMain.objectDB.getObject(objn, "RPG::Map");
        final IEventAccess iea = new TraditionalEventAccess(objn, "RPG::Map", "@events", 1, "RPG::Event");
        return new MapViewDetails(objn, "RPG::Map", new IFunction<String, MapViewState>() {
            private RTilesetCacheHelper tilesetCache = new RTilesetCacheHelper("RPG_RT.ldb");
            @Override
            public MapViewState apply(String changed) {
                long currentTsId = map.getInstVarBySymbol("@tileset_id").fixnumVal;
                RubyIO lastTileset = tilesetCache.receivedChanged(changed, currentTsId);
                if (lastTileset == null) {
                    lastTileset = tsoById(currentTsId);
                    tilesetCache.insertTileset(currentTsId, lastTileset);
                }
                return MapViewState.fromRT(rendererFromMapAndTso(map, lastTileset, iea), objn, new String[] {
                        "RPG_RT.ldb"
                }, map, "@data", false, iea);
            }
        }, new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                return new MapEditingToolbarController(iMapToolContext, false);
            }
        });
    }

    @Override
    public LinkedList<String> getDynamicObjects() {
        return MapSystem.dynamicObjectsFromRM(this);
    }
}

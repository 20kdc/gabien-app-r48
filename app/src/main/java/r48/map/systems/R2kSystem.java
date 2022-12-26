/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.uslx.append.*;
import gabien.ui.Rect;
import gabien.ui.Size;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.IMapContext;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.ObjectInfo;
import r48.dbs.TXDB;
import r48.imageio.BMP8IImageIOFormat;
import r48.imageio.PNG8IImageIOFormat;
import r48.imageio.XYZImageIOFormat;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
import r48.io.data.IRIO;
import r48.map.*;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.drawlayers.*;
import r48.map.events.*;
import r48.map.imaging.*;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.mapinfos.UISaveScanMapInfos;
import r48.map.pass.R2kPassabilitySource;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.LcfTileRenderer;
import r48.maptools.UIMTBase;
import r48.maptools.deep.UIMTFtrGdt01;
import r48.toolsets.utils.RMTranscriptDumper;

import java.util.Collections;
import java.util.LinkedList;

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
    public UIElement createMapExplorer(IMapContext context, String mapInfos) {
        return new UIGRMMapInfos(new R2kRMLikeMapInfoBackend(), context, mapInfos);
    }

    @Override
    public UIElement createSaveExplorer(IMapContext mapBox, String saves) {
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
        String pop = GaBIEn.parentOf(path);
        if (pop == null)
            return null;
        String id = GaBIEn.nameOf(pop);
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


    private IRIO tsoById(long id) {
        return AppMain.objectDB.getObject("RPG_RT.ldb").getObject().getIVar("@tilesets").getHashVal(new RubyIO().setFX(id));
    }

    // saveData is optional, and replaces some things.
    private StuffRenderer rendererFromMapAndTso(IRIO map, IRIO tileset, IEventAccess events) {
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tileset);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        // Cannot get enough information without map & tileset
        if ((map != null) && (tileset != null)) {
            long scrollFlags = map.getIVar("@scroll_type").getFX();
            RubyTable tbl = new RubyTable(map.getIVar("@data").getBuffer());
            String vxaPano = map.getIVar("@parallax_name").decString();
            boolean loopX = false;
            boolean loopY = false;
            int autoLoopX = 0;
            int autoLoopY = 0;
            if (map.getIVar("@parallax_flag").getType() != 'T') {
                vxaPano = "";
            } else {
                loopX = map.getIVar("@parallax_loop_x").getType() == 'T';
                loopY = map.getIVar("@parallax_loop_y").getType() == 'T';
                boolean aloopX = map.getIVar("@parallax_loop_x").getType() == 'T';
                boolean aloopY = map.getIVar("@parallax_loop_y").getType() == 'T';
                if (aloopX)
                    autoLoopX = (int) map.getIVar("@parallax_sx").getFX();
                if (aloopY)
                    autoLoopY = (int) map.getIVar("@parallax_sy").getFX();
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
                    new EventMapViewDrawLayer(0, events, eventRenderer, TXDB.get(" (Below Player)")),
                    new EventMapViewDrawLayer(1, events, eventRenderer, TXDB.get(" (Player/Same)")), // Player/Same
                new R2kTileMapViewDrawLayer(tbl, tileRenderer, 0, true, tileset, TXDB.get("L0 (Upper flag)")),
                new R2kTileMapViewDrawLayer(tbl, tileRenderer, 1, true, tileset, TXDB.get("L1 (Upper flag)")),
                    new EventMapViewDrawLayer(2, events, eventRenderer, TXDB.get(" (Above Player)")),
                new PassabilityMapViewDrawLayer(new R2kPassabilitySource(tbl, tileset, (scrollFlags & 2) != 0, (scrollFlags & 1) != 0), 16),
                    new EventMapViewDrawLayer(0x7FFFFFFF, events, eventRenderer, ""),
                new GridMapViewDrawLayer(),
                new BorderMapViewDrawLayer(tbl.width, tbl.height)
            };
        }
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO tso) {
        ITileRenderer tileRenderer = new LcfTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(imageLoader, tileRenderer);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    public RMMapData[] getAllMaps() {
        LinkedList<RMMapData> rmdList = new LinkedList<RMMapData>();
        IRIO lmti = AppMain.objectDB.getObject("RPG_RT.lmt").getObject().getIVar("@map_infos");
        for (final IRIO key : lmti.getHashKeys()) {
            int id = (int) key.getFX();
            if (id == 0)
                continue;
            RMMapData rmd = new RMMapData(new ISupplier<String>() {
                @Override
                public String get() {
                    IRIO lmtiLocal = AppMain.objectDB.getObject("RPG_RT.lmt").getObject().getIVar("@map_infos");
                    IRIO mapInfo = lmtiLocal.getHashVal(key);
                    if (mapInfo == null)
                        return TXDB.get("<map removed from RPG_RT.lmt>");
                    return mapInfo.getIVar("@name").decString();
                }
            }, id, R2kRMLikeMapInfoBackend.sNameFromInt(id), "RPG::Map");
            rmdList.add(rmd);
        }
        return rmdList.toArray(new RMMapData[0]);
    }

    @Override
    public ILoadedObject getCommonEventRoot() {
        return AppMain.objectDB.getObject("RPG_RT.ldb");
    }

    @Override
    public IRIO[] getAllCommonEvents() {
        IRIO cev = getCommonEventRoot().getObject().getIVar("@common_events");
        LinkedList<Integer> ints = new LinkedList<Integer>();
        for (IRIO i : cev.getHashKeys())
            ints.add((int) i.getFX());
        Collections.sort(ints);
        LinkedList<IRIO> l = new LinkedList<IRIO>();
        for (Integer i : ints)
            l.add(cev.getHashVal(new RubyIO().setFX(i)));
        return l.toArray(new IRIO[0]);
    }

    @Override
    public void dumpCustomData(RMTranscriptDumper dumper) {
        dumper.startFile("RPG_RT.ldb", TXDB.get("System data (of any importance, anyway)."));
        IRIO sys = AppMain.objectDB.getObject("RPG_RT.ldb").getObject();
        dumper.dumpSVListHash("@switches", sys.getIVar("@switches"));
        dumper.dumpSVListHash("@variables", sys.getIVar("@variables"));
        dumper.endFile();
    }

    @Override
    public String mapReferentToGUM(IRIO mapReferent) {
        return R2kRMLikeMapInfoBackend.sTranslateToGUM((int) mapReferent.getFX());
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
            final IObjectBackend.ILoadedObject root = AppMain.objectDB.getObject(obj, "RPG::Save");
            return new MapViewDetails(obj, "RPG::Save", new IFunction<String, MapViewState>() {
                private RTilesetCacheHelper tilesetCache = new RTilesetCacheHelper("RPG_RT.ldb");
                @Override
                public MapViewState apply(String changed) {
                    int mapId = (int) root.getObject().getIVar("@party_pos").getIVar("@map").getFX();
                    tilesetCache.updateMapId(mapId);

                    final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(mapId);
                    IObjectBackend.ILoadedObject map = AppMain.objectDB.getObject(objn);
                    final IEventAccess events = new R2kSavefileEventAccess(obj, root, "RPG::Save");
                    if (map == null)
                        return MapViewState.getBlank(null, new String[] {
                                objn
                        }, events);

                    // Map okay - update tileset cache & render
                    long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                    IRIO lastTileset = tilesetCache.receivedChanged(changed, currentTsId);
                    if (lastTileset == null) {
                        lastTileset = tsoById(currentTsId);
                        tilesetCache.insertTileset(currentTsId, lastTileset);
                    }

                    return MapViewState.fromRT(rendererFromMapAndTso(map.getObject(), lastTileset, events), objn, new String[] {
                            objn,
                            "RPG_RT.ldb"
                    }, map.getObject(), "@data", true, events);
                }
            }, new IFunction<IMapToolContext, IEditingToolbarController>() {
                @Override
                public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                    return new MapEditingToolbarController(iMapToolContext, true);
                }
            });
        }
        // Map, Area
        final IObjectBackend.ILoadedObject root = AppMain.objectDB.getObject("RPG_RT.lmt");
        final IRIO mapInfos = root.getObject().getIVar("@map_infos");
        final IRIO mapInfo = mapInfos.getHashVal(new RubyIO().setFX(v));
        try {
            if (mapInfo.getIVar("@type").getFX() == 2) {
                long parent = mapInfo.getIVar("@parent_id").getFX();
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
        final IObjectBackend.ILoadedObject map = AppMain.objectDB.getObject(objn, "RPG::Map");
        final IEventAccess iea = new TraditionalEventAccess(objn, "RPG::Map", "@events", 1, "RPG::Event");
        return new MapViewDetails(objn, "RPG::Map", new IFunction<String, MapViewState>() {
            private RTilesetCacheHelper tilesetCache = new RTilesetCacheHelper("RPG_RT.ldb");
            @Override
            public MapViewState apply(String changed) {
                long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                IRIO lastTileset = tilesetCache.receivedChanged(changed, currentTsId);
                if (lastTileset == null) {
                    lastTileset = tsoById(currentTsId);
                    tilesetCache.insertTileset(currentTsId, lastTileset);
                }
                return MapViewState.fromRT(rendererFromMapAndTso(map.getObject(), lastTileset, iea), objn, new String[] {
                        "RPG_RT.ldb"
                }, map.getObject(), "@data", false, iea);
            }
        }, new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                return new MapEditingToolbarController(iMapToolContext, false, new ToolButton[] {
                        new ToolButton(TXDB.get("DeepWater")) {
                            @Override
                            public UIMTBase apply(IMapToolContext a) {
                                return new UIMTFtrGdt01(a);
                            }
                        }
                }, new ToolButton[] {
                        new FindTranslatablesToolButton("RPG::EventPage")
                });
            }
        });
    }

    @Override
    public ObjectInfo[] getDynamicObjects() {
        return MapSystem.dynamicObjectsFromRM(this);
    }

    @Override
    public boolean engineUsesPal0Colourkeys() {
        return true;
    }
}

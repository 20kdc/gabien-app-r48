/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.ui.UIElement;
import gabien.ui.dialogs.UIPopupMenu.Entry;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import r48.App;
import r48.IMapContext;
import r48.ITileAccess;
import r48.RubyTableR;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.game.r2k.R2kPassabilitySource;
import r48.game.r2k.R2kTileMapViewDrawLayer;
import r48.imageio.BMP8IImageIOFormat;
import r48.imageio.PNG8IImageIOFormat;
import r48.imageio.XYZImageIOFormat;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.map.*;
import r48.map.MapEditingToolbarController.ToolButton;
import r48.map.drawlayers.*;
import r48.map.events.*;
import r48.map.imaging.*;
import r48.map.mapinfos.R2kRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.mapinfos.UISaveScanMapInfos;
import r48.map.tiles.LcfTileRenderer;
import r48.map.tiles.LoopTileAccess;
import r48.map2d.layers.GridMapViewDrawLayer;
import r48.map2d.layers.MapViewDrawLayer;
import r48.map2d.layers.PassabilityMapViewDrawLayer;
import r48.maptools.UIMTBase;
import r48.maptools.deep.UIMTFtrGdt01;
import r48.texture.CacheTexLoader;
import r48.texture.ChainedTexLoader;
import r48.texture.ITexLoader;
import r48.toolsets.R2kTools;
import r48.toolsets.RMTools;
import r48.toolsets.utils.RMTranscriptDumper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * ...
 * Created on 03/06/17.
 */
public class R2kSystem extends MapSystem implements IRMMapSystem {
    public R2kSystem(App app) {
        super(app, new CacheTexLoader(new FixAndSecondaryImageLoader(app, "", "", new ChainedTexLoader(new ITexLoader[] {
                new ImageIOImageLoader(app, new XYZImageIOFormat(app.t), ".xyz", true),
                // This is actually valid, but almost nobody wanted to use BMP over one of PNG or XYZ. Who'd have guessed?
                new ImageIOImageLoader(app, new BMP8IImageIOFormat(app.t, 8), ".bmp", true),
                new ImageIOImageLoader(app, new PNG8IImageIOFormat(app.t), ".png", true),
                // EasyRPG extension: arbitrary PNGs
                new GabienImageLoader(app, ".png")
        }))), true);
    }

    @Override
    public UIElement createMapExplorer(IMapContext context, String mapInfos) {
        return new UIGRMMapInfos(new R2kRMLikeMapInfoBackend(app), context, mapInfos);
    }

    @Override
    public UIElement createSaveExplorer(IMapContext mapBox, String saves) {
        return new UISaveScanMapInfos(this::getSaveName, (integer) -> "Save." + integer, 1, 99, mapBox, saves);
    }

    @Override
    public Consumer<LinkedList<Entry>> createEngineTools() {
        return new RMTools(app).andThen(new R2kTools(app));
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

    @Override
    protected String mapObjectIDToSchemaID(String objectID) {
        if (objectID.endsWith(".lmt"))
            return "RPG::MapTree";
        if (objectID.endsWith(".lmu"))
            return "RPG::Map";
        if (objectID.endsWith(".ldb"))
            return "RPG::Database";
        if (objectID.endsWith(".lsd"))
            return "RPG::Save";
        return super.mapObjectIDToSchemaID(objectID);
    }

    private @Nullable IRIO tsoById(long id) {
        return app.odb.getObject("RPG_RT.ldb").getObject().getIVar("@tilesets").getHashVal(DMKey.of(id));
    }

    // saveData is optional, and replaces some things.
    private StuffRenderer rendererFromMapAndTso(IRIO map, IRIO tileset, IEventAccess events, LcfTileRenderer tileRenderer) {
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(app, imageLoader, tileRenderer);
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer);
    }

    public MapViewDrawLayer[] createLayersForMap(StuffRenderer renderer, @NonNull IRIO map, IRIO tileset, IEventAccess events) {
        if (tileset == null)
            return new MapViewDrawLayer[0];
        RubyTableR tbl = new RubyTableR(map.getIVar("@data").getBuffer());
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

        long scrollFlags = map.getIVar("@scroll_type").getFX();
        boolean tileLoopX = (scrollFlags & 2) != 0;
        boolean tileLoopY = (scrollFlags & 1) != 0;
        ITileAccess looper = LoopTileAccess.of(tbl, tileLoopX, tileLoopY);

        // Layer order seems to be this:
        // layer 1 lower
        // layer 2 lower
        // <events>
        // layer 1 upper
        // layer 2 upper
        return new MapViewDrawLayer[] {
            new PanoramaMapViewDrawLayer(app.t, img, loopX, loopY, autoLoopX, autoLoopY, tbl.width, tbl.height, 320, 240, 1),
            new R2kTileMapViewDrawLayer(app.t, looper, renderer.tileRenderer, 0, false, tileset, T.m.l_rk0l),
            new R2kTileMapViewDrawLayer(app.t, looper, renderer.tileRenderer, 1, false, tileset, T.m.l_rk1l),
                new EventMapViewDrawLayer(app.a, app.t, 0, events, renderer.eventRenderer, T.m.l_rkbp),
                new EventMapViewDrawLayer(app.a, app.t, 1, events, renderer.eventRenderer, T.m.l_rkps), // Player/Same
            new R2kTileMapViewDrawLayer(app.t, looper, renderer.tileRenderer, 0, true, tileset, T.m.l_rk0u),
            new R2kTileMapViewDrawLayer(app.t, looper, renderer.tileRenderer, 1, true, tileset, T.m.l_rk1u),
                new EventMapViewDrawLayer(app.a, app.t, 2, events, renderer.eventRenderer, T.m.l_rkap),
            new PassabilityMapViewDrawLayer(app.a, app.t, new R2kPassabilitySource(looper, tileset), 16),
                new EventMapViewDrawLayer(app.a, app.t, 0x7FFFFFFF, events, renderer.eventRenderer, ""),
            new GridMapViewDrawLayer(app.t),
            new BorderMapViewDrawLayer(app, tbl.getBounds().multiplied(renderer.tileRenderer.tileSize))
        };
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO tso) {
        LcfTileRenderer tileRenderer = new LcfTileRenderer(app, imageLoader);
        tileRenderer.checkReloadTSO(tso);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(app, imageLoader, tileRenderer);
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer);
    }

    @Override
    public RMMapData[] getAllMaps() {
        LinkedList<RMMapData> rmdList = new LinkedList<RMMapData>();
        IRIO lmti = app.odb.getObject("RPG_RT.lmt").getObject().getIVar("@map_infos");
        for (final DMKey key : lmti.getHashKeys()) {
            int id = (int) key.getFX();
            if (id == 0)
                continue;
            IRIO mapInfoExt = lmti.getHashVal(key);
            if (mapInfoExt.getIVar("@type").getFX() == 2)
                continue;
            RMMapData rmd = new RMMapData(app, () -> {
                IRIO lmtiLocal = app.odb.getObject("RPG_RT.lmt").getObject().getIVar("@map_infos");
                IRIO mapInfo = lmtiLocal.getHashVal(key);
                if (mapInfo == null)
                    return T.m.mapMissing2k3;
                return mapInfo.getIVar("@name").decString();
            }, id, R2kRMLikeMapInfoBackend.sNameFromInt(id));
            rmdList.add(rmd);
        }
        return rmdList.toArray(new RMMapData[0]);
    }

    @Override
    public ObjectRootHandle getCommonEventRoot() {
        return app.odb.getObject("RPG_RT.ldb");
    }

    @Override
    public IRIO[] getAllCommonEvents() {
        IRIO cev = getCommonEventRoot().getObject().getIVar("@common_events");
        LinkedList<Integer> ints = new LinkedList<Integer>();
        for (DMKey i : cev.getHashKeys())
            ints.add((int) i.getFX());
        Collections.sort(ints);
        LinkedList<IRIO> l = new LinkedList<IRIO>();
        for (Integer i : ints)
            l.add(cev.getHashVal(DMKey.of(i)));
        return l.toArray(new IRIO[0]);
    }

    @Override
    public void dumpCustomData(RMTranscriptDumper dumper) {
        dumper.startFile("RPG_RT.ldb", T.h.systemDsc);
        IRIO sys = app.odb.getObject("RPG_RT.ldb").getObject();
        dumper.dumpSVListHash("@switches", sys.getIVar("@switches"));
        dumper.dumpSVListHash("@variables", sys.getIVar("@variables"));
        dumper.endFile();
    }

    @Override
    public String mapReferentToGUM(IRIO mapReferent) {
        return R2kRMLikeMapInfoBackend.sTranslateToGUM(app, (int) mapReferent.getFX());
    }

    @Override
    public MapViewDetails mapViewRequest(String gum, boolean allowCreate) {
        String[] gp = gum.split("\\.");
        final int v = Integer.parseInt(gp[1]);
        if (gp[0].equals("Save")) {
            final String obj = getSaveName(v);
            final ObjectRootHandle root = app.odb.getObject(obj, allowCreate);
            if (root == null)
                return null;
            final LcfTileRenderer tileRenderer = new LcfTileRenderer(app, imageLoader); 
            return new MapViewDetails(app, obj, root) {
                @Override
                public MapViewState rebuild() {
                    int mapId = (int) root.getObject().getIVar("@party_pos").getIVar("@map").getFX();

                    final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(mapId);
                    ObjectRootHandle map = app.odb.getObject(objn);
                    final IEventAccess events = new R2kSavefileEventAccess(app, obj, root, "RPG::Save");
                    if (map == null)
                        return MapViewState.getBlank(app, null, new String[] {
                                objn
                        }, events);

                    // Map okay - update tileset cache & render
                    long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                    IRIO lastTileset = tsoById(currentTsId);
                    tileRenderer.checkReloadTSO(lastTileset);

                    long scrollFlags = map.getObject().getIVar("@scroll_type").getFX();
                    boolean tileLoopX = (scrollFlags & 2) != 0;
                    boolean tileLoopY = (scrollFlags & 1) != 0;

                    StuffRenderer renderer = rendererFromMapAndTso(map.getObject(), lastTileset, events, tileRenderer);
                    MapViewDrawLayer[] layers = createLayersForMap(renderer, map.getObject(), lastTileset, events);
                    return MapViewState.fromRT(renderer, layers, null, objn, new String[] {
                            objn,
                            "RPG_RT.ldb"
                    }, map.getObject(), "@data", true, events, tileLoopX, tileLoopY);
                }
                @Override
                public IEditingToolbarController makeToolbar(IMapToolContext context) {
                    return new MapEditingToolbarController(context, true);
                }
            };
        }
        // Map, Area
        final ObjectRootHandle root = app.odb.getObject("RPG_RT.lmt");
        final IRIO mapInfos = root.getObject().getIVar("@map_infos");
        final IRIO mapInfo = mapInfos.getHashVal(DMKey.of(v));
        try {
            if (mapInfo.getIVar("@type").getFX() == 2) {
                long parent = mapInfo.getIVar("@parent_id").getFX();
                if (parent == v)
                    return null;
                MapViewDetails mvd = mapViewRequest("Map." + parent, false);
                if (mvd == null)
                    return null;
                return new MapViewDetails(app, mvd.objectId, mvd.object) {
                    @Override
                    public MapViewState rebuild() {
                        return mvd.rebuild();
                    }
                    @Override
                    public IEditingToolbarController makeToolbar(IMapToolContext context) {
                        return new R2kAreaEditingToolbarController(context, root, mapInfo);
                    }
                };
            }
        } catch (StackOverflowError soe) {
            // Note the implied change to an Exception, which gets caught
            // This is still not good, though
            throw new RuntimeException(soe);
        }
        final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(v);
        final ObjectRootHandle map = app.odb.getObject(objn, allowCreate);
        if (map == null)
            return null;
        final IEventAccess iea = new TraditionalEventAccess(app, objn, "@events", 1, "RPG::Event");
        final LcfTileRenderer tileRenderer = new LcfTileRenderer(app, imageLoader);
        return new MapViewDetails(app, objn, map) {
            @Override
            public MapViewState rebuild() {
                long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                IRIO lastTileset = tsoById(currentTsId);
                tileRenderer.checkReloadTSO(lastTileset);

                long scrollFlags = map.getObject().getIVar("@scroll_type").getFX();
                boolean tileLoopX = (scrollFlags & 2) != 0;
                boolean tileLoopY = (scrollFlags & 1) != 0;

                StuffRenderer renderer = rendererFromMapAndTso(map.getObject(), lastTileset, iea, tileRenderer);
                MapViewDrawLayer[] layers = createLayersForMap(renderer, map.getObject(), lastTileset, iea);
                return MapViewState.fromRT(renderer, layers, null, objn, new String[] {
                        "RPG_RT.ldb"
                }, map.getObject(), "@data", false, iea, tileLoopX, tileLoopY);
            }

            public IEditingToolbarController makeToolbar(IMapToolContext context) {
                return new MapEditingToolbarController(context, false, new ToolButton[] {
                    new ToolButton(T.m.tDeepWaterButton) {
                        @Override
                        public UIMTBase apply(IMapToolContext a) {
                            return new UIMTFtrGdt01(a);
                        }
                    }
                }, new ToolButton[] {
                    new FindTranslatablesToolButton(app, "RPG::EventPage")
                });
            }
        };
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

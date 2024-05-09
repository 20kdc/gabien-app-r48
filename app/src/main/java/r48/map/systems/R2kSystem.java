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
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import r48.App;
import r48.IMapContext;
import r48.RubyTableR;
import r48.dbs.ObjectInfo;
import r48.imageio.BMP8IImageIOFormat;
import r48.imageio.PNG8IImageIOFormat;
import r48.imageio.XYZImageIOFormat;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
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
import r48.map.pass.R2kPassabilitySource;
import r48.map.tiles.LcfTileRenderer;
import r48.maptools.UIMTBase;
import r48.maptools.deep.UIMTFtrGdt01;
import r48.toolsets.utils.RMTranscriptDumper;

import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * ...
 * Created on 03/06/17.
 */
public class R2kSystem extends MapSystem implements IRMMapSystem, IDynobjMapSystem {
    public R2kSystem(App app) {
        super(app, new CacheImageLoader(new FixAndSecondaryImageLoader(app, "", "", new ChainedImageLoader(new IImageLoader[] {
                new ImageIOImageLoader(app, new XYZImageIOFormat(app), ".xyz", true),
                // This is actually valid, but almost nobody wanted to use BMP over one of PNG or XYZ. Who'd have guessed?
                new ImageIOImageLoader(app, new BMP8IImageIOFormat(app, 8), ".bmp", true),
                new ImageIOImageLoader(app, new PNG8IImageIOFormat(app), ".png", true),
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


    private @Nullable IRIO tsoById(long id) {
        return app.odb.getObject("RPG_RT.ldb").getObject().getIVar("@tilesets").getHashVal(DMKey.of(id));
    }

    // saveData is optional, and replaces some things.
    private StuffRenderer rendererFromMapAndTso(IRIO map, IRIO tileset, IEventAccess events, LcfTileRenderer tileRenderer) {
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(app, imageLoader, tileRenderer);
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        // Cannot get enough information without map & tileset
        if ((map != null) && (tileset != null)) {
            long scrollFlags = map.getIVar("@scroll_type").getFX();
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
            // Layer order seems to be this:
            // layer 1 lower
            // layer 2 lower
            // <events>
            // layer 1 upper
            // layer 2 upper
            layers = new IMapViewDrawLayer[] {
                new PanoramaMapViewDrawLayer(app, img, loopX, loopY, autoLoopX, autoLoopY, tbl.width, tbl.height, 320, 240, 1),
                new R2kTileMapViewDrawLayer(app, tbl, tileRenderer, 0, false, tileset, T.m.l_rk0l),
                new R2kTileMapViewDrawLayer(app, tbl, tileRenderer, 1, false, tileset, T.m.l_rk1l),
                    new EventMapViewDrawLayer(app, 0, events, eventRenderer, T.m.l_rkbp),
                    new EventMapViewDrawLayer(app, 1, events, eventRenderer, T.m.l_rkps), // Player/Same
                new R2kTileMapViewDrawLayer(app, tbl, tileRenderer, 0, true, tileset, T.m.l_rk0u),
                new R2kTileMapViewDrawLayer(app, tbl, tileRenderer, 1, true, tileset, T.m.l_rk1u),
                    new EventMapViewDrawLayer(app, 2, events, eventRenderer, T.m.l_rkap),
                new PassabilityMapViewDrawLayer(app, new R2kPassabilitySource(tbl, tileset, (scrollFlags & 2) != 0, (scrollFlags & 1) != 0), 16),
                    new EventMapViewDrawLayer(app, 0x7FFFFFFF, events, eventRenderer, ""),
                new GridMapViewDrawLayer(app),
                new BorderMapViewDrawLayer(app, tbl.width, tbl.height)
            };
        }
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer, layers);
    }

    @Override
    public StuffRenderer rendererFromTso(IRIO tso) {
        LcfTileRenderer tileRenderer = new LcfTileRenderer(app, imageLoader);
        tileRenderer.checkReloadTSO(tso);
        IEventGraphicRenderer eventRenderer = new R2kEventGraphicRenderer(app, imageLoader, tileRenderer);
        return new StuffRenderer(app, imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
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
            }, id, R2kRMLikeMapInfoBackend.sNameFromInt(id), "RPG::Map");
            rmdList.add(rmd);
        }
        return rmdList.toArray(new RMMapData[0]);
    }

    @Override
    public ILoadedObject getCommonEventRoot() {
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
            if (!allowCreate)
                if (app.odb.getObject(obj, null) == null)
                    return null;
            final IObjectBackend.ILoadedObject root = app.odb.getObject(obj, "RPG::Save");
            final LcfTileRenderer tileRenderer = new LcfTileRenderer(app, imageLoader); 
            return new MapViewDetails(app, obj, "RPG::Save") {
                @Override
                public MapViewState rebuild(String changed) {
                    int mapId = (int) root.getObject().getIVar("@party_pos").getIVar("@map").getFX();

                    final String objn = R2kRMLikeMapInfoBackend.sNameFromInt(mapId);
                    IObjectBackend.ILoadedObject map = app.odb.getObject(objn);
                    final IEventAccess events = new R2kSavefileEventAccess(app, obj, root, "RPG::Save");
                    if (map == null)
                        return MapViewState.getBlank(app, null, new String[] {
                                objn
                        }, events);

                    // Map okay - update tileset cache & render
                    long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                    IRIO lastTileset = tsoById(currentTsId);
                    tileRenderer.checkReloadTSO(lastTileset);

                    return MapViewState.fromRT(rendererFromMapAndTso(map.getObject(), lastTileset, events, tileRenderer), objn, new String[] {
                            objn,
                            "RPG_RT.ldb"
                    }, map.getObject(), "@data", true, events);
                }
                @Override
                public IEditingToolbarController makeToolbar(IMapToolContext context) {
                    return new MapEditingToolbarController(context, true);
                }
            };
        }
        // Map, Area
        final IObjectBackend.ILoadedObject root = app.odb.getObject("RPG_RT.lmt");
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
                return new MapViewDetails(app, mvd.objectId, mvd.objectSchema) {
                    @Override
                    public MapViewState rebuild(String changed) {
                        return mvd.rebuild(changed);
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
        if (!allowCreate)
            if (app.odb.getObject(objn, null) == null)
                return null;
        final IObjectBackend.ILoadedObject map = app.odb.getObject(objn, "RPG::Map");
        final IEventAccess iea = new TraditionalEventAccess(app, objn, "RPG::Map", "@events", 1, "RPG::Event");
        final LcfTileRenderer tileRenderer = new LcfTileRenderer(app, imageLoader);
        return new MapViewDetails(app, objn, "RPG::Map") {
            @Override
            public MapViewState rebuild(String changed) {
                long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                IRIO lastTileset = tsoById(currentTsId);
                tileRenderer.checkReloadTSO(lastTileset);
                return MapViewState.fromRT(rendererFromMapAndTso(map.getObject(), lastTileset, iea, tileRenderer), objn, new String[] {
                        "RPG_RT.ldb"
                }, map.getObject(), "@data", false, iea);
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

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
import r48.RubyTable;
import r48.dbs.ObjectInfo;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
import r48.io.data.IRIO;
import r48.map.*;
import r48.map.drawlayers.*;
import r48.map.events.IEventAccess;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.events.TraditionalEventAccess;
import r48.map.imaging.*;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.mapinfos.UIGRMMapInfos;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.XPTileRenderer;
import r48.toolsets.utils.RMTranscriptDumper;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created on 03/06/17.
 */
public class RXPSystem extends MapSystem implements IRMMapSystem, IDynobjMapSystem {
    public RXPSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("Graphics/", "", new ChainedImageLoader(new IImageLoader[] {
                new GabienImageLoader(".png"),
                new GabienImageLoader(".jpg"),
        }))), true);
    }

    protected static IRIO tsoById(long id) {
        IRIO tileset = null;
        int tid = (int) id;
        IRIO tilesets = AppMain.objectDB.getObject("Tilesets").getObject();
        if ((tid >= 0) && (tid < tilesets.getALen()))
            tileset = tilesets.getAElem(tid);
        if (tileset != null)
            if (tileset.getType() == '0')
                tileset = null;
        return tileset;
    }

    @Override
    public UIElement createMapExplorer(IMapContext mapBox, String mapInfos) {
        return new UIGRMMapInfos(new RXPRMLikeMapInfoBackend(), mapBox, mapInfos);
    }

    @Override
    public Rect getIdealGridForImage(String path, Size img) {
        String pop = GaBIEn.parentOf(path);
        if (pop == null)
            return null;
        String id = GaBIEn.nameOf(pop);
        if (id.equalsIgnoreCase("Characters"))
            return getIdealGridForCharacter(GaBIEn.nameOf(path), img);
        if (id.equalsIgnoreCase("Tilesets"))
            return new Rect(0, 0, 32, 32);
        if (id.equalsIgnoreCase("Autotiles"))
            return new Rect(0, 0, 32, 32);
        if (id.equalsIgnoreCase("Animations"))
            return new Rect(0, 0, 192, 192);
        return null;
    }

    protected Rect getIdealGridForCharacter(String basename, Size img) {
        return new Rect(0, 0, img.width / 4, img.height / 4);
    }

    public StuffRenderer rendererFromMapAndTso(IRIO map, IRIO tileset, IEventAccess events) {
        XPTileRenderer tileRenderer = new XPTileRenderer(imageLoader, tileset);
        RMEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        String pano = "";
        if (tileset != null) {
            IRIO rio = tileset.getIVar("@panorama_name");
            if (rio != null) {
                String sv = rio.decString();
                if (sv.length() > 0)
                    pano = "Panoramas/" + sv;
            }
        }
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        if (map != null) {
            RubyTable rt = new RubyTable(map.getIVar("@data").getBuffer());
            IImage panoImg = null;
            if (!pano.equals(""))
                panoImg = imageLoader.getImage(pano, true);
            RXPAccurateDrawLayer accurate = new RXPAccurateDrawLayer(rt, events, tileRenderer, eventRenderer);
            layers = new IMapViewDrawLayer[] {
                    // works for green docks
                    new PanoramaMapViewDrawLayer(panoImg, true, true, 0, 0, rt.width, rt.height, -1, -1, 2, 1, 0),
                    // Signal layers (controls Z-Emulation)
                    accurate.tileSignalLayers[0],
                    accurate.tileSignalLayers[1],
                    accurate.tileSignalLayers[2],
                    accurate.signalLayerEvA,
                    accurate.signalLayerEvB,
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
        ITileRenderer tileRenderer = new XPTileRenderer(imageLoader, tso);
        IEventGraphicRenderer eventRenderer = new RMEventGraphicRenderer(imageLoader, tileRenderer, false);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, new IMapViewDrawLayer[0]);
    }

    @Override
    public MapViewDetails mapViewRequest(final String gum, boolean allowCreate) {
        if (!allowCreate)
            if (AppMain.objectDB.getObject(gum, null) == null)
                return null;
        final IObjectBackend.ILoadedObject map = AppMain.objectDB.getObject(gum, "RPG::Map");
        final IEventAccess events = new TraditionalEventAccess(gum, "RPG::Map", "@events", 1, "RPG::Event");
        return new MapViewDetails(gum, "RPG::Map", new IFunction<String, MapViewState>() {
            private RTilesetCacheHelper tilesetCache = new RTilesetCacheHelper("Tilesets");
            @Override
            public MapViewState apply(String changed) {
                long currentTsId = map.getObject().getIVar("@tileset_id").getFX();
                IRIO lastTileset = tilesetCache.receivedChanged(changed, currentTsId);
                if (lastTileset == null) {
                    lastTileset = tsoById(currentTsId);
                    tilesetCache.insertTileset(currentTsId, lastTileset);
                }
                return MapViewState.fromRT(rendererFromMapAndTso(map.getObject(), lastTileset, events), gum, new String[] {
                        "Tilesets"
                }, map.getObject(), "@data", false, events);
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
        IRIO mi = AppMain.objectDB.getObject("MapInfos").getObject();
        for (IRIO rio : mi.getHashKeys()) {
            int id = (int) rio.getFX();
            RMMapData rmd = new RMMapData(mi.getHashVal(rio).getIVar("@name").decString(), id, RXPRMLikeMapInfoBackend.sNameFromInt(id), "RPG::Map");
            rmdList.add(rmd);
        }
        Collections.sort(rmdList, RMMapData.COMPARATOR);
        return rmdList.toArray(new RMMapData[0]);
    }

    @Override
    public ILoadedObject getCommonEventRoot() {
        return AppMain.objectDB.getObject("CommonEvents");
    }

    @Override
    public IRIO[] getAllCommonEvents() {
        LinkedList<IRIO> rmdList = new LinkedList<IRIO>();
        IRIO ilo = getCommonEventRoot().getObject();
        int alen = ilo.getALen();
        for (int i = 0; i < alen; i++) {
            IRIO e = ilo.getAElem(i);
            if (e.getType() != '0')
                rmdList.add(e);
        }
        return rmdList.toArray(new IRIO[0]);
    }

    @Override
    public void dumpCustomData(RMTranscriptDumper dumper) {
        dumper.startFile("Items", TXDB.get("The list of items in the game."));
        LinkedList<String> lls = new LinkedList<String>();
        IRIO items = AppMain.objectDB.getObject("Items").getObject();
        int itemCount = items.getALen();
        for (int i = 0; i < itemCount; i++) {
            IRIO item = items.getAElem(i);
            if (item.getType() != '0') {
                lls.add(item.getIVar("@name").decString());
            } else {
                lls.add("<NULL>");
            }
        }
        dumper.dumpBasicList("Names", lls.toArray(new String[0]), 0);
        dumper.endFile();

        dumper.startFile("System", TXDB.get("System data (of any importance, anyway)."));
        IObjectBackend.ILoadedObject sys = AppMain.objectDB.getObject("System");

        dumper.dumpHTML(TXDB.get("Notably, switch and variable lists have a 0th index, but only indexes starting from 1 are actually allowed to be used.") + "<br/>");
        IRIO sys2 = sys.getObject();
        dumper.dumpHTML(TXDB.get("Magic number:") + sys2.getIVar("@magic_number").toString() + "<br/>");
        dumper.dumpHTML(TXDB.get("Magic number II:") + sys2.getIVar("@_").toString() + "<br/>");

        dumper.dumpSVList("@switches", sys2.getIVar("@switches"), 0);
        dumper.dumpSVList("@variables", sys2.getIVar("@variables"), 0);
        dumper.endFile();
    }

    @Override
    public String mapReferentToGUM(IRIO mapReferent) {
        return RXPRMLikeMapInfoBackend.sNameFromInt((int) mapReferent.getFX());
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

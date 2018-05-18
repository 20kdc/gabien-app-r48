/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.*;
import r48.*;
import r48.dbs.TSDB;
import r48.dbs.TXDB;
import r48.io.PathUtils;
import r48.io.cs.CSOObjectBackend;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.drawlayers.*;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.TraditionalEventAccess;
import r48.map.imaging.*;
import r48.map.tiles.GenericTileRenderer;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.IndirectTileRenderer;
import r48.map.tiles.NullTileRenderer;

import java.io.OutputStream;

/**
 * It's a secret to everybody.
 *
 * NOTE: GUMs here are CSO object names without the "all:" prefix.
 *
 * Created on 11th May 2018
 */
public class CSOSystem extends MapSystem {
    public String spawns = TXDB.get("Player Spawns");
    public String boops = TXDB.get("+ New Spawn");
    public IImage cts;
    public CSOSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("", "", new ChainedImageLoader(new IImageLoader[] {
                // PNGs are NOT interpreted via PNG8I, ever
                new GabienImageLoader(".png")
        }))), true);
        cts = new TSDB("CSO/TileInfo.txt").compileSheet(256, 16);
    }

    @Override
    public UIElement createMapExplorer(IConsumer<UIElement> windowMaker, final IMapContext mapBox, String mapInfos) {
        final UIScrollLayout usl = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public String toString() {
                return TXDB.get("Map List");
            }
        };
        final Runnable refresh = new Runnable() {
            @Override
            public void run() {
                usl.panelsClear();
                String base = PathUtils.autoDetectWindows(AppMain.rootPath + "stages");
                if (!GaBIEn.dirExists(base))
                    GaBIEn.makeDirectories(base);
                if (!GaBIEn.dirExists(base)) {
                    usl.panelsAdd(new UILabel(TXDB.get("Instability warning: Unable to create stages folder. Remove offending conflicting file and refresh."), FontSizes.mapInfosTextHeight));
                } else {
                    for (String gamemode : GaBIEn.listEntries(base)) {
                        String adw = PathUtils.autoDetectWindows(AppMain.rootPath + "stages/" + gamemode);
                        if (GaBIEn.dirExists(adw)) {
                            for (String map : GaBIEn.listEntries(adw)) {
                                if (GaBIEn.dirExists(PathUtils.autoDetectWindows(AppMain.rootPath + "stages/" + gamemode + "/" + map))) {
                                    final String mapFinale = gamemode + "/" + map;
                                    usl.panelsAdd(new UISplitterLayout(new UITextButton(mapFinale, FontSizes.mapInfosTextHeight, new Runnable() {
                                        @Override
                                        public void run() {
                                            mapBox.loadMap(mapFinale);
                                        }
                                    }), new UITextButton(TXDB.get("Match Info"), FontSizes.mapInfosTextHeight, new Runnable() {
                                        @Override
                                        public void run() {
                                            AppMain.launchSchema("CSOMatchData", AppMain.objectDB.getObject("mtd:" + mapFinale, "CSOMatchData"), null);
                                        }
                                    }), false, 1d));
                                }
                            }
                        }
                    }
                }
                final UITextButton refresh = new UITextButton(TXDB.get("Refresh"), FontSizes.mapInfosTextHeight, this);
                final UITextBox gameMode = new UITextBox("ffa", FontSizes.mapInfosTextHeight);
                final UITextBox mapName = new UITextBox("MyMap", FontSizes.mapInfosTextHeight);
                UITextButton newB = new UITextButton(TXDB.get("New Map"), FontSizes.mapInfosTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        // Ok, so this gets odd. See, if another map already exists, case-insensitive, we need to stop the user.
                        // We also want to verify that the map name is valid.
                        String gn = gameMode.text + "/" + mapName.text;
                        CSOObjectBackend.CSOParsedOP op = CSOObjectBackend.parseObjectName(gn);
                        CSOObjectBackend.CSOParsedOP op2 = CSOObjectBackend.parseObjectName("mtd:" + gn);
                        if ((op == null) || (op2 == null)) {
                            AppMain.launchDialog(TXDB.get("The map name or game mode given wasn't valid."));
                        } else if (GaBIEn.fileOrDirExists(PathUtils.autoDetectWindows(AppMain.rootPath + "stages/" + op.fileName))) {
                            AppMain.launchDialog(TXDB.get("It seems map with this name already exists, so it cannot be created. (You may have to delete a directory or file with this name.)"));
                        } else {
                            String dir = GaBIEn.dirname(PathUtils.autoDetectWindows(AppMain.rootPath + "stages/" + op.fileName));
                            GaBIEn.makeDirectories(dir);
                            AppMain.csoNewMapMagic(op.fileName);
                            RubyIO rio1 = AppMain.objectDB.getObject(gn, "CSOMap");
                            RubyIO rio2 = AppMain.objectDB.getObject("mtd:" + gn, "CSOMatchData");
                            AppMain.objectDB.ensureSaved(gn, rio1);
                            AppMain.objectDB.ensureSaved("mtd:" + gn, rio2);
                            mapBox.loadMap(gn);
                            AppMain.launchDialog(TXDB.get("Please go to Map.\nNote: You may need to use ... -> Reload TS after placing/editing the tileset."));
                            refresh.onClick.run();
                        }
                    }
                });
                usl.panelsAdd(new UISplitterLayout(new UISplitterLayout(gameMode, mapName, false, 0.5d), new UISplitterLayout(newB, refresh, false, 1), false, 1));
            }
        };
        refresh.run();
        return usl;
    }

    @Override
    public StuffRenderer rendererFromTso(RubyIO target) {
        IImage pano = GaBIEn.getErrorImage();
        IMapViewDrawLayer[] layers = new IMapViewDrawLayer[0];
        ITileRenderer tr = new NullTileRenderer();
        final IImage quote = GaBIEn.getImageEx("CSO/quote.png", false, true);
        IEventGraphicRenderer ev = new IEventGraphicRenderer() {
            @Override
            public int determineEventLayer(RubyIO event) {
                return 0;
            }

            @Override
            public RubyIO extractEventGraphic(RubyIO event) {
                return event;
            }

            @Override
            public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd, int sprScale) {
                int scx = 0;
                if (target.getInstVarBySymbol("@type").fixnumVal < 0)
                    scx = 1;
                igd.blitScaledImage(scx * 16, 0, 16, 16, ox, oy, 16 * sprScale, 16 * sprScale, quote);
            }
        };
        if (target != null) {
            // Target might be the map, which is used as a TSO for the PXA access.
            if (target.type != '"')
                target = new RubyIO().setString(AppMain.objectDB.getIdByObject(target), true);
        }
        if (target != null) {
            String str = target.decString();
            CSOObjectBackend.CSOParsedOP pop = CSOObjectBackend.parseObjectName(str);
            pano = imageLoader.getImage(AppMain.dataPath + pop.fileName + "BG", true);
            tr = new GenericTileRenderer(imageLoader.getImage(AppMain.dataPath + pop.fileName, true), 16, 16, 256);
            RubyIO target2 = AppMain.objectDB.getObject(str);
            TraditionalEventAccess tea = new TraditionalEventAccess(target2, "@psp", 0, "SPEvent", spawns, boops);
            // biscuits are not available in this build.
            RubyTable pxmTab = new RubyTable(target2.getInstVarBySymbol("@pxm").userVal);
            RubyTable pxaTab = new RubyTable(target2.getInstVarBySymbol("@pxa").userVal);
            // KEEP IN SYNC WITH THUMBNAIL CREATOR
            layers = new IMapViewDrawLayer[] {
                    new PanoramaMapViewDrawLayer(pano, true, true, 0, 0, 0, 0, 0, 0, 1),
                    new TileMapViewDrawLayer(pxmTab, 0, tr),
                    new TileMapViewDrawLayer(pxmTab, 0, new IndirectTileRenderer(pxaTab, new GenericTileRenderer(cts, 16, 256, 256))),
                    new EventMapViewDrawLayer(0, tea, ev, 16),
                    new EventMapViewDrawLayer(0x7FFFFFFF, tea, ev, 16),
                    new GridMapViewDrawLayer()
            };
        }
        return new StuffRenderer(imageLoader, tr, ev, layers, new boolean[] {
                true,
                true,
                false,
                true,
                true,
                false
        });
    }

    @Override
    public MapViewDetails mapViewRequest(final String gum, boolean allowCreate) {
        if (!allowCreate)
            if (AppMain.objectDB.getObject(gum, null) == null)
                return null;
        final RubyIO mapRIO = AppMain.objectDB.getObject(gum, "CSOMap");
        return new MapViewDetails(gum, "CSOMap", new IFunction<String, MapViewState>() {
            @Override
            public MapViewState apply(String s) {
                return createMapViewState(gum, mapRIO);
            }
        }, new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext iMapToolContext) {
                return new MapEditingToolbarController(iMapToolContext, false);
            }
        });
    }

    private MapViewState createMapViewState(String gum, RubyIO mapRIO) {
        return MapViewState.fromRT(rendererFromTso(new RubyIO().setString(gum, true)), gum, new String[] {}, mapRIO, "@pxm", false, new TraditionalEventAccess(mapRIO, "@psp", 0, "SPEvent", spawns, boops));
    }

    @Override
    public void saveHook(String objectName) {
        CSOObjectBackend.CSOParsedOP op = CSOObjectBackend.parseObjectName(objectName);
        if (op == null) {
            System.err.println("CSOSystem had to deal with unknown obj '" + objectName + "', Leo please stop using CSOSystem");
            return;
        }
        final RubyIO mapRIO = AppMain.objectDB.getObject(objectName);
        if (mapRIO == null) {
            System.err.println("Hook oddity: saved and then gone?");
            return;
        }
        if (op.subtype == CSOObjectBackend.CSOSubtype.Main) {
            // EXTREMELY HACKY BUT NECESSARY
            MapViewState mvs = createMapViewState(objectName, mapRIO);
            int ts = mvs.renderer.tileRenderer.getTileSize();
            int w = mvs.width * ts;
            int h = mvs.height * ts;
            int px = Math.max(0, (426 - w) / 2);
            int py = Math.max(0, (240 - h) / 2);
            w = Math.max(w, 426);
            h = Math.max(h, 240);
            boolean[] vis = new boolean[mvs.renderer.layers.length];
            vis[0] = true;
            vis[1] = true;
            vis[3] = true;
            IGrDriver igd = GaBIEn.makeOffscreenBuffer(w, h, true);
            mvs.renderCore(igd, -px, -py, vis, 0, false);
            byte[] thumb = igd.createPNG();
            OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "stages/" + op.fileName + "Thumb.png"));
            try {
                os.write(thumb);
                os.close();
            } catch (Exception e) {
                try {
                    os.close();
                } catch (Exception e2) {

                }
            }
            igd.shutdown();
        }
    }
}

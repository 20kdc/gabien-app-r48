/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.systems;

import gabien.uslx.append.*;

import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.render.IGrDriver;
import gabien.ui.*;
import gabien.ui.dialogs.UIPopupMenu;
import r48.App;
import r48.IMapContext;
import r48.ITileAccess;
import r48.RubyTable;
import r48.dbs.ObjectRootHandle;
import r48.dbs.ObjectInfo;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapViewDrawContext;
import r48.map.StuffRenderer;
import r48.map.drawlayers.GridMapViewDrawLayer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.drawlayers.PassabilityMapViewDrawLayer;
import r48.map.events.IEventAccess;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.LoopTileAccess;
import r48.map.tiles.NOPWriteTileAccess;
import r48.schema.SchemaElement;

/**
 * Responsible for creating NSRs and such.
 * Everything that makes R48 a map editor hangs off of this.
 * The default system is the Null system.
 * Note that, at least theoretically, it is possible to run any system on the "R48" Ruby object backend, if you wish...
 * ...if it's a good idea is not my place to say. :)
 * Created on 03/06/17.
 */
public abstract class MapSystem extends App.Svc {

    // All implementations will probably use a common image loader across the mapsystem.
    // It's not an absolute, but it's pretty likely.
    protected final IImageLoader imageLoader;
    // If this is off, almost everything here is inaccessible,
    //  apart from the generic StuffRenderers
    public final boolean enableMapSubsystem;

    public MapSystem(App app, IImageLoader imgLoad, boolean enableSwitch) {
        super(app);
        imageLoader = imgLoad;
        enableMapSubsystem = enableSwitch;
    }

    /**
     * Maps an object ID to a schema ID.
     * Notably, can always be overridden by File. schemas.
     */
    protected @NonNull String mapObjectIDToSchemaID(String objectID) {
        return "File." + objectID;
    }

    /**
     * Maps an object ID to a schema element.
     */
    public final @Nullable SchemaElement mapObjectIDToSchema(String objectID) {
        String filedot = "File." + objectID;
        if (app.sdb.hasSDBEntry(filedot))
            return app.sdb.getSDBEntry(filedot);

        String res = mapObjectIDToSchemaID(objectID);
        if (app.sdb.hasSDBEntry(res))
            return app.sdb.getSDBEntry(res);

        return null;
    }

    /**
     * Returns all dynamic object IDs that are part of the game.
     * Object IDs that are not part of the game (save files) do not count.
     */
    public @NonNull ObjectInfo[] getDynamicObjects() {
        return new ObjectInfo[0];
    }

    protected static ObjectInfo[] dynamicObjectsFromRM(IRMMapSystem rm) {
        IRMMapSystem.RMMapData[] maps = rm.getAllMaps();
        ObjectInfo[] dobj = new ObjectInfo[maps.length];
        for (int i = 0; i < dobj.length; i++)
            dobj[i] = maps[i];
        return dobj;
    }

    public static @NonNull MapSystem create(App app, String sysBackend) {
        if (sysBackend.equals("null")) {
            return new NullSystem(app);
        } else if (sysBackend.equals("RXP")) {
            return new RXPSystem(app);
        } else if (sysBackend.equals("RVXA")) {
            return new RVXASystem(app);
        } else if (sysBackend.equals("Ika")) {
            return new IkaSystem(app);
        } else if (sysBackend.equals("R2k")) {
            return new R2kSystem(app);
        } else {
            throw new RuntimeException("Unknown MapSystem backend " + sysBackend);
        }
    }

    // If null, the map explorer is not enabled.
    public UIElement createMapExplorer(final IMapContext mapBox, final String mapInfos) {
        return new UIPopupMenu(new String[] {
                T.m.bLoadMap
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        mapBox.loadMap("Map");
                    }
                }
        }, app.f.menuTH, app.f.menuS, false) {
            @Override
            public String toString() {
                return mapInfos;
            }
        };
    }

    // If null, the save explorer is not enabled.
    public UIElement createSaveExplorer(final IMapContext mapBox, String saves) {
        return null;
    }

    /**
     * Creates Engine Tools population.
     */
    public Consumer<LinkedList<UIPopupMenu.Entry>> createEngineTools() {
        return (entries) -> {};
    }

    public Rect getIdealGridForImage(String path, Size img) {
        // NOTE: The implementations of this right now just follow Demetrius's rules, see the additions to issue #20.
        // System.out.println("IMGDETECTGRID: " + path);
        return null;
    }

    // Converts "map_id"-style elements to their GUM strings
    // Returns null if the reference doesn't exist.
    public String mapReferentToGUM(IRIO mapReferent) {
        return "Map";
    }

    // This is used in cases when a StuffRenderer is needed outside of a map.
    // If target is null, then this isn't a tileset object, so it has to just act generically.
    // Otherwise, this is a tileset object, so use that info.
    public abstract StuffRenderer rendererFromTso(IRIO target);

    // Used to prepare a UIMapView. Can only return null if allowCreate is false.
    // Can throw an exception if the GUM is actually invalid, because GUMs are internal strings and can't be entered by the user.
    public abstract MapViewDetails mapViewRequest(String gum, boolean allowCreate);

    // Some MapSystems (CSOMapSystem) want to update ancillary files in a way that isn't compatible with the IMI-friendly "purist IO backend" stuff.
    // These files are NON-CRITICAL (thumbnails & such) so the solution is this function, which is triggered on a successful save.
    public void saveHook(String objectName) {

    }

    public boolean engineUsesPal0Colourkeys() {
        return false;
    }

    /*
     * Acts as a wrapper around RubyTable, but also provides a StuffRenderer separately.
     * Calling setTiletype requires modification notification.
     * Calling resize requires *immediate* modification notification, leading to state replacement.
     * Notably, the wrapping is NOT to be used for the renderers because the wrapping kind of noms performance.
     */
    public static class MapViewState {
        /**
         * Customized renderer.
         */
        public final StuffRenderer renderer;
        /**
         * Map draw layers.
         */
        public final IMapViewDrawLayer[] layers;
        /**
         * Default states of map draw layers.
         */
        public final boolean[] activeDef;
        /**
         * Used for __MAP__ dictionaries
         */
        public final String underscoreMapObjectId;
        /**
         * This is an additional list of object roots to listen for modifications on.
         */
        public final String[] refreshOnObjectChange;
        /**
         * Only for tool use, so can be null if the tools won't ever access it
         */
        public final IEventAccess eventAccess;
        /**
         * "The Obvious" attributes.
         */
        public final int width, height, planeCount;
        /**
         * Read/write tile data
         */
        public final ITileAccess.RW tileAccess;
        /**
         * int[] contains X, Y, (defaults...)
         */
        public final Consumer<int[]> resize;

        public MapViewState(StuffRenderer r, @NonNull IMapViewDrawLayer[] layers, @Nullable boolean[] activeDefault, String usm, String[] exrefresh, ITileAccess.RWBounded tileAccess, Consumer<int[]> rz, IEventAccess iea) {
            renderer = r;
            this.layers = layers;
            if (activeDefault != null) {
                activeDef = activeDefault;
            } else {
                activeDef = new boolean[layers.length];
                for (int i = 0; i < activeDef.length; i++) {
                    activeDef[i] = true;
                    if (layers[i] instanceof PassabilityMapViewDrawLayer)
                        activeDef[i] = false;
                    if (layers[i] instanceof GridMapViewDrawLayer)
                        activeDef[i] = false;
                }
            }
            underscoreMapObjectId = usm;
            refreshOnObjectChange = exrefresh;
            width = tileAccess.getBounds().width;
            height = tileAccess.getBounds().height;
            planeCount = tileAccess.getPlanes();
            this.tileAccess = tileAccess;
            resize = rz;
            eventAccess = iea;
        }

        public boolean outOfBounds(int mouseXT, int mouseYT) {
            return !tileAccess.coordAccessible(mouseXT, mouseYT);
        }

        public boolean outOfBoundsUnlooped(int mouseXT, int mouseYT) {
            if (mouseXT < 0)
                return true;
            if (mouseYT < 0)
                return true;
            if (mouseXT >= width)
                return true;
            if (mouseYT >= height)
                return true;
            return false;
        }

        /**
         * Renderer for mapshots/etc.
         */
        public void renderCore(IGrDriver igd, int vCX, int vCY, boolean[] layerVis, int currentLayer, boolean debugToggle) {
            int tileSize = renderer.tileRenderer.tileSize;

            MapViewDrawContext mvdc = new MapViewDrawContext(renderer.app, new Rect(vCX, vCY, igd.getWidth(), igd.getHeight()), tileSize, false);

            mvdc.currentLayer = currentLayer;
            mvdc.debugToggle = debugToggle;
            mvdc.igd = igd;

            for (int i = 0; i < layers.length; i++)
                if (layerVis[i])
                    layers[i].draw(mvdc);
        }

        public static MapViewState getBlank(App app, String underscoreMapObjectId, String[] ex, IEventAccess iea) {
            return new MapViewState(app.stuffRendererIndependent, new IMapViewDrawLayer[0], null, underscoreMapObjectId, ex, new ITileAccess.RWBounded() {
                @Override
                public Rect getBounds() {
                    return Rect.ZERO;
                }
                @Override
                public int getPlanes() {
                    return 0;
                }
                @Override
                public int getPBase(int p) {
                    return -1;
                }
                @Override
                public int getXBase(int x) {
                    return -1;
                }
                @Override
                public int getYBase(int y) {
                    return -1;
                }
                @Override
                public int getTiletypeRaw(int cellID) {
                    return 0;
                }
                @Override
                public void setTiletypeRaw(int cellID, int value) {
                }
            }, new Consumer<int[]>() {
                @Override
                public void accept(int[] ints) {

                }
            }, iea);
        }

        public static MapViewState fromRT(@NonNull StuffRenderer stuffRenderer, @NonNull IMapViewDrawLayer[] mvdl, @Nullable boolean[] activeDef, String underscoreMapObjectId, String[] ex, final IRIO its, final String str, final boolean readOnly, IEventAccess iea, final boolean loopX, final boolean loopY) {
            // This happens once in a blue moon, it's fine
            final IRIO sz = PathSyntax.compile(stuffRenderer.app, str).getRW(its);
            final RubyTable rt = new RubyTable(sz.editUser());
            ITileAccess.RWBounded rtLooped = LoopTileAccess.of(rt, loopX, loopY);
            ITileAccess.RWBounded tar = rtLooped;
            if (readOnly)
                tar = new NOPWriteTileAccess(tar);
            return new MapViewState(stuffRenderer, mvdl, activeDef, underscoreMapObjectId, ex, tar, (ints) -> {
                if (readOnly)
                    return;
                int[] defs = new int[ints.length - 2];
                for (int i = 0; i < defs.length; i++)
                    defs[i] = ints[i + 2];
                if (its.getIVar("@width") != null)
                    its.getIVar("@width").setFX(ints[0]);
                if (its.getIVar("@height") != null)
                    its.getIVar("@height").setFX(ints[1]);
                sz.putBuffer(rt.resize(ints[0], ints[1], defs).data);
            }, iea);
        }

        public int getTiletype(int i, int i1, int i2) {
            return tileAccess.getTiletype(i, i1, i2);
        }

        public void setTiletype(int i, int i1, int i2, int i3) {
            tileAccess.setTiletype(i, i1, i2, i3);
        }

        public void resize(int w, int h) {
            int[] r = new int[planeCount + 2];
            r[0] = w;
            r[1] = h;
            if (w > 0)
                if (h > 0)
                    for (int i = 0; i < planeCount; i++)
                        r[i + 2] = tileAccess.getTiletype(0, 0, i);
            resize.accept(r);
        }
    }

    /**
     * Represents a map editing session.
     */
    public static abstract class MapViewDetails {
        // Used for bringing up relevant dialogs & adding listeners.
        // MapViewState really runs the show
        // dictionaryObjectId is used for __MAP__
        public final @NonNull String objectId;
        public final @NonNull SchemaElement objectSchema;
        // NOTE: The main modification listener gets inserted on this root,
        //        and changes to the map cause this root to be modified.
        // Additional modification listeners are inserted on a per-State basis.
        public final ObjectRootHandle object;

        public MapViewDetails(App app, String o, ObjectRootHandle orh) {
            final SchemaElement rootSchema2 = orh.rootSchema;
            // Eclipse check says it has to be done this way despite rootSchema being final
            if (rootSchema2 == null)
                throw new RuntimeException("Schema cannot be null for MapViewDetails.");
            objectSchema = rootSchema2;
            objectId = o;
            object = orh;
        }

        /**
         * Rebuilds the MapViewState on a change.
         */
        public abstract MapViewState rebuild();

        /**
         * Creates a toolbar.
         */
        public abstract IEditingToolbarController makeToolbar(IMapToolContext context);
    }
}

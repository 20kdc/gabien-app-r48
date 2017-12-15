/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import gabien.ui.*;
import r48.*;
import r48.dbs.TXDB;
import r48.map.IEditingToolbarController;
import r48.map.IMapToolContext;
import r48.map.MapEditingToolbarController;
import r48.map.StuffRenderer;
import r48.map.imaging.IImageLoader;

/**
 * Responsible for creating NSRs and such.
 * Everything that makes R48 a map editor hangs off of this.
 * The default system is the Null system.
 * Note that, at least theoretically, it is possible to run any system on the "R48" Ruby object backend, if you wish...
 * ...if it's a good idea is not my place to say. :)
 * Created on 03/06/17.
 */
public abstract class MapSystem {

    // All implementations will probably use a common image loader across the mapsystem.
    // It's not an absolute, but it's pretty likely.
    protected final IImageLoader imageLoader;

    public MapSystem(IImageLoader imgLoad) {
        imageLoader = imgLoad;
    }

    // If null, maps are not enabled.
    public abstract String mapSchema();

    public UIElement createMapExplorer(final ISupplier<IConsumer<UIElement>> windowMaker, final IMapContext mapBox) {
        return new UIPopupMenu(new String[] {
                TXDB.get("Load Map")
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        mapBox.loadMap("Map");
                    }
                }
        }, FontSizes.menuTextHeight, false);
    }

    // Converts "map_id"-style elements to their GUM strings
    // Returns null if the reference doesn't exist.
    public String mapReferentToGUM(RubyIO mapReferent) {
        return "Map";
    }

    // This is used in cases when a StuffRenderer is needed outside of a map.
    // If target is null, then this isn't a tileset object, so it has to just act generically.
    // Otherwise, this is a tileset object, so use that info.
    public abstract StuffRenderer rendererFromTso(RubyIO target);

    // Used to prepare a UIMapView.
    public abstract MapViewDetails mapViewRequest(String gum);

    // Used to get UI details.
    public IFunction<IMapToolContext, IEditingToolbarController> mapLoadRequest(String gum, final ISupplier<IConsumer<UIElement>> windowMaker) {
        return new IFunction<IMapToolContext, IEditingToolbarController>() {
            @Override
            public IEditingToolbarController apply(IMapToolContext uiMapView) {
                return new MapEditingToolbarController(uiMapView, windowMaker);
            }
        };
    }

    /*
     * Acts as a wrapper around RubyTable, but also provides a StuffRenderer separately.
     * Calling setTiletype requires modification notification.
     * Calling resize requires *immediate* modification notification, leading to state replacement.
     * Notably, the wrapping is NOT to be used for the renderers because the wrapping kind of noms performance.
     */
    public static class MapViewState {
        public final StuffRenderer renderer;
        public final int width, height, planeCount;
        // int[] contains X, Y, Layer
        public final IFunction<int[], Short> getTileData;
        // int[] contains X, Y, Layer, value
        public final IConsumer<int[]> setTileData;
        // int[] contains X, Y, (defaults...)
        public final IConsumer<int[]> resize;

        public MapViewState(StuffRenderer r, int w, int h, int pc, IFunction<int[], Short> gtd, IConsumer<int[]> std, IConsumer<int[]> rz) {
            renderer = r;
            width = w;
            height = h;
            planeCount = pc;
            getTileData = gtd;
            setTileData = std;
            resize = rz;
        }

        public boolean outOfBounds(int mouseXT, int mouseYT) {
            if (mouseXT < 0)
                return false;
            if (mouseYT < 0)
                return false;
            if (mouseXT >= width)
                return false;
            if (mouseYT >= height)
                return false;
            return true;
        }

        public static MapViewState fromRT(StuffRenderer stuffRenderer, final RubyIO its, final String str) {
            final RubyTable rt = new RubyTable(its.getInstVarBySymbol(str).userVal);
            return new MapViewState(stuffRenderer, rt.width, rt.height, rt.planeCount, new IFunction<int[], Short>() {
                @Override
                public Short apply(int[] ints) {
                    return rt.getTiletype(ints[0], ints[1], ints[2]);
                }
            }, new IConsumer<int[]>() {
                @Override
                public void accept(int[] ints) {
                    rt.setTiletype(ints[0], ints[1], ints[2], (short) ints[3]);
                }
            }, new IConsumer<int[]>() {
                @Override
                public void accept(int[] ints) {
                    int[] defs = new int[ints.length - 2];
                    for (int i = 0; i < defs.length; i++)
                        defs[i] = ints[i + 2];
                    if (its.getInstVarBySymbol("@width") != null)
                        its.getInstVarBySymbol("@width").fixnumVal = ints[0];
                    if (its.getInstVarBySymbol("@height") != null)
                        its.getInstVarBySymbol("@height").fixnumVal = ints[1];
                    rt.resize(ints[0], ints[1], defs);
                }
            });
        }

        public short getTiletype(int i, int i1, int i2) {
            return getTileData.apply(new int[] {i, i1, i2});
        }

        public void setTiletype(int i, int i1, int i2, short i3) {
            setTileData.accept(new int[] {i, i1, i2, i3});
        }

        public void resize(int w, int h) {
            int[] r = new int[planeCount];
            if (w > 0)
                if (h > 0)
                    for (int i = 0; i < r.length; i++)
                        r[i] = getTileData.apply(new int[] {0, 0, i}) & 0xFFFF;
        }
    }

    public static class MapViewDetails {
        // Used for bringing up relevant dialogs & adding listeners.
        // MapViewState really runs the show
        public final String objectId;
        public final RubyIO object;
        public final ISupplier<MapViewState> rendererRetriever;

        public MapViewDetails(String o, String os, ISupplier<MapViewState> mvs) {
            objectId = o;
            object = AppMain.objectDB.getObject(o, os);
            rendererRetriever = mvs;
        }
    }
}

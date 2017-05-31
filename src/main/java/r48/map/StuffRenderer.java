/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.imaging.IImageLoader;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.IkaEventGraphicRenderer;
import r48.map.events.NullEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.imaging.XYZImageLoader;
import r48.map.mapinfos.UIRMMapInfos;
import r48.map.tiles.*;

/**
 * First class of the new year. What does it do?
 * It's a grouping of stuff in other classes which has to go indirectly for sanity reasons.
 * (Example: UIMapView has to be the one /rendering/ tiles, but EPGDisplaySchemaElement
 * has absolutely no other reason to be in contact with the current UIMapView at all.)
 * This also has the nice effect of keeping the jarlightHax stuff out of some random UI code.
 *
 * -- May 29th, 2017 retrospective:
 * Basically, all of the version-specific IFDEF-like stuff that had to exist for maps went into here.
 * Better here than anywhere else, really.
 * (I suppose a StuffRenderer interface could've worked?)
 * The point is that ObjectBackend is independent of what the objects actually mean.
 * A JSONObjectBackend could serve a variety of purposes, all handling JSON objects with different map formats.
 * The Schemas and the StuffRenderer give *meaning* to that.
 *
 * Another thing to note is that the StuffRenderer is swapped about a lot. Specifically whenever the map changes.
 * There's also a magical "default" StuffRenderer which acts without a map context.
 * Parameters are (null, "").
 *
 * Created on 1/1/17.
 */
public class StuffRenderer {
    // can be set by SDB
    public static String versionId = "XP";

    public final ITileRenderer tileRenderer;
    public final IEventGraphicRenderer eventRenderer;
    public final IImageLoader imageLoader;

    public static UIElement createMapExplorer(final ISupplier<IConsumer<UIElement>> windowMaker, final UIMapViewContainer mapBox) {
        if (versionId.equals("VXA") || versionId.equals("RXP"))
            return new UIRMMapInfos(windowMaker, mapBox);
        return new UIPopupMenu(new String[] {
                "Load Map"
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        mapBox.loadMap("Map");
                    }
                }
        }, FontSizes.menuTextHeight, false);
    }

    public static StuffRenderer rendererFromMap(RubyIO map) {
        if (versionId.equals("VXA")) {
            String vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            if (map.getInstVarBySymbol("@parallax_show").type != 'T')
                vxaPano = "";
            return new StuffRenderer(tsoFromMap(map), vxaPano);
        }
        if (versionId.equals("RXP"))
            return new StuffRenderer(tsoFromMap(map), "");
        return new StuffRenderer(null, null);
    }

    private static RubyIO tsoFromMap(RubyIO map) {
        RubyIO tileset = null;
        int tid = (int) map.getInstVarBySymbol("@tileset_id").fixnumVal;
        RubyIO tilesets = AppMain.objectDB.getObject("Tilesets");
        if ((tid >= 0) && (tid < tilesets.arrVal.length))
            tileset = tilesets.arrVal[tid];
        if (tileset != null)
            if (tileset.type == '0')
                tileset = null;
        return tileset;
    }

    public StuffRenderer(RubyIO tso, String vxaPano) {
        if (versionId.equals("Ika")) {
            imageLoader = new GabienImageLoader(AppMain.rootPath + "Pbm/", ".pbm");
            tileRenderer = new IkaTileRenderer(imageLoader);
            eventRenderer = new IkaEventGraphicRenderer(imageLoader);
            return;
        }
        if (versionId.equals("lcf2000")) {
            imageLoader = new CacheImageLoader(new XYZImageLoader(AppMain.rootPath));
            tileRenderer = new LcfTileRenderer(imageLoader);
            eventRenderer = new NullEventGraphicRenderer();
            return;
        }
        if (versionId.equals("XP")) {
            imageLoader = new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png");
            tileRenderer = new XPTileRenderer(imageLoader, tso);
            eventRenderer = new RMEventGraphicRenderer(this);
            return;
        }
        if (versionId.equals("VXA")) {
            imageLoader = new GabienImageLoader(AppMain.rootPath + "Graphics/", ".png");
            tileRenderer = new VXATileRenderer(imageLoader, tso, vxaPano);
            eventRenderer = new RMEventGraphicRenderer(this);
            return;
        }
        tileRenderer = new NullTileRenderer();
        eventRenderer = new NullEventGraphicRenderer();
        imageLoader = new GabienImageLoader(AppMain.rootPath, "");
    }
}

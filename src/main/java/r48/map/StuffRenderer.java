/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import r48.AppMain;
import r48.RubyIO;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.IkaEventGraphicRenderer;
import r48.map.events.NullEventGraphicRenderer;
import r48.map.events.RMEventGraphicRenderer;
import r48.map.tiles.*;

/**
 * First class of the new year. What does it do?
 * It's a grouping of stuff in other classes which has to go indirectly for sanity reasons.
 * (Example: UIMapView has to be the one /rendering/ tiles, but EPGDisplaySchemaElement
 * has absolutely no other reason to be in contact with the current UIMapView at all.)
 * This also has the nice effect of keeping the jarlightHax stuff out of some random UI code.
 * Created on 1/1/17.
 */
public class StuffRenderer {
    // can be set by SDB
    public static String versionId = "XP";

    public final ITileRenderer tileRenderer;
    public final IEventGraphicRenderer eventRenderer;

    public static StuffRenderer rendererFromMap(RubyIO map) {
        String vxaPano = "";
        if (versionId.equals("VXA")) {
            vxaPano = map.getInstVarBySymbol("@parallax_name").decString();
            if (map.getInstVarBySymbol("@parallax_show").type != 'T')
                vxaPano = "";
        }
        if (versionId.equals("Ika"))
            return new StuffRenderer(null, null);
        return new StuffRenderer(tsoFromMap(map), vxaPano);
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
            tileRenderer = new IkaTileRenderer();
            eventRenderer = new IkaEventGraphicRenderer();
            return;
        }
        if (versionId.equals("XP")) {
            tileRenderer = new XPTileRenderer(tso);
            eventRenderer = new RMEventGraphicRenderer(this);
            return;
        }
        if (versionId.equals("VXA")) {
            tileRenderer = new VXATileRenderer(tso, vxaPano);
            eventRenderer = new RMEventGraphicRenderer(this);
            return;
        }
        tileRenderer = new NullTileRenderer();
        eventRenderer = new NullEventGraphicRenderer();
    }

}

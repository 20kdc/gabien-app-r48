/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.map.tiles.*;
import r48.RubyIO;

import java.util.HashMap;

/**
 * First class of the new year. What does it do?
 * It's a grouping of stuff in other classes which has to go indirectly for sanity reasons.
 * (Example: UIMapView has to be the one /rendering/ tiles, but EPGDisplaySchemaElement
 *   has absolutely no other reason to be in contact with the current UIMapView at all.)
 * This also has the nice effect of keeping the jarlightHax stuff out of some random UI code.
 * Created on 1/1/17.
 */
public class StuffRenderer {
    // can be set by SDB
    public static String versionId = "XP";
    private int patternCount = 4;
    private boolean useVXAExtensionScheme = false;

    private HashMap<String, IGrInDriver.IImage> additiveBlending = new HashMap<String, IGrInDriver.IImage>();

    public final ITileRenderer tileRenderer;

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
        if ((tid >= 0) && (tid < AppMain.tilesets.arrVal.length))
            tileset = AppMain.tilesets.arrVal[tid];
        if (tileset != null)
            if (tileset.type == '0')
                tileset = null;
        return tileset;
    }

    public StuffRenderer(RubyIO tso, String vxaPano) {
        if (versionId.equals("Ika")) {
            tileRenderer = new IkaTileRenderer();
            return;
        }
        if (versionId.equals("XP")) {
            tileRenderer = new XPTileRenderer(tso);
            return;
        }
        if (versionId.equals("VXA")) {
            patternCount = 3;
            useVXAExtensionScheme = true;
            tileRenderer = new VXATileRenderer(tso, vxaPano);
            return;
        }
        tileRenderer = new NullTileRenderer();
    }

    private int lookupDirection(int dir) {
        if (dir == 2)
            return 0;
        if (dir == 4)
            return 1;
        if (dir == 6)
            return 2;
        if (dir == 8)
            return 3;
        return -1;
    }

    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrInDriver igd) {
        int pat = (int) target.getInstVarBySymbol("@pattern").fixnumVal;
        int coreDir = (int) target.getInstVarBySymbol("@direction").fixnumVal;
        int dir = lookupDirection(coreDir);
        if (dir == -1) {
            dir = 0;
            UILabel.drawString(igd, ox, oy, "D" + coreDir, false, false);
        }
        RubyIO cName = target.getInstVarBySymbol("@character_name");
        short tId = (short) target.getInstVarBySymbol("@tile_id").fixnumVal;
        if (cName.strVal.length == 0) {
            tileRenderer.drawTile(tId, ox, oy, igd, tileRenderer.getTileSize());
        } else {
            // lower centre of tile, the reference point for characters
            ox += 16;
            oy += 32;
            String s = cName.decString();
            IGrInDriver.IImage i = GaBIEn.getImage(AppMain.rootPath + "Graphics/Characters/" + s + ".png", 0, 0, 0);
            int sprW = i.getWidth() / patternCount;
            int sprH = i.getHeight() / 4;
            // Direction 2, pattern 0 == 0, ? (safe @ cliffs, page 0)
            // Direction 2, pattern 2 == 2, ? (safe @ cliffs, page 1)
            int tx = pat;
            int ty = dir;

            if (useVXAExtensionScheme) {
                if (!s.startsWith("!$")) {
                    sprW = 32;
                    sprH = 32;
                    int idx = (int) target.getInstVarBySymbol("@character_index").fixnumVal;
                    // NOTE: still unsure on how segmentation works.
                    // for now, things work out?
                    ty += (idx / 4) * 4;
                    tx += (idx % 4) * 3;
                }
            }

            boolean doBlend = false;
            RubyIO blendData = target.getInstVarBySymbol("@blend_type");
            if (blendData != null)
                doBlend = target.getInstVarBySymbol("@blend_type").fixnumVal == 1;
            if (doBlend) {
                // firstly, let's edit the image
                if (!additiveBlending.containsKey(s)) {
                    int[] rpg = i.getPixels();
                    for (int j = 0; j < rpg.length; j++) {
                        // backup alpha, then remove it
                        int alD = ((rpg[j] & 0xFF000000) >> 24) & 0xFF;
                        rpg[j] &= 0xFFFFFF;

                        // extract components to try to work out something that looks OK
                        int alA = (rpg[j] & 0xFF);
                        int alB = (rpg[j] & 0xFF00) >> 8;
                        int alC = (rpg[j] & 0xFF0000) >> 16;
                        // This needs to simulate additive blending with mixing.
                        // Somehow.
                        int r = (alA + alB + alC) / 3;
                        r *= alD;
                        r /= 256;
                        // put in new alpha
                        rpg[j] |= r << 24;
                    }
                    additiveBlending.put(s, GaBIEn.createImage(rpg, i.getWidth(), i.getHeight()));
                }
                igd.blitImage(tx * sprW, ty * sprH, sprW, sprH, ox - (sprW / 2), oy - sprH, additiveBlending.get(s));
            } else {
                igd.blitImage(tx * sprW, ty * sprH, sprW, sprH, ox - (sprW / 2), oy - sprH, i);
            }
        }
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.UILabel;
import gabienapp.dbs.ATDB;
import gabienapp.schema.util.SchemaPath;

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

    public static final int tileSize = 32;

    private HashMap<String, IGrInDriver.IImage> additiveBlending = new HashMap<String, IGrInDriver.IImage>();

    private final RubyIO tileset;
    public final IGrInDriver.IImage[] tilesetMaps = new IGrInDriver.IImage[8];

    public static StuffRenderer rendererFromMap(RubyIO map) {
        RubyIO tileset = null;
        int tid = (int) map.getInstVarBySymbol("@tileset_id").fixnumVal;
        if ((tid >= 0) && (tid < Application.tilesets.arrVal.length))
            tileset = Application.tilesets.arrVal[tid];
        if (tileset != null)
            if (tileset.type == '0')
                tileset = null;
        return new StuffRenderer(tileset);
    }

    public StuffRenderer(RubyIO tileset) {
        this.tileset = tileset;
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
        if (tileset != null) {
            String expectedTS = tileset.getInstVarBySymbol("@tileset_name").decString();
            if (expectedTS.length() != 0)
                tilesetMaps[0] = GaBIEn.getImage(Application.rootPath + "Graphics/Tilesets/" + expectedTS + ".png", 0, 0, 0);
            RubyIO[] amNames = tileset.getInstVarBySymbol("@autotile_names").arrVal;
            for (int i = 0; i < 7; i++) {
                RubyIO rio = amNames[i];
                if (rio.strVal.length != 0) {
                    String expectedAT = rio.decString();
                    tilesetMaps[i + 1] = GaBIEn.getImage(Application.rootPath + "Graphics/Autotiles/" + expectedAT + ".png", 0, 0, 0);
                }
            }
        }
    }

    public void drawTile(short tidx, int px, int py, IGrInDriver igd, int ets) {
        // The logic here is only documented in the mkxp repository, in tilemap.cpp.
        // I really hope it doesn't count as stealing here,
        //  if I would've had to have typed this code ANYWAY
        //  after an age trying to figure it out.
        if (tidx < (48 * 8)) {
            // Autotile
            int atMap = tidx / 48;
            if (atMap == 0)
                return;
            tidx %= 48;
            boolean didDraw = false;
            if (tilesetMaps[atMap] != null) {
                if (ets == tileSize) {
                    ATDB.Autotile at = Application.autoTiles.entries[tidx];
                    if (at != null){
                        int cSize = tileSize / 2;
                        for (int sA = 0; sA < 2; sA++)
                            for (int sB = 0; sB < 2; sB++) {
                                int ti = at.corners[sA + (sB * 2)];
                                int tx = ti % 3;
                                int ty = ti / 3;
                                int sX = (sA * cSize);
                                int sY = (sB * cSize);
                                igd.blitImage((tx * tileSize) + sX, (ty * tileSize) + sY, cSize, cSize, px + sX, py + sY, tilesetMaps[atMap]);
                            }
                        didDraw = true;
                    }
                } else {
                    igd.blitImage(tileSize, 2 * tileSize, ets, ets, px, py, tilesetMaps[atMap]);
                    didDraw = true; // Close enough
                }
            } else {
                didDraw = true; // It's invisible, so it should just be considered drawn no matter what
            }
            if (!didDraw)
                UILabel.drawString(igd, px, py, ":" + tidx, false, false);
            return;
        }
        tidx -= 48 * 8;
        int tsh = 8;
        int tx = tidx % tsh;
        int ty = tidx / tsh;
        if (tilesetMaps[0] != null)
            igd.blitImage(tx * tileSize, ty * tileSize, ets, ets, px, py, tilesetMaps[0]);
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
            drawTile(tId, ox, oy, igd, 32);
        } else {
            // lower centre of tile, the reference point for characters
            ox += 16;
            oy += 32;
            String s = cName.decString();
            IGrInDriver.IImage i = GaBIEn.getImage(Application.rootPath + "Graphics/Characters/" + s + ".png", 0, 0, 0);
            int sprW = i.getWidth() / 4;
            int sprH = i.getHeight() / 4;
            // Direction 2, pattern 0 == 0, ? (safe @ cliffs, page 0)
            // Direction 2, pattern 2 == 2, ? (safe @ cliffs, page 1)
            int tx = pat;
            int ty = dir;
            if (target.getInstVarBySymbol("@blend_type").fixnumVal == 1) {
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

    public String getPanorama() {
        if (tileset != null)
            return tileset.getInstVarBySymbol("@panorama_name").decString();
        return "";
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.events;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.map.StuffRenderer;

import java.util.HashMap;

/**
 * Created on 1/27/17.
 */
public class RMEventGraphicRenderer implements IEventGraphicRenderer {

    private int patternCount = 4;
    private boolean useVXAExtensionScheme = false;
    private StuffRenderer host;

    public RMEventGraphicRenderer(StuffRenderer h) {
        host = h;
        if (StuffRenderer.versionId.equals("VXA")) {
            patternCount = 3;
            useVXAExtensionScheme = true;
        }
    }

    private HashMap<String, IGrInDriver.IImage> additiveBlending = new HashMap<String, IGrInDriver.IImage>();

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

    @Override
    public RubyIO extractEventGraphic(RubyIO evI) {
        return evI.getInstVarBySymbol("@pages").arrVal[0].getInstVarBySymbol("@graphic");
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrInDriver igd) {
        int pat = (int) target.getInstVarBySymbol("@pattern").fixnumVal;
        int coreDir = (int) target.getInstVarBySymbol("@direction").fixnumVal;
        int dir = lookupDirection(coreDir);
        if (dir == -1) {
            dir = 0;
            UILabel.drawString(igd, ox, oy, "D" + coreDir, false, FontSizes.mapDebugTextHeight);
        }
        RubyIO cName = target.getInstVarBySymbol("@character_name");
        short tId = (short) target.getInstVarBySymbol("@tile_id").fixnumVal;
        if (cName.strVal.length == 0) {
            host.tileRenderer.drawTile(0, tId, ox, oy, igd, host.tileRenderer.getTileSize());
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
                if (s.startsWith("!$")) {
                    // Character index doesn't work on these
                } else if (s.startsWith("!")) {
                    // Character index works, width is 32
                    sprW = 32;
                    sprH = i.getHeight() / 8;
                    int idx = (int) target.getInstVarBySymbol("@character_index").fixnumVal;
                    // NOTE: still unsure on how segmentation works.
                    // for now, things work out?
                    ty += (idx / 4) * 4;
                    tx += (idx % 4) * 3;
                } else {
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

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.events;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.imagefx.HueShiftImageEffect;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;

import java.util.HashMap;

/**
 * Created on 1/27/17.
 */
public class RMEventGraphicRenderer implements IEventGraphicRenderer {

    private int patternCount = 4;
    private boolean useVXAExtensionScheme = false;
    private final IImageLoader imageLoader;
    private final ITileRenderer tileRenderer;

    public RMEventGraphicRenderer(IImageLoader img, ITileRenderer tile, boolean vxa) {
        imageLoader = img;
        tileRenderer = tile;
        if (vxa) {
            patternCount = 3;
            useVXAExtensionScheme = true;
        }
    }

    public static int lookupDirection(int dir) {
        if (dir == 2)
            return 0;
        if (dir == 4)
            return 1;
        if (dir == 6)
            return 2;
        if (dir == 8)
            return 3;
        return 0;
    }

    @Override
    public int determineEventLayer(RubyIO event) {
        if (useVXAExtensionScheme)
            return (int) event.getInstVarBySymbol("@pages").arrVal[0].getInstVarBySymbol("@priority_type").fixnumVal;
        // Assume RXP. R.Q.U suggests this is 0, see "31 Hall 1" in Maintenance, specifically the pipework in front of the door at the top right.
        // But O.S suggests this is 2, or 1.
        // Current guess is that Y position means more than layer???
        // Basically: R.Q.U stuff says this MUST be 0. No matter what.
        // O.S suggests it's 2 (230: Ground 2)
        // For now I'm assuming a glitch in R.Q.U for lack of any better ideas.
        // (Also the fact that O.S. "258: Memory" doesn't show up right compared to in-game. Odds are against us.)
        return 2;
    }

    @Override
    public RubyIO extractEventGraphic(RubyIO evI) {
        return evI.getInstVarBySymbol("@pages").arrVal[0].getInstVarBySymbol("@graphic");
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd) {
        int pat = (int) target.getInstVarBySymbol("@pattern").fixnumVal;
        int coreDir = (int) target.getInstVarBySymbol("@direction").fixnumVal;
        int dir = lookupDirection(coreDir);
        if (dir == -1) {
            dir = 0;
            UILabel.drawString(igd, ox, oy, "D" + coreDir, false, FontSizes.mapDebugTextHeight);
        }
        RubyIO cName = target.getInstVarBySymbol("@character_name");
        short tId = (short) target.getInstVarBySymbol("@tile_id").fixnumVal;
        if (tId != 0) {
            tileRenderer.drawTile(0, tId, ox, oy, igd, tileRenderer.getTileSize());
        } else if (cName.strVal.length > 0) {
            // lower centre of tile, the reference point for characters
            ox += 16;
            oy += 32;
            String s = cName.decString();
            if (useVXAExtensionScheme)
                if (!s.startsWith("!"))
                    oy -= 4;
            IGrInDriver.IImage i = imageLoader.getImage("Characters/" + s, false);
            int sprW = i.getWidth() / patternCount;
            int sprH = i.getHeight() / 4;
            // Direction 2, pattern 0 == 0, ? (safe @ cliffs, page 0)
            // Direction 2, pattern 2 == 2, ? (safe @ cliffs, page 1)
            int tx = pat;
            int ty = dir;

            if (useVXAExtensionScheme) {
                sprW = i.getWidth() / 12;
                sprH = i.getHeight() / 8;
                int idx = (int) target.getInstVarBySymbol("@character_index").fixnumVal;
                if (s.startsWith("!$") || s.startsWith("$")) {
                    // Character index doesn't work on these
                    sprW = i.getWidth() / 3;
                    sprH = i.getHeight() / 4;
                } else {
                    ty += (idx / 4) * 4;
                    tx += (idx % 4) * 3;
                }
            }

            boolean doBlend = false;
            boolean doBlendType = false;
            RubyIO blendData = target.getInstVarBySymbol("@blend_type");
            if (blendData != null) {
                long m = target.getInstVarBySymbol("@blend_type").fixnumVal;
                if (m == 1)
                    doBlend = true;
                if (m == 2) {
                    doBlend = true;
                    doBlendType = true;
                }
            }
            RubyIO hueCtrl = target.getInstVarBySymbol("@character_hue");
            if (hueCtrl != null) {
                int hue = (int) (hueCtrl.fixnumVal);
                if (hue != 0)
                    i = AppMain.imageFXCache.process(i, new HueShiftImageEffect(hue));
            }
            if (doBlend) {
                igd.blendRotatedScaledImage(tx * sprW, ty * sprH, sprW, sprH, ox - (sprW / 2), oy - sprH, sprW, sprH, 0, i, doBlendType);
            } else {
                igd.blitImage(tx * sprW, ty * sprH, sprW, sprH, ox - (sprW / 2), oy - sprH, i);
            }
        }
    }

    @Override
    public int eventIdBase() {
        return 1;
    }
}

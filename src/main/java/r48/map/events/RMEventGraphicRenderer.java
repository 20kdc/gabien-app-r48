/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import gabien.FontManager;
import gabien.IGrDriver;
import gabien.IImage;
import gabienapp.Application;
import r48.AppMain;
import r48.RubyIO;
import r48.imagefx.AlphaControlImageEffect;
import r48.imagefx.HueShiftImageEffect;
import r48.imagefx.IImageEffect;
import r48.imagefx.ToneImageEffect;
import r48.map.UIMapView;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;

import java.util.LinkedList;

/**
 * Created on 1/27/17.
 */
public class RMEventGraphicRenderer implements IEventGraphicRenderer {

    private int patternCount = 4;
    private boolean useVXAExtensionScheme = false;
    public final IImageLoader imageLoader;
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
        // NOTE: This is actually used specially, by the RXPAccurateDrawLayer.
        return (event.getInstVarBySymbol("@pages").arrVal[0].getInstVarBySymbol("@always_on_top").type == 'T') ? 1 : 0;
    }

    @Override
    public RubyIO extractEventGraphic(RubyIO evI) {
        return evI.getInstVarBySymbol("@pages").arrVal[0].getInstVarBySymbol("@graphic");
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd, int sprScale) {
        int pat = (int) target.getInstVarBySymbol("@pattern").fixnumVal;
        int coreDir = (int) target.getInstVarBySymbol("@direction").fixnumVal;
        int dir = lookupDirection(coreDir);
        if (dir == -1) {
            dir = 0;
            FontManager.drawString(igd, ox, oy, "D" + coreDir, false, false, UIMapView.mapDebugTextHeight * sprScale);
        }
        RubyIO cName = target.getInstVarBySymbol("@character_name");
        short tId = (short) target.getInstVarBySymbol("@tile_id").fixnumVal;
        if (tId != 0) {
            tileRenderer.drawTile(0, tId, ox, oy, igd, sprScale);
        } else if (cName.strVal.length > 0) {
            // lower centre of tile, the reference point for characters
            ox += 16;
            oy += 32;
            String s = cName.decString();
            if (useVXAExtensionScheme)
                if (!s.startsWith("!"))
                    oy -= 4;
            IImage i = imageLoader.getImage("Characters/" + s, false);
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

            int blendType = 0;
            RubyIO blendData = target.getInstVarBySymbol("@blend_type");
            if (blendData != null)
                blendType = (int) blendData.fixnumVal;
            RubyIO hueCtrl = target.getInstVarBySymbol("@character_hue");
            LinkedList<IImageEffect> hsie = new LinkedList<IImageEffect>();
            if (hueCtrl != null) {
                int hue = (int) (hueCtrl.fixnumVal);
                if (hue != 0)
                    hsie.add(new HueShiftImageEffect(hue));
            }
            if (blendType != 0)
                hsie.add(new ToneImageEffect(1, 1, 1, 2, 2));
            i = AppMain.imageFXCache.process(i, hsie);
            flexibleSpriteDraw(tx * sprW, ty * sprH, sprW, sprH, ox - ((sprW * sprScale) / 2), oy - (sprH * sprScale), sprW * sprScale, sprH * sprScale, 0, i, blendType, igd);
        }
    }

    public static void flexibleSpriteDraw(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IImage i, int blendType, IGrDriver igd) {
        boolean doBlend = false;
        boolean doBlendType = false;
        if (blendType == 1)
            doBlend = true;
        if (blendType == 2) {
            doBlend = true;
            doBlendType = true;
        }
        if (doBlend) {
            if (!Application.allowBlending) {
                doBlend = false;
                i = AppMain.imageFXCache.process(i, new AlphaControlImageEffect(doBlendType));
            }
        }
        if (doBlend) {
            igd.blendRotatedScaledImage(srcx, srcy, srcw, srch, x, y, acw, ach, angle, i, doBlendType);
        } else {
            if ((angle % 360) == 0) {
                if (acw == srcw) {
                    if (ach == srch) {
                        igd.blitImage(srcx, srcy, srcw, srch, x, y, i);
                        return;
                    }
                }
                igd.blitScaledImage(srcx, srcy, srcw, srch, x, y, acw, ach, i);
            } else {
                igd.blitRotatedScaledImage(srcx, srcy, srcw, srch, x, y, acw, ach, angle, i);
            }
        }
    }
}

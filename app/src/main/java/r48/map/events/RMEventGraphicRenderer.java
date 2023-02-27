/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import gabien.FontManager;
import gabien.IGrDriver;
import gabien.IImage;
import r48.App;
import r48.imagefx.AlphaControlImageEffect;
import r48.imagefx.HueShiftImageEffect;
import r48.imagefx.IImageEffect;
import r48.imagefx.ToneImageEffect;
import r48.io.data.IRIO;
import r48.map.UIMapView;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;

import java.util.LinkedList;

/**
 * Created on 1/27/17.
 */
public class RMEventGraphicRenderer extends App.Svc implements IEventGraphicRenderer {

    private int patternCount = 4;
    private boolean useVXAExtensionScheme = false;
    public final IImageLoader imageLoader;
    private final ITileRenderer tileRenderer;

    public RMEventGraphicRenderer(App app, IImageLoader img, ITileRenderer tile, boolean vxa) {
        super(app);
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
    public int determineEventLayer(IRIO event) {
        if (useVXAExtensionScheme)
            return (int) event.getIVar("@pages").getAElem(0).getIVar("@priority_type").getFX();
        // NOTE: This is actually used specially, by the RXPAccurateDrawLayer.
        return (event.getIVar("@pages").getAElem(0).getIVar("@always_on_top").getType() == 'T') ? 1 : 0;
    }

    @Override
    public IRIO extractEventGraphic(IRIO evI) {
        return evI.getIVar("@pages").getAElem(0).getIVar("@graphic");
    }

    @Override
    public void drawEventGraphic(IRIO target, int ox, int oy, IGrDriver igd, int sprScale) {
        int pat = (int) target.getIVar("@pattern").getFX();
        int coreDir = (int) target.getIVar("@direction").getFX();
        int dir = lookupDirection(coreDir);
        if (dir == -1) {
            dir = 0;
            FontManager.drawString(igd, ox, oy, "D" + coreDir, false, false, UIMapView.mapDebugTextHeight * sprScale);
        }
        IRIO cName = target.getIVar("@character_name");
        String cNameS = cName.decString();
        short tId = (short) target.getIVar("@tile_id").getFX();
        if (tId != 0) {
            tileRenderer.drawTile(0, tId, ox, oy, igd, sprScale, false);
        } else if (cNameS.length() > 0) {
            // lower centre of tile, the reference point for characters
            ox += 16;
            oy += 32;
            if (useVXAExtensionScheme)
                if (!cNameS.startsWith("!"))
                    oy -= 4;
            IImage i = imageLoader.getImage("Characters/" + cNameS, false);
            int sprW = i.getWidth() / patternCount;
            int sprH = i.getHeight() / 4;
            // Direction 2, pattern 0 == 0, ? (safe @ cliffs, page 0)
            // Direction 2, pattern 2 == 2, ? (safe @ cliffs, page 1)
            int tx = pat;
            int ty = dir;

            if (useVXAExtensionScheme) {
                sprW = i.getWidth() / 12;
                sprH = i.getHeight() / 8;
                int idx = (int) target.getIVar("@character_index").getFX();
                if (cNameS.startsWith("!$") || cNameS.startsWith("$")) {
                    // Character index doesn't work on these
                    sprW = i.getWidth() / 3;
                    sprH = i.getHeight() / 4;
                } else {
                    ty += (idx / 4) * 4;
                    tx += (idx % 4) * 3;
                }
            }

            int blendType = 0;
            IRIO blendData = target.getIVar("@blend_type");
            if (blendData != null)
                blendType = (int) blendData.getFX();
            IRIO hueCtrl = target.getIVar("@character_hue");
            LinkedList<IImageEffect> hsie = new LinkedList<IImageEffect>();
            if (hueCtrl != null) {
                int hue = (int) (hueCtrl.getFX());
                if (hue != 0)
                    hsie.add(new HueShiftImageEffect(hue));
            }
            if (blendType != 0)
                hsie.add(new ToneImageEffect(1, 1, 1, 2, 2));
            i = app.ui.imageFXCache.process(i, hsie);
            flexibleSpriteDraw(app, tx * sprW, ty * sprH, sprW, sprH, ox - ((sprW * sprScale) / 2), oy - (sprH * sprScale), sprW * sprScale, sprH * sprScale, 0, i, blendType, igd);
        }
    }

    public static void flexibleSpriteDraw(App app, int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IImage i, int blendType, IGrDriver igd) {
        boolean doBlend = false;
        boolean doBlendType = false;
        if (blendType == 1)
            doBlend = true;
        if (blendType == 2) {
            doBlend = true;
            doBlendType = true;
        }
        if (doBlend) {
            if (!app.c.allowBlending) {
                doBlend = false;
                i = app.ui.imageFXCache.process(i, new AlphaControlImageEffect(doBlendType));
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

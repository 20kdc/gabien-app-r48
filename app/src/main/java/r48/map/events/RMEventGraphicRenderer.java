/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.uslx.append.MathsX;
import r48.App;
import r48.imagefx.HueShiftImageEffect;
import r48.imagefx.IImageEffect;
import r48.io.data.RORIO;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

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
    public int determineEventLayer(RORIO event) {
        if (useVXAExtensionScheme)
            return (int) event.getIVar("@pages").getAElem(0).getIVar("@priority_type").getFX();
        // NOTE: This is actually used specially, by the RXPAccurateDrawLayer.
        return (event.getIVar("@pages").getAElem(0).getIVar("@always_on_top").getType() == 'T') ? 1 : 0;
    }

    @Override
    public RORIO extractEventGraphic(RORIO evI) {
        return evI.getIVar("@pages").getAElem(0).getIVar("@graphic");
    }

    @Override
    public void drawEventGraphic(RORIO target, int ox, int oy, IGrDriver igd, int sprScale, @Nullable RORIO originalEvent) {
        RORIO opav = target.getIVar("@opacity");
        int opa = opav != null ? (int) opav.getFX() : 255;
        RORIO patv = target.getIVar("@pattern");
        RORIO dirv = target.getIVar("@direction");
        int pat = patv != null ? (int) patv.getFX() : 0;
        int coreDir = dirv != null ? (int) dirv.getFX() : 0;
        int dir = lookupDirection(coreDir);
        if (dir == -1) {
            dir = 0;
            GaBIEn.engineFonts.f8.drawLAB(igd, ox, oy, "D" + coreDir, false);
        }
        // @step_anime
        if (originalEvent != null) {
            RORIO pages = originalEvent.getIVar("@pages");
            RORIO page = pages != null ? pages.getAElem(0) : null;
            RORIO stepAnime = page != null ? page.getIVar("@step_anime") : null;
            if (stepAnime != null && stepAnime.getType() == 'T')
                pat = MathsX.seqModulo(tileRenderer.getFrame(), 4);
        }
        RORIO cName = target.getIVar("@character_name");
        String cNameS = cName == null ? "" : cName.decString();
        RORIO tidv = target.getIVar("@tile_id");
        short tId = (short) (tidv == null ? 0 : tidv.getFX());
        if (tId != 0) {
            tileRenderer.drawTile(0, tId, ox, oy, igd, sprScale);
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
                RORIO cIdxV = target.getIVar("@character_index");
                int idx = cIdxV != null ? (int) cIdxV.getFX() : 0;
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
            RORIO blendData = target.getIVar("@blend_type");
            if (blendData != null)
                blendType = (int) blendData.getFX();
            RORIO hueCtrl = target.getIVar("@character_hue");
            LinkedList<IImageEffect> hsie = new LinkedList<IImageEffect>();
            if (hueCtrl != null) {
                int hue = (int) (hueCtrl.getFX());
                if (hue != 0)
                    hsie.add(new HueShiftImageEffect(hue));
            }
            i = app.ui.imageFXCache.process(i, hsie);
            int blendMode = IGrDriver.BLEND_NORMAL;
            if (blendType == 1)
                blendMode = IGrDriver.BLEND_ADD;
            if (blendType == 2)
                blendMode = IGrDriver.BLEND_SUB;
            if (opa == 255) {
                igd.blitScaledImage(tx * sprW, ty * sprH, sprW, sprH, ox - ((sprW * sprScale) / 2), oy - (sprH * sprScale), sprW * sprScale, sprH * sprScale, i, blendMode, 0);
            } else {
                float a = opa / 255.0f;
                igd.drawScaledColoured(tx * sprW, ty * sprH, sprW, sprH, ox - ((sprW * sprScale) / 2), oy - (sprH * sprScale), sprW * sprScale, sprH * sprScale, i, blendMode, 0, a, a, a, a);
            }
        }
    }
}

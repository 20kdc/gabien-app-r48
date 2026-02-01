/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.ui.UIElement;
import r48.map2d.MapViewDrawContext;
import r48.map2d.layers.MapViewDrawLayer;
import r48.tr.pages.TrRoot;

/**
 * Used for drawing panoramas.
 * Created on 08/06/17.
 */
public class PanoramaMapViewDrawLayer extends MapViewDrawLayer {
    private final IImage im;
    private boolean loopX, loopY;
    private int autoLoopX, autoLoopY, mapTilesW, mapTilesH, scrW, scrH, panoScale;
    private int parallaxRatioA, parallaxRatioB;

    public PanoramaMapViewDrawLayer(TrRoot t, IImage pano, boolean lx, boolean ly, int alx, int aly, int mtx, int mty, int scw, int sch, int pScale) {
        super(t.m.l_panorama);
        im = pano;
        loopX = lx;
        loopY = ly;
        autoLoopX = alx;
        autoLoopY = aly;
        mapTilesW = mtx;
        mapTilesH = mty;
        scrW = scw;
        scrH = sch;
        panoScale = pScale;
        parallaxRatioA = 2;
        parallaxRatioB = 1;
    }

    public PanoramaMapViewDrawLayer(TrRoot t, IImage pano, boolean lx, boolean ly, int alx, int aly, int mtx, int mty, int scw, int sch, int pScale, int pra, int prb) {
        super(t.m.l_panorama);
        im = pano;
        loopX = lx;
        loopY = ly;
        autoLoopX = alx;
        autoLoopY = aly;
        mapTilesW = mtx;
        mapTilesH = mty;
        scrW = scw;
        scrH = sch;
        panoScale = pScale;
        parallaxRatioA = pra;
        parallaxRatioB = prb;
    }

    public void draw(MapViewDrawContext mvdc) {
        // Panorama Enable
        if (im != null) {
            int effectiveImWidth = im.getWidth() * panoScale;
            int effectiveImHeight = im.getHeight() * panoScale;

            // Need to tile the area with the image.
            // I give up, this is what I've got now.
            // It works better this way than the other way under some cases.

            int eCamX = 0;
            int eCamY = 0;

            // ... later:
            // The basis of parallax appears to be "whatever the camera was set to beforehand"
            // For accurate results despite this "varying basis",
            //  emulation needs to get the difference between R48's camera and an idealized 20x15 camera @ the top-left.
            // For animated parallax, it takes 40 seconds for a value of 1 to travel 160 pixels (tested on RPG_RT)
            // This boils down to precisely 4 pixels per second per speed value.
            int centreX = scrW / 2;
            int centreY = scrH / 2;
            int cxc = mvdc.cam.x + (mvdc.igd.getWidth() / 2);
            int cyc = mvdc.cam.y + (mvdc.igd.getHeight() / 2);

            // Yume Nikki's Incredibly Long Climb Up A Very Boring Staircase (map ID 64, just above BLOCK 5),
            //  as a 'true' case, and the Nexus, as a 'false' case
            // At this point I don't know QUITE how the maths are working in all the cases they do.
            // In practice that means I probably found the right formula and thus I don't need special cases
            // As a good test for *looping*, unsure, but 110 of the 85 additionals...
            if (loopX) {
                eCamX -= (((cxc - centreX) * parallaxRatioB) / parallaxRatioA) + ((int) (autoLoopX * 4 * GaBIEn.getTime()));
            } else {
                if (scrW != -1) {
                    // Bind to the centre of the map, get the 'extra'
                    int mapW = mvdc.tileSize * mapTilesW;
                    int mapM = mapW - scrW;
                    int mapCM = cxc - (scrW / 2);
                    int exT = effectiveImWidth - scrW;
                    eCamX = -(mvdc.igd.getWidth() / 2);
                    eCamX += effectiveImWidth / 2;
                    if (mapM > 0) {
                        long extra = exT;
                        // arcane maths
                        extra *= mapCM;
                        extra /= mapM;
                        eCamX += extra - (exT / 2);
                    }
                }
            }
            if (loopY) {
                eCamY -= (((cyc - centreY) * parallaxRatioB) / parallaxRatioA) + ((int) (autoLoopY * 4 * GaBIEn.getTime()));
            } else {
                if (scrH != -1) {
                    int mapH = mvdc.tileSize * mapTilesH;
                    int mapM = mapH - scrH;
                    int mapCM = cyc - (scrH / 2);
                    int exT = effectiveImHeight - scrH;
                    eCamY = -(mvdc.igd.getHeight() / 2);
                    eCamY += effectiveImHeight / 2;
                    if (mapM > 0) {
                        long extra = exT;
                        // arcane maths
                        extra *= mapCM;
                        extra /= mapM;
                        eCamY += extra - (exT / 2);
                    }
                }
            }

            int camOTX = UIElement.sensibleCellDiv(eCamX + mvdc.cam.x, effectiveImWidth);
            int camOTY = UIElement.sensibleCellDiv(eCamY + mvdc.cam.y, effectiveImHeight);
            int camOTeX = UIElement.sensibleCellDiv(eCamX + mvdc.cam.x + mvdc.cam.width, effectiveImWidth) + 1;
            int camOTeY = UIElement.sensibleCellDiv(eCamY + mvdc.cam.y + mvdc.cam.height, effectiveImHeight) + 1;

            // If *nothing's* looping, it's probably 'bound to the map' (YumeNikki Nexus, OneShot Maize).
            // Failing anything else this helps avoid confusion: "where was the actual map again?"
            // (PARTICULARLY helps with YumeNikki igloos, but really, the whole thing *just works well*)
            if (!(loopX || loopY)) {
                camOTX = 0;
                camOTeX = 0;
                camOTY = 0;
                camOTeY = 0;
                eCamX = 0;
                eCamY = 0;
            }

            if (panoScale != 1) {
                for (int i = camOTX; i <= camOTeX; i++)
                    for (int j = camOTY; j <= camOTeY; j++)
                        mvdc.igd.blitScaledImage(0, 0, im.getWidth(), im.getHeight(), (i * effectiveImWidth) - eCamX, (j * effectiveImHeight) - eCamY, effectiveImWidth, effectiveImHeight, im);
            } else {
                int totalW = (camOTeX - camOTX) + 1;
                int totalH = (camOTeY - camOTY) + 1;
                mvdc.igd.blitTiledImage((camOTX * effectiveImWidth) - eCamX, (camOTY * effectiveImHeight) - eCamY, totalW * effectiveImWidth, totalH * effectiveImHeight, im);
            }
        }
    }
}

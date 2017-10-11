/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.drawlayers;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.UIElement;
import r48.dbs.TXDB;
import r48.map.IMapViewCallbacks;

/**
 * Used for drawing panoramas.
 * Created on 08/06/17.
 */
public class PanoramaMapViewDrawLayer implements IMapViewDrawLayer {
    private final IImage im;
    private boolean loopX, loopY;
    private int autoLoopX, autoLoopY, mapTilesW, mapTilesH, scrW, scrH, panoScale;

    public PanoramaMapViewDrawLayer(IImage pano, boolean lx, boolean ly, int alx, int aly, int mtx, int mty, int scw, int sch, int pScale) {
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
    }

    @Override
    public String getName() {
        return TXDB.get("Panorama");
    }

    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        // Panorama Enable
        if (im != null) {
            int effectiveImWidth = im.getWidth() * panoScale;
            int effectiveImHeight = im.getHeight() * panoScale;
            // Need to tile the area with the image.
            // I give up, this is what I've got now.
            // It works better this way than the other way under some cases.

            int eCamX = camX;
            int eCamY = camY;

            // ... later:
            // The basis of parallax appears to be "whatever the camera was set to beforehand"
            // For accurate results despite this "varying basis",
            //  emulation needs to get the difference between R48's camera and an idealized 20x15 camera @ the top-left.
            // For animated parallax, it takes 40 seconds for a value of 1 to travel 160 pixels (tested on RPG_RT)
            // This boils down to precisely 4 pixels per second per speed value.
            int centreX = scrW / 2;
            int centreY = scrH / 2;
            int cxc = camX + (igd.getWidth() / 2);
            int cyc = camY + (igd.getHeight() / 2);

            // Yume Nikki's Incredibly Long Climb Up A Very Boring Staircase (map ID 64, just above BLOCK 5),
            //  as a 'true' case, and the Nexus, as a 'false' case
            // At this point I don't know QUITE how the maths are working in all the cases they do.
            // In practice that means I probably found the right formula and thus I don't need special cases
            // As a good test for *looping*, unsure, but 110 of the 85 additionals...
            if (loopX) {
                eCamX -= ((cxc - centreX) / 2) + ((int) (autoLoopX * 4 * GaBIEn.getTime()));
            } else {
                if (scrW != -1) {
                    // Bind to the centre of the map, get the 'extra'
                    int mapW = eTileSize * mapTilesW;
                    int mapM = mapW - scrW;
                    int mapCM = cxc - (scrW / 2);
                    int exT = effectiveImWidth - scrW;
                    eCamX = -(igd.getWidth() / 2);
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
                eCamY -= ((cyc - centreY) / 2) + ((int) (autoLoopY * 4 * GaBIEn.getTime()));
            } else {
                if (scrH != -1) {
                    int mapH = eTileSize * mapTilesH;
                    int mapM = mapH - scrH;
                    int mapCM = cyc - (scrH / 2);
                    int exT = effectiveImHeight - scrH;
                    eCamY = -(igd.getHeight() / 2);
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

            int camOTX = UIElement.sensibleCellDiv(eCamX, im.getWidth());
            int camOTY = UIElement.sensibleCellDiv(eCamY, im.getHeight());
            int camOTeX = UIElement.sensibleCellDiv(eCamX + igd.getWidth(), im.getWidth()) + 1;
            int camOTeY = UIElement.sensibleCellDiv(eCamY + igd.getHeight(), im.getHeight()) + 1;

            // If *nothing's* looping, it's probably 'bound to the map' (YumeNikki Nexus, OneShot Maize).
            // Failing anything else this helps avoid confusion: "where was the actual map again?"
            // (PARTICULARLY helps with YumeNikki igloos, but really, the whole thing *just works well*)
            if (!(loopX || loopY)) {
                camOTX = 0;
                camOTeX = 0;
                camOTY = 0;
                camOTeY = 0;
                eCamX = camX;
                eCamY = camY;
            }

            for (int i = camOTX; i <= camOTeX; i++)
                for (int j = camOTY; j <= camOTeY; j++)
                    igd.blitScaledImage(0, 0, im.getWidth(), im.getHeight(), (i * effectiveImWidth) - eCamX, (j * effectiveImHeight) - eCamY, effectiveImWidth, effectiveImHeight, im);
        }
    }
}

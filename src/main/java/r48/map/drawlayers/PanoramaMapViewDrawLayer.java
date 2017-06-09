/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.ui.UIElement;
import r48.map.IMapViewCallbacks;

/**
 * Used for drawing panoramas.
 * Created on 08/06/17.
 */
public class PanoramaMapViewDrawLayer implements IMapViewDrawLayer {
    private final IGrInDriver.IImage im;
    public PanoramaMapViewDrawLayer(IGrInDriver.IImage pano) {
        im = pano;
    }

    @Override
    public String getName() {
        return "Panorama";
    }

    public void draw(int camX, int camY, int camTX, int camTY, int camTR, int camTB, int mouseXT, int mouseYT, int eTileSize, int currentLayer, IMapViewCallbacks callbacks, boolean debug, IGrDriver igd) {
        // Panorama Enable
        if (im != null) {
            // Need to tile the area with the image.
            // I give up, this is what I've got now.
            // It works better this way than the other way under some cases.
            int eCamX = camX;
            int eCamY = camY;
            //eCamX -= im.getWidth() / 4;
            //eCamY -= im.getHeight() / 4;
            int camOTX = UIElement.sensibleCellDiv(eCamX, im.getWidth());
            int camOTY = UIElement.sensibleCellDiv(eCamY, im.getHeight());
            int camOTeX = UIElement.sensibleCellDiv(eCamX + igd.getWidth(), im.getWidth()) + 1;
            int camOTeY = UIElement.sensibleCellDiv(eCamY + igd.getHeight(), im.getHeight()) + 1;
            for (int i = camOTX; i <= camOTeX; i++)
                for (int j = camOTY; j <= camOTeY; j++)
                    igd.blitImage(0, 0, im.getWidth(), im.getHeight(), (i * im.getWidth()) - eCamX, (j * im.getHeight()) - eCamY, im);
        }
    }
}

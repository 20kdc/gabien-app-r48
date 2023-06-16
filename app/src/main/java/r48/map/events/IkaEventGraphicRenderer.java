/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.FontManager;
import r48.App;
import r48.io.data.IRIO;
import r48.map.imaging.IImageLoader;

/**
 * Ikachan's event graphic renderer
 * Created on 1/27/17.
 */
public class IkaEventGraphicRenderer extends App.Svc implements IEventGraphicRenderer {

    private final IImageLoader imageLoader;

    public IkaEventGraphicRenderer(App app, IImageLoader il) {
        super(app);
        imageLoader = il;
    }

    @Override
    public int determineEventLayer(IRIO event) {
        return 0;
    }


    @Override
    public IRIO extractEventGraphic(IRIO event) {
        return event;
    }

    @Override
    public void drawEventGraphic(IRIO target, int ox, int oy, IGrDriver igd, int sprScale) {
        String[] graphics = new String[] {"Hari", "Isogin", "Kani", "Sleep", "Chibi", "Hoshi", "Dum", "Carry", "Juel", "Ufo"};
        int dsX = 16;
        int dsY = 16;
        int dfX = 0;
        int dfY = 0;
        int doX = 0;
        int doY = 0;
        int type = (int) target.getIVar("@type").getFX();

        // Deal with specific cases
        if (type == 7) {
            dsX = 30;
            dsY = 20;
            doX = -7;
            doY = -2;
        } else if ((type == 6) || (type == 1)) {
            dfX = (int) target.getIVar("@tOX").getFX();
        } else if (type == 9) {
            dsX = 32;
            dsY = 32;
            doX = 0;
            doY = -16;
        } else if (type == 3) {
            dsX = 20;
            dsY = 20;
            doX = -2;
            doY = -4;
        } else if (type == 4) {
            dsX = 8;
            dsY = 8;
        }

        boolean fail = false;
        if (type < 0)
            fail = true;
        if (type >= graphics.length)
            fail = true;
        if (!fail) {
            String r = graphics[type];
            IImage im = imageLoader.getImage(r, false);
            RMEventGraphicRenderer.flexibleSpriteDraw(app, dfX * dsX, dfY * dsY, dsX, dsY, ox + (doX * sprScale), oy + (doY * sprScale), dsX * sprScale, dsY * sprScale, 0, im, 0, igd);
            fail = false;
        }
        if (fail)
            FontManager.drawString(igd, ox, oy, "EV" + type, true, false, 8);
    }
}

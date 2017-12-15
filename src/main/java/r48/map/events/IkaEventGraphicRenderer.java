/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import gabien.IGrDriver;
import gabien.IImage;
import r48.RubyIO;
import r48.map.imaging.IImageLoader;

/**
 * Ikachan's event graphic renderer
 * Created on 1/27/17.
 */
public class IkaEventGraphicRenderer implements IEventGraphicRenderer {

    private final IImageLoader imageLoader;

    public IkaEventGraphicRenderer(IImageLoader il) {
        imageLoader = il;
    }

    @Override
    public int determineEventLayer(RubyIO event) {
        return 0;
    }


    @Override
    public RubyIO extractEventGraphic(RubyIO event) {
        return event;
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd, int sprScale) {
        String[] graphics = new String[] {"Hari", "Isogin", "Kani", "Sleep", "Chibi", "Hoshi", "Dum", "Carry", "Juel", "Ufo"};
        int dsX = 16;
        int dsY = 16;
        int dfX = 0;
        int dfY = 0;
        int doX = 0;
        int doY = 0;
        int type = (int) target.getInstVarBySymbol("@type").fixnumVal;

        // Deal with specific cases
        if (type == 7) {
            dsX = 30;
            dsY = 20;
            doX = -7;
            doY = -2;
        } else if ((type == 6) || (type == 1)) {
            dfX = (int) target.getInstVarBySymbol("@tOX").fixnumVal;
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
            RMEventGraphicRenderer.flexibleSpriteDraw(dfX * dsX, dfY * dsY, dsX, dsY, ox + (doX * sprScale), oy + (doY * sprScale), dsX * sprScale, dsY * sprScale, 0, im, 0, igd);
            fail = false;
        }
        if (fail)
            igd.drawText(ox, oy, 255, 255, 255, 8, "EV" + type);
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.events;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.RubyIO;

/**
 * Ikachan's event graphic renderer
 * Created on 1/27/17.
 */
public class IkaEventGraphicRenderer implements IEventGraphicRenderer {
    @Override
    public int determineEventLayer(RubyIO event) {
        return 0;
    }

    @Override
    public RubyIO extractEventGraphic(RubyIO event) {
        return event;
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrInDriver igd) {
        String[] graphics = new String[] {"Pbm/Hari.pbm", "Pbm/Isogin.pbm", "Pbm/Kani.pbm", "Pbm/Sleep.pbm", "Pbm/Chibi.pbm", "Pbm/Hoshi.pbm", "Pbm/Dum.pbm", "Pbm/Carry.pbm", "Pbm/Juel.pbm", "Pbm/Ufo.pbm"};
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
            IGrInDriver.IImage im = GaBIEn.getImage(r, 0, 0, 0);
            igd.blitBCKImage(dfX * dsX, dfY * dsY, dsX, dsY, ox + doX, oy + doY, im);
            fail = false;
        }
        if (fail)
            igd.drawText(ox, oy, 255, 255, 255, 8, "EV" + type);
    }
}

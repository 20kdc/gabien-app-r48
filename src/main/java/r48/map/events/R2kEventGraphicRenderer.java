/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.events;

import gabien.IGrInDriver;
import r48.RubyIO;
import r48.map.imaging.IImageLoader;

/**
 * An interlude.
 * Created on 31/05/17.
 */
public class R2kEventGraphicRenderer implements IEventGraphicRenderer {
    public final IImageLoader imageLoader;

    public R2kEventGraphicRenderer(IImageLoader imageLoad) {
        imageLoader = imageLoad;
    }

    @Override
    public int determineEventLayer(RubyIO event) {
        if (event.getInstVarBySymbol("@pages").arrVal.length <= 1)
            return 0;
        int ld = (int) event.getInstVarBySymbol("@pages").arrVal[1].getInstVarBySymbol("@layer").fixnumVal;
        if (ld == 0)
            return 0;
        if (ld == 1)
            return 1;
        // something else???
        if (ld == 2)
            return 2;
        return 1;
    }

    @Override
    public int extraEventLayers() {
        return 1;
    }

    @Override
    public RubyIO extractEventGraphic(RubyIO event) {
        // 'Zero Page' gets in the way here.
        if (event.getInstVarBySymbol("@pages").arrVal.length <= 1)
            return null;
        return event.getInstVarBySymbol("@pages").arrVal[1];
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrInDriver igd) {
        String cName = target.getInstVarBySymbol("@character_name").decString();
        if (!cName.equals("")) {
            IGrInDriver.IImage i = imageLoader.getImage("CharSet/" + cName, 0, 0, 0);
            int sx = i.getWidth() / 12;
            int sy = i.getHeight() / 8;
            int idx = ((int) target.getInstVarBySymbol("@character_index").fixnumVal);
            // Direction is apparently in a 0123 format???
            int dir = ((int) target.getInstVarBySymbol("@character_direction").fixnumVal);
            int pat = ((int) target.getInstVarBySymbol("@character_pattern").fixnumVal);
            int px = ((idx % 4) * 3) + pat;
            int py = ((idx / 4) * 4) + dir;
            // The vertical offset is either 12 or 16?
            // 16 causes papers to be weirdly offset, 12 causes lift doors to be out of place
            igd.blitImage(sx * px, sy * py, sx, sy, (ox + 8) - (sx / 2), (oy - sy) + 16, i);
        }
    }
}

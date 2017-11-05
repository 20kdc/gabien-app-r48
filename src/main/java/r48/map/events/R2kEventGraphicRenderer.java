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
import r48.map.tiles.ITileRenderer;

/**
 * An interlude.
 * Created on 31/05/17.
 */
public class R2kEventGraphicRenderer implements IEventGraphicRenderer {
    public final IImageLoader imageLoader;
    public final ITileRenderer tileRenderer;

    public R2kEventGraphicRenderer(IImageLoader imageLoad, ITileRenderer tr) {
        tileRenderer = tr;
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
    public RubyIO extractEventGraphic(RubyIO event) {
        // 'Zero Page' gets in the way here.
        if (event.getInstVarBySymbol("@pages").arrVal.length <= 1)
            return null;
        return event.getInstVarBySymbol("@pages").arrVal[1];
    }

    @Override
    public void drawEventGraphic(RubyIO target, int ox, int oy, IGrDriver igd, int sprScale) {
        String cName = target.getInstVarBySymbol("@character_name").decString();
        if (!cName.equals("")) {
            IImage i = imageLoader.getImage("CharSet/" + cName, false);
            int sx = i.getWidth() / 12;
            int sy = i.getHeight() / 8;
            if (target.getInstVarBySymbol("@character_name").strVal[0] != '$') {
                sx = 24;
                sy = 32;
            }
            int idx = ((int) target.getInstVarBySymbol("@character_index").fixnumVal);
            // Direction is apparently in a 0123 format???
            int dir = 2;
            RubyIO dirVal = target.getInstVarBySymbol("@character_direction");
            if (dirVal != null)
                dir = ((int) dirVal.fixnumVal);
            int pat = 1;
            RubyIO patVal = target.getInstVarBySymbol("@character_pattern");
            if (patVal != null)
                pat = ((int) patVal.fixnumVal);
            int px = ((idx % 4) * 3) + pat;
            int py = ((idx / 4) * 4) + dir;
            // The vertical offset is either 12 or 16?
            // 16 causes papers to be weirdly offset, 12 causes lift doors to be out of place
            int blendType = 0;
            RMEventGraphicRenderer.flexibleSpriteDraw(sx * px, sy * py, sx, sy, (ox + (8 * sprScale)) - ((sx * sprScale) / 2), (oy - (sy * sprScale)) + (16 * sprScale), sx * sprScale, sy * sprScale, 0, i, blendType, igd);
        } else {
            // ok, so in this case it's a tile. In the index field.
            tileRenderer.drawTile(0, (short) (target.getInstVarBySymbol("@character_index").fixnumVal + 10000), ox, oy, igd, sprScale);
        }
    }

    @Override
    public int eventIdBase() {
        return 1;
    }
}

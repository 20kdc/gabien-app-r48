/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.events;

import org.eclipse.jdt.annotation.Nullable;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import r48.App;
import r48.io.data.RORIO;
import r48.map.imaging.IImageLoader;
import r48.map.tiles.ITileRenderer;

/**
 * An interlude.
 * Created on 31/05/17.
 */
public class R2kEventGraphicRenderer extends App.Svc implements IEventGraphicRenderer {
    public final IImageLoader imageLoader;
    public final ITileRenderer tileRenderer;
    // Idea is, if 2x is needed, this is set in the constructor.
    // DO NOT use this for all calculations. This is meant for calculations on the event file,
    //  and it's relation is meant for calculations on the tiles.
    public final int localTileSize = 16;
    private final int remoteTileSize;

    public R2kEventGraphicRenderer(App app, IImageLoader imageLoad, ITileRenderer tr) {
        super(app);
        tileRenderer = tr;
        imageLoader = imageLoad;
        remoteTileSize = tileRenderer.tileSize;
    }

    @Override
    public int determineEventLayer(RORIO event) {
        RORIO eventCore = extractEventGraphic(event);
        if (eventCore == null)
            return -1;
        RORIO active = eventCore.getIVar("@active");
        if (active != null)
            if (active.getType() == 'F')
                return -1;
        int ld = (int) eventCore.getIVar("@layer").getFX();
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
    public RORIO extractEventGraphic(RORIO event) {
        if (event.getSymbol() == null)
            return null;
        // Savefile event classes
        if (event.getSymbol().equals("RPG::SavePartyLocation"))
            return event;
        if (event.getSymbol().equals("RPG::SaveVehicleLocation"))
            return event;
        if (event.getSymbol().equals("RPG::SaveMapEvent"))
            return event;
        // 'Zero Page' gets in the way here.
        if (event.getIVar("@pages").getALen() <= 1)
            return null;
        return event.getIVar("@pages").getAElem(1);
    }

    @Override
    public void drawEventGraphic(RORIO target, int ox, int oy, IGrDriver igd, int sprScale, @Nullable RORIO originalEvent) {
        String cName = target.getIVar("@character_name").decString();
        if (!cName.equals("")) {
            IImage i = imageLoader.getImage("CharSet/" + cName, false);
            int sx = i.getWidth() / 12;
            int sy = i.getHeight() / 8;
            int rsx = scaleLocalToRemote(sx);
            int rsy = scaleLocalToRemote(sy);
            if (cName.charAt(0) != '$') {
                // @16 : 24x32
                sx = (localTileSize * 3) / 2;
                sy = localTileSize * 2;
                rsx = (remoteTileSize * 3) / 2;
                rsy = remoteTileSize * 2;
            }
            int idx = ((int) target.getIVar("@character_index").getFX());
            // Direction is apparently in a 0123 format???
            int dir = 2;
            RORIO dirVal = target.getIVar("@character_direction");
            if (dirVal != null)
                dir = ((int) dirVal.getFX());
            int pat = 1;
            RORIO patVal = target.getIVar("@character_pattern");
            if (patVal != null)
                pat = ((int) patVal.getFX());
            int px = ((idx % 4) * 3) + pat;
            int py = ((idx / 4) * 4) + dir;
            // The vertical offset is either 12 or 16?
            // 16 causes papers to be weirdly offset, 12 causes lift doors to be out of place
            igd.blitScaledImage(sx * px, sy * py, sx, sy, ox + (((remoteTileSize * sprScale) - (sx * sprScale)) / 2), (oy - (rsy * sprScale)) + (remoteTileSize * sprScale), rsx * sprScale, rsy * sprScale, i);
        } else {
            // ok, so in this case it's a tile. In the index field.
            tileRenderer.drawTile(0, (short) (target.getIVar("@character_index").getFX() + 10000), ox, oy, igd, sprScale);
        }
    }

    private int scaleLocalToRemote(int i) {
        return (i * remoteTileSize) / localTileSize;
    }
}

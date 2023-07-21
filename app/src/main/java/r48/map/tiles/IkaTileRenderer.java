/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.tiles;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.atlas.AtlasSet;
import gabien.atlas.BinaryTreeAtlasStrategy;
import gabien.atlas.ImageAtlasDrawable;
import gabien.atlas.SimpleAtlasBuilder;
import gabien.render.IGrDriver;
import gabien.render.ITexRegion;
import gabien.uslx.append.DepsLocker;
import r48.App;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * Created on 1/27/17.
 */
public class IkaTileRenderer extends ITileRenderer {
    private final IImageLoader imageLoader;
    private static final String[] blockTypes = {
        null, null, "filt", null,
        "Item", null, "Dir", null,
        "Block", null, "Dmg", null,
        null, null, "Snack", null
    };
    private DepsLocker tilesDeps = new DepsLocker();
    private final ITexRegion[] tileSheets = new ITexRegion[16];
    private AtlasSet<Object> lastAtlasSet;

    public IkaTileRenderer(App app, IImageLoader il) {
        super(app, 16, 16);
        imageLoader = il;
        checkReload();
    }

    public void checkReload() {
        Object[] deps = new Object[16];
        for (int i = 0; i < blockTypes.length; i++)
            if (blockTypes[i] != null)
                deps[i] = imageLoader.getImage("Prt" + blockTypes[i], false);
        if (tilesDeps.shouldUpdate(deps)) {
            SimpleAtlasBuilder<Object> sab = new SimpleAtlasBuilder<>(256, 256, BinaryTreeAtlasStrategy.INSTANCE);
            for (int i = 0; i < blockTypes.length; i++)
                if (deps[i] != null)
                    sab.add(i, new ImageAtlasDrawable((ITexRegion) deps[i]));
            lastAtlasSet = sab.compile();
            for (int i = 0; i < blockTypes.length; i++)
                tileSheets[i] = lastAtlasSet.contents.get(i);
        }
    }

    @Override
    public void drawTile(int layer, short tidx, int px, int py, IGrDriver igd, boolean editor) {
        int plane = (tidx & 0xFFF0) >> 4;
        int block = tidx & 0xF;
        if (plane < 0)
            return;
        if (plane > 15)
            return;
        ITexRegion i = tileSheets[plane];
        if (i == null)
            return;
        if (plane != 6) {
            igd.blitImage(tileSize * block, 0, tileSize, tileSize, px, py, i);
        } else {
            // fun fact, this was probably the most loved feature of IkachanMapEdit.
            int frame = getFrame();
            if (block == 0)
                igd.blitImage(frame, 0, tileSize, tileSize, px, py, i);
            if (block == 1)
                igd.blitImage(tileSize - frame, 0, tileSize, tileSize, px, py, i);
            if (block == 2)
                igd.blitImage(0, frame, tileSize, tileSize, px, py, i);
            if (block == 3)
                igd.blitImage(0, tileSize - frame, tileSize, tileSize, px, py, i);
        }
    }

    @Override
    public TileEditingTab[] getEditConfig(int layerIdx) {
        return new TileEditingTab[] {
                new TileEditingTab(T.m.l257, false, TileEditingTab.range(0, 256))
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        // simple enough: Ikachan doesn't have ATs.
        return new AutoTileTypeField[0];
    }

    @Override
    public int getFrame() {
        double time = GaBIEn.getTime();
        return (int) ((time - Math.floor(time)) * 64) % tileSize;
    }

    @Override
    @Nullable
    public AtlasSet<?> getAtlasSet() {
        return lastAtlasSet;
    }
}

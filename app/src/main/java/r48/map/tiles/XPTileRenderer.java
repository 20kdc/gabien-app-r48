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
import gabien.render.IImage;
import gabien.render.ITexRegion;
import gabien.uslx.append.DepsLocker;
import r48.App;
import r48.RubyTableR;
import r48.io.data.IRIO;
import r48.map.imaging.IImageLoader;
import r48.map.tileedit.AutoTileTypeField;
import r48.map.tileedit.TileEditingTab;

/**
 * Created on 1/27/17.
 */
public class XPTileRenderer extends TSOAwareTileRenderer {
    public final IImageLoader imageLoader;

    public RubyTableR priorities;

    // Note that this only covers rendering resources, not priorities
    private final DepsLocker depsLocker = new DepsLocker();
    private ITexRegion commonTiles;
    private int commonTilesHeight;
    private ITexRegion[][][] atFields;
    private AtlasSet atlasSet;

    public XPTileRenderer(App app, IImageLoader imageLoader) {
        super(app, 32, 8);
        this.imageLoader = imageLoader;
    }

    @Override
    public void checkReloadTSO(@Nullable IRIO tileset) {
        if (tileset != null) {
            // Always reload priorities immediately.
            priorities = new RubyTableR(tileset.getIVar("@priorities").getBuffer());
            IRIO tn = tileset.getIVar("@tileset_name");
            IImage[] tilesetMaps = new IImage[8];
            if (tn != null) {
                // XP
                String expectedTS = tn.decString();
                if (expectedTS.length() != 0)
                    tilesetMaps[0] = imageLoader.getImage("Tilesets/" + expectedTS, false);
            }
            IRIO amNames = tileset.getIVar("@autotile_names");
            if (amNames != null) {
                for (int i = 0; i < 7; i++) {
                    IRIO rio = amNames.getAElem(i);
                    String expectedAT = rio.decString();
                    if (expectedAT.length() > 0)
                        tilesetMaps[i + 1] = imageLoader.getImage("Autotiles/" + expectedAT, false);
                }
            }
            doReloadImg(tilesetMaps); 
        } else {
            // Clear everything.
            priorities = null;
            depsLocker.shouldUpdate();
            commonTiles = null;
            atFields = null;
            atlasSet = null;
        }
        // If the tileset's null, then just give up.
        // The tileset being/not being null is an implementation detail anyway.
    }

    private void doReloadImg(IImage[] tilesetMaps) {
        // and that's why it's a deep compare
        if (!depsLocker.shouldUpdate((Object) tilesetMaps))
            return;
        SimpleAtlasBuilder sab = new SimpleAtlasBuilder(1024, 1024, BinaryTreeAtlasStrategy.INSTANCE);
        if (tilesetMaps[0] != null) {
            commonTilesHeight = tilesetMaps[0].height;
            sab.add((res) -> commonTiles = res, new ImageAtlasDrawable(tilesetMaps[0]));
        }
        atFields = new ITexRegion[7][][];
        for (int i = 0; i < 7; i++) {
            IImage atf = tilesetMaps[i + 1];
            if (atf == null)
                continue;
            int count = atf.getWidth() / 96;
            if (count < 1)
                count = 1;
            ITexRegion[][] ts = new ITexRegion[count][];
            for (int j = 0; j < ts.length; j++) {
                ITexRegion atfSrc = atf.subRegion(j * 96, 0, 96, 128);
                ITexRegion[] compiledATF = ATFieldAtlasDrawable.addToSimpleAtlasBuilder(tileSize, app.autoTiles[0], sab, atfSrc, false);
                ts[j] = compiledATF;
            }
            atFields[i] = ts;
        }
        atlasSet = sab.compile();
    }

    @Override
    public void drawTile(int layer, int tidx, int px, int py, IGrDriver igd) {
        /*
         * First 48 tiles: Nothing ("AT 0")
         * 7 sets of 48 tiles afterwards: Each of the 7 AT fields
         * After this is the common tiles set
         */
        if (tidx < (48 * 8)) {
            // Autotile
            int atMap = tidx / 48;
            if (atMap == 0)
                return;
            atMap--;
            tidx %= 48;
            ITexRegion[][] animatedATF = atFields[atMap];
            if (animatedATF != null) {
                int animControl = 0;
                int animSets = animatedATF.length;
                if (animSets > 0)
                    animControl = getFrame() % animSets;
                ITexRegion[] atfFrame = animatedATF[animControl];
                ITexRegion atfTile = atfFrame[tidx];
                if (atfTile != null)
                    igd.blitImage(px, py, atfTile);
            }
            return;
        }
        tidx -= 48 * 8;
        int tsh = 8;
        int tx = tidx % tsh;
        int ty = tidx / tsh;
        if (commonTiles != null) {
            // Do not render out of range tiles
            if (ty * tileSize >= commonTilesHeight)
                return;
            igd.blitImage(tx * tileSize, ty * tileSize, tileSize, tileSize, px, py, commonTiles);
        }
    }

    @Override
    public TileEditingTab[] getEditConfig(int layerIdx) {
        int tileCount = 48;
        ITexRegion tm0 = commonTiles;
        if (tm0 != null) {
            int rh = (int) tm0.getRegionHeight();
            tileCount = ((rh / 32) * 8);
        }
        return new TileEditingTab[] {
                new TileEditingTab(app, "AUTO", false, new int[] {
                        0,
                        48,
                        48 * 2,
                        48 * 3,
                        48 * 4,
                        48 * 5,
                        48 * 6,
                        48 * 7
                }, indicateATs()),
                new TileEditingTab("TMAP", false, true, TileEditingTab.range(48 * 8, tileCount)),
                new TileEditingTab("AT-M", false, false, TileEditingTab.range(48, 48 * 7)),
        };
    }

    @Override
    public AutoTileTypeField[] indicateATs() {
        return new AutoTileTypeField[] {
                new AutoTileTypeField(0, 48, 0, 47),
                new AutoTileTypeField(48, 48, 0, 47),
                new AutoTileTypeField(48 * 2, 48, 0, 47),
                new AutoTileTypeField(48 * 3, 48, 0, 47),
                new AutoTileTypeField(48 * 4, 48, 0, 47),
                new AutoTileTypeField(48 * 5, 48, 0, 47),
                new AutoTileTypeField(48 * 6, 48, 0, 47),
                new AutoTileTypeField(48 * 7, 48, 0, 47),
        };
    }

    @Override
    public int getFrame() {
        // 16 / 40
        double m = 16.0d / 40.0d;
        return (int) (GaBIEn.getTime() / m);
    }

    @Override
    public @Nullable AtlasSet getAtlasSet() {
        return atlasSet;
    }
}

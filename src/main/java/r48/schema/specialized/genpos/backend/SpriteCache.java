/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos.backend;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.AppMain;
import r48.RubyIO;
import r48.imagefx.HueShiftImageEffect;
import r48.imagefx.IImageEffect;
import r48.imagefx.MirrorSubspritesImageEffect;
import r48.imagefx.OpacityImageEffect;

import java.util.LinkedList;

/**
 * RMAnimRootPanel stuff that doesn't fit in IGenposFrame R/N
 * Created on 28/07/17.
 */
public class SpriteCache {
    public RubyIO target;
    public String framesetALoc, framesetBLoc, filePrefix;
    public int spriteSize;

    public SpriteCache(RubyIO targ, String fal, String fbl, int ss, String prefix) {
        target = targ;
        framesetALoc = fal;
        framesetBLoc = fbl;
        filePrefix = prefix;
        spriteSize = ss;
        prepareFramesetCache();
    }

    public IGrInDriver.IImage framesetCacheA, framesetCacheB;

    public int getScaledImageIconSize(int scale) {
        return (int) (spriteSize * (scale / 100.0d));
    }

    // Prepares a bunch of generated images (because IGrInDriver already has 2 special methods for this package only, one more and it'll explode)
    public void prepareFramesetCache() {
        String nameA = target.getInstVarBySymbol(framesetALoc).decString();
        String nameB = target.getInstVarBySymbol(framesetBLoc).decString();
        framesetCacheA = null;
        framesetCacheB = null;
        if (nameA.length() != 0)
            framesetCacheA = AppMain.stuffRendererIndependent.imageLoader.getImage(filePrefix + nameA, false);
        if (nameB.length() != 0)
            framesetCacheB = AppMain.stuffRendererIndependent.imageLoader.getImage(filePrefix + nameB, false);
    }

    public IGrInDriver.IImage getFramesetCache(boolean b, boolean mirror, int opacity, int hue) {
        LinkedList<IImageEffect> effectList = new LinkedList<IImageEffect>();
        if (hue != 0)
            effectList.add(new HueShiftImageEffect(hue));
        if (mirror)
            effectList.add(new MirrorSubspritesImageEffect(spriteSize, 5, 6));
        if (opacity != 255)
            effectList.add(new OpacityImageEffect(opacity));
        return AppMain.imageFXCache.process(b ? framesetCacheB : framesetCacheA, effectList);
    }
}

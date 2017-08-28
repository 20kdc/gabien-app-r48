/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos.backend;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ui.IFunction;
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
    public String framesetALoc, framesetAHue, framesetBLoc, framesetBHue;
    public IFunction<RubyIO, Integer> spsDeterminant;
    public IFunction<RubyIO, String> pfxDeterminant;

    private IImage framesetCacheA, framesetCacheB;
    public int spriteSize;

    public SpriteCache(RubyIO targ, String fal, String fah, String fbl, String fbh, IFunction<RubyIO, Integer> spriteSizeDeterminant, IFunction<RubyIO, String> prefixDeterminant) {
        target = targ;
        framesetALoc = fal;
        framesetBLoc = fbl;
        framesetAHue = fah;
        framesetBHue = fbh;
        spsDeterminant = spriteSizeDeterminant;
        pfxDeterminant = prefixDeterminant;
        prepareFramesetCache();
    }

    public int getScaledImageIconSize(int scale) {
        return (int) (spriteSize * (scale / 100.0d));
    }

    // Prepares the frameset images.
    public void prepareFramesetCache() {
        spriteSize = spsDeterminant.apply(target);
        framesetCacheA = GaBIEn.getErrorImage();
        if (framesetALoc != null) {
            String nameA = target.getInstVarBySymbol(framesetALoc).decString();
            if (nameA.length() != 0)
                framesetCacheA = AppMain.stuffRendererIndependent.imageLoader.getImage(pfxDeterminant.apply(target) + nameA, false);
            if (framesetAHue != null)
                framesetCacheA = AppMain.imageFXCache.process(framesetCacheA, new HueShiftImageEffect((int) target.getInstVarBySymbol(framesetAHue).fixnumVal));
        }
        framesetCacheB = GaBIEn.getErrorImage();
        if (framesetBLoc != null) {
            String nameB = target.getInstVarBySymbol(framesetBLoc).decString();
            if (nameB.length() != 0)
                framesetCacheB = AppMain.stuffRendererIndependent.imageLoader.getImage(pfxDeterminant.apply(target) + nameB, false);
            if (framesetBHue != null)
                framesetCacheB = AppMain.imageFXCache.process(framesetCacheB, new HueShiftImageEffect((int) target.getInstVarBySymbol(framesetBHue).fixnumVal));
        }
    }

    public IImage getFramesetCache(boolean b, boolean mirror, int opacity) {
        LinkedList<IImageEffect> effectList = new LinkedList<IImageEffect>();
        if (mirror)
            effectList.add(new MirrorSubspritesImageEffect(spriteSize, 5, 6));
        if (opacity != 255)
            effectList.add(new OpacityImageEffect(opacity));
        return AppMain.imageFXCache.process(b ? framesetCacheB : framesetCacheA, effectList);
    }
}

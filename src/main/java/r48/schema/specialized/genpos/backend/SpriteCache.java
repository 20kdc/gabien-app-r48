/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos.backend;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.IFunction;
import r48.AppMain;
import r48.imagefx.HueShiftImageEffect;
import r48.imagefx.IImageEffect;
import r48.imagefx.MirrorSubspritesImageEffect;
import r48.imagefx.OpacityImageEffect;
import r48.io.data.IRIO;

import java.util.LinkedList;

/**
 * RMAnimRootPanel stuff that doesn't fit in IGenposFrame R/N
 * Created on 28/07/17.
 */
public class SpriteCache {
    public IRIO target;
    public String framesetALoc, framesetAHue, framesetBLoc, framesetBHue;
    public IFunction<IRIO, Integer> spsDeterminant;
    public IFunction<IRIO, String> pfxDeterminant;

    private IImage framesetCacheA, framesetCacheB;
    public int spriteSize;

    public SpriteCache(IRIO targ, String fal, String fah, String fbl, String fbh, IFunction<IRIO, Integer> spriteSizeDeterminant, IFunction<IRIO, String> prefixDeterminant) {
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
            String nameA = target.getIVar(framesetALoc).decString();
            if (nameA.length() != 0)
                framesetCacheA = AppMain.stuffRendererIndependent.imageLoader.getImage(pfxDeterminant.apply(target) + nameA, false);
            if (framesetAHue != null)
                framesetCacheA = AppMain.imageFXCache.process(framesetCacheA, new HueShiftImageEffect((int) target.getIVar(framesetAHue).getFX()));
        }
        framesetCacheB = GaBIEn.getErrorImage();
        if (framesetBLoc != null) {
            String nameB = target.getIVar(framesetBLoc).decString();
            if (nameB.length() != 0)
                framesetCacheB = AppMain.stuffRendererIndependent.imageLoader.getImage(pfxDeterminant.apply(target) + nameB, false);
            if (framesetBHue != null)
                framesetCacheB = AppMain.imageFXCache.process(framesetCacheB, new HueShiftImageEffect((int) target.getIVar(framesetBHue).getFX()));
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

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.AppMain;
import r48.RubyIO;

/**
 * RMAnimRootPanel stuff that doesn't fit in IGenposFrame R/N
 * Created on 28/07/17.
 */
public class SpriteCache {
    public RubyIO target;
    public String framesetALoc, framesetBLoc;
    // 0-99: Set A 100-199: Set B
    public IGrInDriver.IImage[] framesetCacheA, framesetCacheB;
    public IGrInDriver.IImage[] framesetCacheAMirror, framesetCacheBMirror;

    public int getScaledImageIconSize(int scale) {
        return (int) (192 * (scale / 100.0d));
    }

    // Prepares a bunch of generated images (because IGrInDriver already has 2 special methods for this package only, one more and it'll explode)
    public void prepareFramesetCache() {
        String nameA = target.getInstVarBySymbol(framesetALoc).decString();
        String nameB = target.getInstVarBySymbol(framesetBLoc).decString();
        framesetCacheA = new IGrInDriver.IImage[16];
        framesetCacheB = new IGrInDriver.IImage[16];
        framesetCacheAMirror = new IGrInDriver.IImage[16];
        framesetCacheBMirror = new IGrInDriver.IImage[16];
        if (nameA.length() != 0) {
            framesetCacheA[15] = AppMain.stuffRendererIndependent.imageLoader.getImage("Animations/" + nameA, false);
            framesetCacheAMirror[15] = mirrorFrameset(framesetCacheA[15]);
        }
        if (nameB.length() != 0) {
            framesetCacheB[15] = AppMain.stuffRendererIndependent.imageLoader.getImage("Animations/" + nameB, false);
            framesetCacheBMirror[15] = mirrorFrameset(framesetCacheB[15]);
        }
    }

    public IGrInDriver.IImage getFramesetCache(boolean b, boolean mirror, int opacity) {
        IGrInDriver.IImage[] source = mirror ? framesetCacheAMirror : framesetCacheA;
        if (b)
            source = mirror ? framesetCacheBMirror : framesetCacheB;
        // ok, found the source
        if (source[15] == null)
            return null;
        opacity >>= 4;
        if (source[opacity] == null)
            source[opacity] = generateOpacityImage(source[15], (opacity << 4) + 0xF);
        return source[opacity];
    }

    private IGrInDriver.IImage generateOpacityImage(IGrInDriver.IImage image, int o) {
        int[] data = image.getPixels();
        for (int i = 0; i < data.length; i++) {
            int base = data[i] & 0xFFFFFF;
            int rest = (data[i] >> 24) & 0xFF;
            double dp = rest / 255.0d;
            dp *= o / 255.0d;
            rest = (int) (dp * 255);
            data[i] = base | (rest << 24);
        }
        return GaBIEn.createImage(data, image.getWidth(), image.getHeight());
    }

    private IGrInDriver.IImage mirrorFrameset(IGrInDriver.IImage framesetCache) {
        // 192x192 tiles, in 6 rows of 5.
        // Simple, right?
        // ... right?
        if (framesetCache == null)
            return null;
        int[] originalData = framesetCache.getPixels();
        int originalWidth = framesetCache.getWidth();
        int[] cache = new int[192 * 192 * 5 * 6];
        for (int y = 0; y < 6; y++)
            for (int x = 0; x < 5; x++)
                installMirror(cache, (x * 192) + (192 * 5 * (y * 192)), 192 * 5, originalData, (x * 192) + (originalWidth * (y * 192)), originalWidth);
        return GaBIEn.createImage(cache, 192 * 5, 192 * 6);
    }

    private void installMirror(int[] cache, int base, int stride, int[] origData, int baseO, int strideO) {
        for (int i = 0; i < 192; i++)
            installMirrorRow(cache, base + (stride * i), origData, baseO + (strideO * i));
    }

    private void installMirrorRow(int[] cache, int i, int[] origData, int i1) {
        for (int j = 0; j < 192; j++) {
            int tx = i + j;
            if (tx >= cache.length)
                continue;
            int tx2 = i1 + (191 - j);
            if (tx2 >= origData.length)
                continue;
            cache[tx] = origData[tx2];
        }
    }
}

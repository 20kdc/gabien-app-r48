/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.imagefx;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.IImage;

/**
 * Created on 30/07/17.
 */
public class MirrorSubspritesImageEffect implements IImageEffect {
    public final int spriteSize;
    public final int spritesW;
    public final int spritesH;

    public MirrorSubspritesImageEffect(int sub, int sub1, int sub2) {
        spriteSize = sub;
        spritesW = sub1;
        spritesH = sub2;
    }

    @Override
    public String uniqueToString() {
        return "M" + spriteSize + "." + spritesW + "." + spritesH;
    }

    @Override
    public IImage process(IImage framesetCache) {
        // 192x192 tiles, in 6 rows of 5.
        // Simple, right?
        // ... right?
        if (framesetCache == null)
            return null;
        int[] originalData = framesetCache.getPixels();
        int originalWidth = framesetCache.getWidth();
        int[] cache = new int[spriteSize * spriteSize * spritesW * spritesH];
        for (int y = 0; y < spritesH; y++)
            for (int x = 0; x < spritesW; x++)
                installMirror(cache, (x * spriteSize) + (spriteSize * spritesW * (y * spriteSize)), spriteSize * spritesW, originalData, (x * spriteSize) + (originalWidth * (y * spriteSize)), originalWidth);
        return GaBIEn.createImage(cache, spriteSize * spritesW, spriteSize * spritesH);
    }

    private void installMirror(int[] cache, int base, int stride, int[] origData, int baseO, int strideO) {
        for (int i = 0; i < spriteSize; i++)
            installMirrorRow(cache, base + (stride * i), origData, baseO + (strideO * i));
    }

    private void installMirrorRow(int[] cache, int i, int[] origData, int i1) {
        for (int j = 0; j < spriteSize; j++) {
            int tx = i + j;
            if (tx >= cache.length)
                continue;
            int tx2 = i1 + ((spriteSize - 1) - j);
            if (tx2 >= origData.length)
                continue;
            cache[tx] = origData[tx2];
        }
    }
}

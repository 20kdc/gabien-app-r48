/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.IImage;
import r48.dbs.TXDB;
import r48.imageio.ImageIOImage;

import java.util.LinkedList;

/**
 * The back-end-ish to the image editor.
 * Provides palette editing functions that are independent of an actual palette or a fake palette.
 * Created on April 14th 2018.
 */
public class ImageEditorImage extends ImageIOImage {
    // Internal backup for the palette APIs on truecolour images.
    private int[] editorPalette = new int[] {
            0x00201000,
            0xFF000000,
            0xFFFFFFFF,
            0xFF0000FF,
            0xFF00FFFF,
            0xFF00FF00,
            0xFFFFFF00,
            0xFFFF0000,
            0xFFFF00FF
    };
    // Set to null on modification.
    private IImage cachedData;
    public final boolean t1Lock;

    // The best way to put this is that it provides an extended "view" of a ImageIOImage.
    public ImageEditorImage(ImageIOImage copyFrom, boolean a1Lock) {
        super(copyFrom.width, copyFrom.height, copyFrom.colourData, copyFrom.palette);
        t1Lock = a1Lock;
        if (t1Lock)
            handleT1Import();
    }

    // This isn't exactly a copy.
    public ImageEditorImage(ImageIOImage copyFrom, boolean a1Lock, boolean makePal) {
        super(copyFrom.width, copyFrom.height, new int[copyFrom.width * copyFrom.height], makePal ? new LinkedList<Integer>() : null);
        if (makePal)
            palette.add(editorPalette[0]);
        t1Lock = a1Lock;
        if (t1Lock)
            handleT1Import();
        // Now that everything is ready, let's begin
        for (int i = 0; i < copyFrom.colourData.length; i++) {
            int c = copyFrom.getRGB(i, 0);
            if (makePal) {
                // We're doing it "by the book"
                int idx = palette.indexOf(c);
                if (idx == -1) {
                    appendToPalette(c);
                    idx = palette.size() - 1;
                }
                colourData[i] = idx;
            } else {
                // Just copy ARGB
                colourData[i] = c;
            }
        }
    }

    public ImageEditorImage(int width, int height, int[] newImage, LinkedList<Integer> palette, boolean a1Lock) {
        super(width, height, newImage, palette);
        t1Lock = a1Lock;
        if (t1Lock)
            handleT1Import();
    }

    public ImageEditorImage(int w, int h) {
        super(w, h, new int[w * h], new LinkedList<Integer>());
        if (palette != null) {
            for (int c : editorPalette)
                palette.add(c);
            t1Lock = true;
            handleT1Import();
        } else {
            t1Lock = false;
        }
    }

    private void handleT1Import() {
        if (palette == null)
            throw new IllegalArgumentException("cannot have null palette & t1lock");
        // Final pass now all swaps & such are done: Ensure only the first entry is transparent
        palette.set(0, palette.get(0) & 0xFFFFFF);
        int paletteSize = palette.size();
        for (int i = 1; i < paletteSize; i++)
            palette.set(i, palette.get(i) | 0xFF000000);
    }

    public IImage rasterize() {
        if (cachedData != null)
            return cachedData;
        return cachedData = super.rasterize();
    }

    // Sets a pixel by palette index.
    public void setPixel(int x, int y, int selPaletteIndex) {
        if (palette == null)
            selPaletteIndex = editorPalette[selPaletteIndex];
        colourData[x + (y * width)] = selPaletteIndex;
        cachedData = null;
    }

    public int paletteSize() {
        if (palette != null)
            return palette.size();
        return editorPalette.length;
    }

    public int getPaletteRGB(int idx) {
        if (palette == null)
            return editorPalette[idx];
        return palette.get(idx);
    }

    public String describeColourFormat() {
        if (t1Lock)
            return TXDB.get("Colourkey Indexed");
        return (palette != null) ? TXDB.get("Indexed") : TXDB.get("32-bit ARGB");
    }

    public void appendToPalette(int rgb) {
        if (palette == null) {
            int[] ia = new int[editorPalette.length + 1];
            System.arraycopy(editorPalette, 0, ia, 0, editorPalette.length);
            ia[ia.length - 1] = rgb;
            editorPalette = ia;
        } else {
            if (t1Lock)
                rgb |= 0xFF000000;
            palette.add(rgb);
            cachedData = null;
        }
    }

    public void removeFromPalette(int fidx, boolean sanity) {
        if (palette == null) {
            if (editorPalette.length <= 1)
                return;
            int[] ia = new int[editorPalette.length - 1];
            System.arraycopy(editorPalette, 0, ia, 0, fidx);
            System.arraycopy(editorPalette, fidx + 1, ia, fidx, ia.length - fidx);
            editorPalette = ia;
        } else {
            if (palette.size() <= 1)
                return;
            palette.remove(fidx);
            if (fidx == 0)
                palette.set(0, palette.get(0) & 0xFFFFFF);
            if (sanity)
                for (int i = 0; i < colourData.length; i++)
                    if (colourData[i] > fidx)
                        colourData[i]--;
            cachedData = null;
        }
    }

    public void swapInPalette(int selPaletteIndex, int fidx, boolean sanity) {
        if (palette == null) {
            int t = editorPalette[selPaletteIndex];
            editorPalette[selPaletteIndex] = editorPalette[fidx];
            editorPalette[fidx] = t;
        } else {
            // NOTE: This logic affects T1CK import, see ensureT1.
            if (sanity) {
                int selCol = palette.get(selPaletteIndex);
                int fidCol = palette.get(fidx);
                if (t1Lock) {
                    selCol |= 0xFF000000;
                    fidCol |= 0xFF000000;
                    if (selPaletteIndex == 0)
                        fidCol &= 0xFFFFFF;
                    if (fidx == 0)
                        selCol &= 0xFFFFFF;
                }
                palette.set(selPaletteIndex, fidCol);
                palette.set(fidx, selCol);
            }
            for (int i = 0; i < colourData.length; i++) {
                if (colourData[i] == selPaletteIndex) {
                    colourData[i] = fidx;
                } else if (colourData[i] == fidx) {
                    colourData[i] = selPaletteIndex;
                }
            }
            cachedData = null;
        }
    }

    public boolean usesPalette() {
        return palette != null;
    }
}

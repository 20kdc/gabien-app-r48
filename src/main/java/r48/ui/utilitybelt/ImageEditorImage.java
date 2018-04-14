/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.IImage;
import r48.dbs.TXDB;

/**
 * An image in the image editor.
 * Created on April 13th 2018
 */
public class ImageEditorImage {
    // If this exists, the image strictly follows this palette.
    private int[] palette;
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
    public final int width, height;
    // ARGB or indexes as appropriate.
    private int[] colourData;
    private IImage cachedData;
    public final boolean t1Lock;

    public ImageEditorImage(int w, int h, int[] data, boolean indexed, boolean a1Lock) {
        t1Lock = a1Lock;
        if (a1Lock && !indexed)
            throw new RuntimeException("Cannot use A1Lock with non-indexed image");
        width = w;
        height = h;
        if (data.length != (w * h))
            throw new IndexOutOfBoundsException("Length must be equal to w * h (" + (w * h) + "), not " + data.length);
        colourData = data;
        if (indexed) {
            palette = editorPalette;
            editorPalette = null;
        }
    }

    public IImage rasterize() {
        if (cachedData != null)
            return cachedData;
        if (palette == null) {
            return cachedData = GaBIEn.createImage(colourData, width, height);
        } else {
            int[] data = new int[colourData.length];
            for (int i = 0; i < data.length; i++)
                data[i] = translatePalette(colourData[i]);
            return cachedData = GaBIEn.createImage(data, width, height);
        }
    }

    private int translatePalette(int c) {
        if ((c < 0) || (c >= palette.length)) {
            return 0xFFFF00FF;
        } else {
            return palette[c];
        }
    }

    // Sets a pixel by palette index.
    public void setPixel(int x, int y, int selPaletteIndex) {
        if (palette == null)
            selPaletteIndex = editorPalette[selPaletteIndex];
        colourData[x + (y * width)] = selPaletteIndex;
        cachedData = null;
    }

    public int getRGB(int i, int j) {
        int c = colourData[i + (j * width)];
        if (palette != null)
            return translatePalette(c);
        return c;
    }

    public int getRaw(int i, int j) {
        return colourData[i + (j * width)];
    }

    public void changePalette(int[] newPal) {
        if (newPal.length == 0)
            throw new IndexOutOfBoundsException("Cannot use palette with no entries");
        if (palette != null) {
            palette = newPal;
            cachedData = null;
            if (t1Lock) {
                // Ensure t1Lock constraint followed
                boolean aok = false;
                for (int i = 0; i < newPal.length; i++) {
                    if ((newPal[i] & 0xFF000000) == 0) {
                        swapInPalette(0, i, true);
                        aok = true;
                        break;
                    }
                }
                if (!aok) {
                    appendToPalette(0x800080); // this will get changed to 0xFF800080
                    swapInPalette(0, palette.length - 1, true);
                    palette[0] = 0; // fix it
                }
                // Final pass now all swaps & such are done: Ensure only the first entry is transparent
                for (int i = 1; i < palette.length; i++)
                    palette[i] |= 0xFF000000;
            }
        } else {
            editorPalette = newPal;
        }
    }

    public int paletteSize() {
        if (palette != null)
            return palette.length;
        return editorPalette.length;
    }

    public int getPaletteRGB(int idx) {
        if (palette == null)
            return editorPalette[idx];
        return palette[idx];
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
            int[] ia = new int[palette.length + 1];
            System.arraycopy(palette, 0, ia, 0, palette.length);
            ia[ia.length - 1] = rgb;
            palette = ia;
            cachedData = null;
        }
    }

    public void removeFromPalette(int fidx, boolean sanity) {
        if (palette == null) {
            int[] ia = new int[editorPalette.length - 1];
            System.arraycopy(editorPalette, 0, ia, 0, fidx);
            System.arraycopy(editorPalette, fidx + 1, ia, fidx, ia.length - fidx);
            editorPalette = ia;
        } else {
            int[] ia = new int[palette.length - 1];
            System.arraycopy(palette, 0, ia, 0, fidx);
            System.arraycopy(palette, fidx + 1, ia, fidx, ia.length - fidx);
            palette = ia;
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
            if (sanity) {
                int t = palette[selPaletteIndex];
                palette[selPaletteIndex] = palette[fidx];
                palette[fidx] = t;
                if (t1Lock) {
                    palette[selPaletteIndex] |= 0xFF000000;
                    palette[fidx] |= 0xFF000000;
                    palette[0] &= 0xFFFFFF;
                    cachedData = null;
                    return;
                }
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

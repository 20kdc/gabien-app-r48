/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import r48.imageio.ImageIOImage;

import java.util.LinkedList;

import gabien.render.IImage;

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
    private IImage cachedD1;
    // Locks palette index 0 to transparent, others are not.
    // This is essentially RPG Maker Emulation Mode.
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
                // Upper bit indicates alpha >= 0x80, so c >= 0 means < 0x80 alpha
                int idx;
                if (a1Lock && (c >= 0)) {
                    idx = 0;
                } else {
                    // We're doing it "by the book"
                    idx = palette.indexOf(c);
                    if (idx == -1) {
                        appendToPalette(c);
                        idx = palette.size() - 1;
                    }
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
        for (int c : editorPalette)
            palette.add(c);
        t1Lock = true;
        handleT1Import();
    }

    @Override
    public ImageEditorImage clone() {
        LinkedList<Integer> pal = null;
        if (palette != null)
            pal = new LinkedList<Integer>(palette);
        ImageEditorImage iei2 = new ImageEditorImage(width, height, new int[width * height], pal, t1Lock);
        if (palette == null) {
            iei2.editorPalette = new int[editorPalette.length];
            System.arraycopy(editorPalette, 0, iei2.editorPalette, 0, editorPalette.length);
        }
        System.arraycopy(colourData, 0, iei2.colourData, 0, iei2.colourData.length);
        return iei2;
    }

    public boolean equalToImage(ImageEditorImage iei) {
        if (iei.palette == null) {
            if (palette != null)
                return false;
        } else {
            if (palette == null)
                return false;
        }
        if (iei.paletteSize() != paletteSize())
            return false;
        int size = paletteSize();
        for (int i = 0; i < size; i++)
            if (getPaletteRGB(i) != iei.getPaletteRGB(i))
                return false;
        if (iei.width != width)
            return false;
        if (iei.height != height)
            return false;
        for (int i = 0; i < colourData.length; i++)
            if (colourData[i] != iei.colourData[i])
                return false;
        return true;
    }

    private void handleT1Import() {
        if (palette == null)
            throw new IllegalArgumentException("cannot have null palette & t1lock");
        // Final pass now all swaps & such are done: Ensure only the first entry is transparent
        int paletteSize = palette.size();
        for (int i = 0; i < paletteSize; i++)
            palette.set(i, sanitizeColour(palette.get(i), i));
    }

    private void clearCachedData() {
        cachedD1 = null;
    }

    public IImage rasterize() {
        if (cachedD1 != null)
            return cachedD1;
        return cachedD1 = super.rasterize();
    }

    // Fun wrapper function like thing
    public int rawToPalette(int rawValue) {
        // There is a palette, so this must be within that. Right?
        if (palette != null) {
            if (rawValue < 0)
                return 0;
            if (rawValue >= palette.size())
                return palette.size() - 1;
            return rawValue;
        }
        // There isn't a palette. If there's anything in the editor palette, use it,
        //  else just give up and add something.
        for (int i = 0; i < editorPalette.length; i++)
            if (editorPalette[i] == rawValue)
                return i;
        appendToPalette(rawValue);
        return editorPalette.length - 1;
    }

    // Sets a pixel by palette index.
    public void setPixel(int x, int y, int selPaletteIndex) {
        if (palette == null)
            selPaletteIndex = editorPalette[selPaletteIndex];
        colourData[x + (y * width)] = selPaletteIndex;
        clearCachedData();
    }

    public void setRaw(int x, int y, int selPaletteIndex) {
        colourData[x + (y * width)] = selPaletteIndex;
        clearCachedData();
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

    public void appendToPalette(int rgb) {
        if (palette == null) {
            int[] ia = new int[editorPalette.length + 1];
            System.arraycopy(editorPalette, 0, ia, 0, editorPalette.length);
            ia[ia.length - 1] = rgb;
            editorPalette = ia;
        } else {
            int idx = palette.size();
            rgb = sanitizeColour(rgb, idx);
            palette.add(rgb);
            clearCachedData();
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
            int paletteSize = palette.size();
            if (paletteSize <= 1)
                return;
            palette.remove(fidx);
            paletteSize--;
            for (int i = 0; i < paletteSize; i++)
                palette.set(i, sanitizeColour(palette.get(i), i));
            if (sanity)
                for (int i = 0; i < colourData.length; i++)
                    if (colourData[i] > fidx)
                        colourData[i]--;
            clearCachedData();
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
                // Sanitize colours for new indexes
                selCol = sanitizeColour(selCol, fidx);
                fidCol = sanitizeColour(fidCol, selPaletteIndex);
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
            clearCachedData();
        }
    }

    private int sanitizeColour(int fidCol, int selPaletteIndex) {
        if (t1Lock) {
            fidCol |= 0xFF000000;
            if (selPaletteIndex == 0)
                fidCol &= 0xFFFFFF;
        }
        return fidCol;
    }

    public void changePalette(int fidx, int col) {
        if (palette == null) {
            editorPalette[fidx] = col;
        } else {
            palette.set(fidx, sanitizeColour(col, fidx));
        }
        clearCachedData();
    }

    public boolean usesPalette() {
        return palette != null;
    }
}

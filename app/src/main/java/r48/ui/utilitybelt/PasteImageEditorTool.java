/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.*;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;
import gabien.uslx.io.ByteArrayMemoryish;
import r48.App;
import r48.io.BMPConnection;
import r48.io.data.RORIO;

import java.io.IOException;

/**
 * Created on 14th July 2018
 */
public class PasteImageEditorTool extends ImageEditorTool {
    public boolean flipX, flipY, swapXY;
    public boolean rawCopy = true;

    public PasteImageEditorTool(App app) {
        super(app);
    }

    @Override
    public void forceDifferentTool(UIImageEditView uiev) {
    }

    @Override
    public void apply(int x, int y, UIImageEditView view, boolean major, boolean dragging) {
        if (!major)
            return;
        if (dragging)
            return;
        BMPConnection result = null;

        RORIO ro = app.theClipboard;
        if (ro != null) {
            if (ro.getType() == 'u') {
                if (ro.getSymbol().equals("Image")) {
                    // Everything valid so far...
                    try {
                        result = new BMPConnection(new ByteArrayMemoryish(ro.getBufferCopy()), BMPConnection.CMode.Normal, 0, false);
                    } catch (IOException ioe) {
                    }
                }
            }
        }

        if (result == null) {
            app.ui.launchDialog(T.ie.badClipboard);
            return;
        }

        view.eds.startSection();
        // Firstly, check if colour mapping is needed.
        // If not a raw copy, colour mapping isn't allowed.
        boolean performColourMap = true;
        boolean imageUsesPal = view.image.usesPalette();
        int imagePalSize = view.image.paletteSize();
        if ((!result.ignoresPalette) && imageUsesPal && rawCopy) {
            if (result.paletteCol == imagePalSize) {
                performColourMap = false;
                for (int i = 0; i < result.paletteCol; i++)
                    if ((result.getPalette(i) & 0xFFFFFF) != (view.image.getPaletteRGB(i) & 0xFFFFFF))
                        performColourMap = true;
            }
        }
        // Secondly, perform the main copy loop.
        for (int i = 0; i < result.width; i++) {
            for (int j = 0; j < result.height; j++) {
                int i2 = i, j2 = j;
                if (flipX)
                    i2 = result.width - (1 + i);
                if (flipY)
                    j2 = result.height - (1 + j);
                if (swapXY) {
                    int k = i2;
                    i2 = j;
                    j2 = k;
                }
                FillAlgorithm.Point p = view.correctPoint(x + i2, y + j2);
                if (p != null) {
                    // For each pixel...
                    int res;
                    if (performColourMap) {
                        // Input
                        int argb = result.getPixel(i, j);
                        if (!result.ignoresPalette)
                            argb = result.getPalette(argb);
                        // If not a raw copy, modify for blending
                        if (!rawCopy) {
                            int argbSurface = view.image.getRGB(p.x, p.y);
                            // split
                            int a1 = (argbSurface >> 24) & 0xFF;
                            int a2 = (argb >> 24) & 0xFF;
                            int r1 = (argbSurface >> 16) & 0xFF;
                            int r2 = (argb >> 16) & 0xFF;
                            int g1 = (argbSurface >> 8) & 0xFF;
                            int g2 = (argb >> 8) & 0xFF;
                            int b1 = (argbSurface >> 0) & 0xFF;
                            int b2 = (argb >> 0) & 0xFF;
                            // calculate
                            int f1 = (a1 * (0xFF - a2)) / 0xFF;
                            int f2 = a2;
                            int a3 = a2 + f1;
                            if (a3 > 0xFF)
                                a3 = 0xFF;
                            int a3d = a3;
                            if (a3d == 0)
                                a3d = 1;
                            int r3 = ((f1 * r1) + (f2 * r2)) / a3d;
                            int g3 = ((f1 * g1) + (f2 * g2)) / a3d;
                            int b3 = ((f1 * b1) + (f2 * b2)) / a3d;
                            // join
                            argb = (a3 << 24) | (r3 << 16) | (g3 << 8) | (b3 << 0);
                        }
                        // Output
                        if (imageUsesPal) {
                            int distance = Integer.MAX_VALUE;
                            int best = 0;
                            for (int k = 0; k < imagePalSize; k++) {
                                int argb2 = view.image.getPaletteRGB(k);
                                int ndist = colourDistance(argb, argb2);
                                if (ndist < distance) {
                                    best = k;
                                    distance = ndist;
                                }
                            }
                            res = best;
                        } else {
                            res = argb;
                        }
                    } else {
                        // known to be a valid index
                        res = result.getPixel(i, j);
                    }
                    view.image.setRaw(p.x, p.y, res);
                }
            }
        }
        view.eds.endSection();
        view.newToolCallback.run();
    }

    private int colourDistance(int argb, int argb2) {
        int ad = Math.abs(((argb >> 24) & 0xFF) - ((argb2 >> 24) & 0xFF));
        int rd = Math.abs(((argb >> 16) & 0xFF) - ((argb2 >> 16) & 0xFF));
        int gd = Math.abs(((argb >> 8) & 0xFF) - ((argb2 >> 8) & 0xFF));
        int bd = Math.abs((argb & 0xFF) - (argb2 & 0xFF));
        return (ad * 768) + rd + gd + bd;
    }

    @Override
    public void endApply(UIImageEditView view) {
    }

    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        UITextButton a = new UITextButton(T.ie.flipX, app.f.schemaFieldTH, () -> {
            flipX = !flipX;
        }).togglable(flipX);
        UITextButton b = new UITextButton(T.ie.flipY, app.f.schemaFieldTH, () -> {
            flipY = !flipY;
        }).togglable(flipY);
        UITextButton c = new UITextButton(T.ie.swapXY, app.f.schemaFieldTH, () -> {
            swapXY = !swapXY;
        }).togglable(swapXY);
        UITextButton d = new UITextButton(T.ie.rawCopy, app.f.schemaFieldTH, () -> {
            rawCopy = !rawCopy;
        }).togglable(rawCopy);
        UISplitterLayout sl = new UISplitterLayout(a, b, false, 0.5d);
        UIElement toolPalette = RootImageEditorTool.createToolPalette(uiev, PasteImageEditorTool.class);
        return new UIScrollLayout(true, app.f.mapToolbarS, toolPalette, new UISplitterLayout(sl, c, false, 0.6666d), d);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return T.ie.tdPaste;
    }

    @Override
    public ImageEditorTool getCamModeLT() {
        return null;
    }

}

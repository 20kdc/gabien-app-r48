/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.maptools.UIMTBase;
import r48.ui.UIAppendButton;
import r48.ui.UIColourPicker;
import r48.ui.UIColourSwatch;

import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Oh, this can't be good news.
 * - 7th October, 2017
 */
public class ImageEditorController {
    public UISplitterLayout rootView;
    private UIImageEditView imageEditView;
    private UIScrollLayout paletteView;
    public LinkedList<Integer> palette = new LinkedList<Integer>();
    public int selPaletteIndex = 0;
    public boolean rectangleRunning;
    public int rectanglePoint1X, rectanglePoint1Y;
    public IConsumer<UIElement> windowMaker;

    public ImageEditorController(IConsumer<UIElement> worldMachine) {
        windowMaker = worldMachine;
        palette.add(0xFF000000);
        palette.add(0xFF0000FF);
        palette.add(0xFF00FF00);
        palette.add(0xFF00FFFF);
        palette.add(0xFFFF0000);
        palette.add(0xFFFF00FF);
        palette.add(0xFFFFFF00);
        palette.add(0xFFFFFFFF);
        palette.add(0x00000000);
        imageEditView = new UIImageEditView(new Runnable() {
            @Override
            public void run() {
                applyCursor();
            }
        });
        paletteView = new UIScrollLayout(true, FontSizes.generalScrollersize);
        initPalette();
        rootView = new UISplitterLayout(imageEditView, paletteView, false, 1.0d) {
            @Override
            public String toString() {
                return TXDB.get("Image Editor");
            }
        };
    }

    public void applyCursor() {
        if (!rectangleRunning) {
            rectanglePoint1X = imageEditView.cursorX;
            rectanglePoint1Y = imageEditView.cursorY;
        }
        int col = palette.get(selPaletteIndex);
        for (int i = Math.max(0, Math.min(rectanglePoint1X, imageEditView.cursorX)); i < Math.min(imageEditView.imageW, Math.max(rectanglePoint1X, imageEditView.cursorX) + 1); i++)
            for (int j = Math.max(0, Math.min(rectanglePoint1Y, imageEditView.cursorY)); j < Math.min(imageEditView.imageH, Math.max(rectanglePoint1Y, imageEditView.cursorY) + 1); j++)
                imageEditView.image[i + (j * imageEditView.imageW)] = col;
        imageEditView.showTarget = false;
        if (rectangleRunning) {
            rectangleRunning = false;
            initPalette();
        }
    }

    public void load(String filename) {
        GaBIEn.hintFlushAllTheCaches();
        IImage im = GaBIEn.getImageEx(filename, true, false);
        if (im == GaBIEn.getErrorImage()) {
            AppMain.launchDialog("Failed to load " + filename + ".");
        } else {
            imageEditView.imageW = im.getWidth();
            imageEditView.imageH = im.getHeight();
            imageEditView.image = im.getPixels();
        }
    }

    public void save(String filename) {
        try {
            OutputStream os = GaBIEn.getOutFile(filename);
            os.write(imageEditView.createImg().createPNG());
            os.close();
        } catch (Exception e) {
            AppMain.launchDialog("Failed to save " + filename + ".");
        }
    }

    private void initPalette() {
        paletteView.panelsClear();
        final String fbStrB = TXDB.get("Back");
        final String fbStrA = TXDB.get("Accept");
        final String fbStrL = TXDB.get("Load: ");
        final String fbStrS = TXDB.get("Save: ");
        UISplitterLayout saveLoad = new UISplitterLayout(new UITextButton(TXDB.get("Load PNG..."), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(new UIFileBrowser(new IConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        if (s != null)
                            load(s);
                    }
                }, fbStrL, fbStrB, fbStrA, FontSizes.schemaButtonTextHeight, FontSizes.generalScrollersize));
            }
        }), new UITextButton(TXDB.get("Save PNG..."), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(new UIFileBrowser(new IConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        if (s != null)
                            save(s);
                    }
                }, fbStrS, fbStrB, fbStrA, FontSizes.schemaButtonTextHeight, FontSizes.generalScrollersize));
            }
        }), false, 0.5d);
        paletteView.panelsAdd(saveLoad);
        int widthGuess = saveLoad.getWantedSize().width;
        paletteView.panelsAdd(new UISplitterLayout(new UITextButton(TXDB.get("Add Colour"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(new UIColourPicker(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == null)
                            return;
                        palette.add(integer);
                        initPalette();
                    }
                }, true));
            }
        }), new UITextButton(TXDB.get("From Image"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                palette.add(imageEditView.image[imageEditView.cursorX + (imageEditView.cursorY * imageEditView.imageW)]);
                initPalette();
            }
        }), false, 0.5d));
        if (!rectangleRunning) {
            paletteView.panelsAdd(new UITextButton(TXDB.get("Rectangle"), FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    rectangleRunning = true;
                    imageEditView.targetX = rectanglePoint1X = imageEditView.cursorX;
                    imageEditView.targetY = rectanglePoint1Y = imageEditView.cursorY;
                    imageEditView.showTarget = true;
                    initPalette();
                }
            }));
        } else {
            paletteView.panelsAdd(new UITextButton(TXDB.get("Cancel"), FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    rectangleRunning = false;
                    imageEditView.showTarget = false;
                    initPalette();
                }
            }));
        }
        paletteView.panelsAdd(new UITextButton(TXDB.get("Resize"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                showXYChanger(new Rect(0, 0, imageEditView.imageW, imageEditView.imageH), new IConsumer<Rect>() {
                    @Override
                    public void accept(Rect rect) {
                        // X/Y is where to put the input on the output.
                        int[] newImage = new int[rect.width * rect.height];
                        for (int i = 0; i < Math.min(rect.width - rect.x, imageEditView.imageW); i++) {
                            for (int j = 0; j < Math.min(rect.height - rect.y, imageEditView.imageH); j++) {
                                if (i + rect.x < 0)
                                    continue;
                                if (j + rect.y < 0)
                                    continue;
                                int c = imageEditView.image[i + (j * imageEditView.imageW)];
                                newImage[(i + rect.x) + ((j + rect.y) * rect.width)] = c;
                            }
                        }
                        imageEditView.cursorX = rect.width / 2;
                        imageEditView.cursorY = rect.height / 2;
                        imageEditView.imageW = rect.width;
                        imageEditView.imageH = rect.height;
                        imageEditView.image = newImage;
                    }
                }, TXDB.get("Resize..."));
            }
        }));
        paletteView.panelsAdd(new UISplitterLayout(new UITextButton(TXDB.get("Grid Size"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                showXYChanger(new Rect(imageEditView.gridOX, imageEditView.gridOY, imageEditView.gridW, imageEditView.gridH), new IConsumer<Rect>() {
                    @Override
                    public void accept(Rect rect) {
                        imageEditView.gridOX = rect.x;
                        imageEditView.gridOY = rect.y;
                        imageEditView.gridW = rect.width;
                        imageEditView.gridH = rect.height;
                    }
                }, TXDB.get("Change Grid..."));
            }
        }), new UITextButton(TXDB.get("Colour"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(new UIColourPicker(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == null)
                            return;
                        imageEditView.gridColour = integer & 0xFFFFFF;
                    }
                }, false));
            }
        }), false, 0.5d));
        int idx = 0;
        for (final Integer col : palette) {
            final int fidx = idx;
            UIElement cPanel = new UIColourSwatch(col);
            if (selPaletteIndex == fidx) {
                cPanel = new UIAppendButton("X", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        if (palette.size() > 1) {
                            selPaletteIndex--;
                            if (selPaletteIndex == -1)
                                selPaletteIndex++;
                            palette.remove(fidx);
                            initPalette();
                        }
                    }
                }, FontSizes.schemaButtonTextHeight);
            } else {
                cPanel = new UIAppendButton("~", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        int thi = palette.get(fidx);
                        int tha = palette.get(selPaletteIndex);
                        palette.set(selPaletteIndex, thi);
                        palette.set(fidx, tha);
                        initPalette();
                    }
                }, FontSizes.schemaButtonTextHeight);
            }
            cPanel = new UISplitterLayout(new UITextButton("<", FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    if (fidx != selPaletteIndex) {
                        selPaletteIndex = fidx;
                        initPalette();
                    }
                }
            }), cPanel, false, 0.0d);
            paletteView.panelsAdd(cPanel);
            idx++;
        }
    }

    private void showXYChanger(Rect targetVal, final IConsumer<Rect> iConsumer, final String title) {
        UIScrollLayout xyChanger = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public String toString() {
                return title;
            }
        };
        final UIMTBase res = UIMTBase.wrap(null, xyChanger);
        final UINumberBox wVal, hVal, xVal, yVal;
        final UITextButton acceptButton;
        wVal = new UINumberBox(targetVal.width, FontSizes.schemaFieldTextHeight);
        hVal = new UINumberBox(targetVal.height, FontSizes.schemaFieldTextHeight);
        xVal = new UINumberBox(targetVal.x, FontSizes.schemaFieldTextHeight);
        yVal = new UINumberBox(targetVal.y, FontSizes.schemaFieldTextHeight);
        hVal.onEdit = wVal.onEdit = new Runnable() {
            @Override
            public void run() {
                wVal.number = Math.max(1, wVal.number);
                hVal.number = Math.max(1, hVal.number);
            }
        };

        acceptButton = new UITextButton(TXDB.get("Accept"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                Rect r = new Rect((int) xVal.number, (int) yVal.number, Math.max((int) wVal.number, 1), Math.max((int) hVal.number, 1));
                iConsumer.accept(r);
                res.selfClose = true;
            }
        });
        xyChanger.panelsAdd(new UILabel(TXDB.get("Size"), FontSizes.schemaFieldTextHeight));
        xyChanger.panelsAdd(new UISplitterLayout(wVal, hVal, false, 1, 2));
        xyChanger.panelsAdd(new UILabel(TXDB.get("Offset"), FontSizes.schemaFieldTextHeight));
        xyChanger.panelsAdd(new UISplitterLayout(xVal, yVal, false, 1, 2));
        xyChanger.panelsAdd(acceptButton);

        res.forceToRecommended(null);
        windowMaker.accept(res);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.io.PathUtils;
import r48.maptools.UIMTBase;
import r48.ui.UIAppendButton;
import r48.ui.UIColourPicker;
import r48.ui.UIColourSwatch;

import java.io.OutputStream;

/**
 * Oh, this can't be good news.
 * - 7th October, 2017
 */
public class ImageEditorController {
    public UISplitterLayout rootView;
    private UIImageEditView imageEditView;
    private UIScrollLayout paletteView;
    public int selPaletteIndex = 0;
    public boolean rectangleRunning;
    public int rectanglePoint1X, rectanglePoint1Y;
    public IConsumer<UIElement> windowMaker;

    // This holds a bit of state, so let's just attach it/detach it as we want
    private final UITextButton sanityButton = new UITextButton(TXDB.get("Adjust"), FontSizes.schemaButtonTextHeight, null).togglable(true);
    // The current thing holding the sanity button (needed so it can be broken apart on UI rebuild)
    private UISplitterLayout sanityButtonHolder = null;

    public ImageEditorController(IConsumer<UIElement> worldMachine) {
        windowMaker = worldMachine;
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
        for (int i = Math.max(0, Math.min(rectanglePoint1X, imageEditView.cursorX)); i < Math.min(imageEditView.image.width, Math.max(rectanglePoint1X, imageEditView.cursorX) + 1); i++)
            for (int j = Math.max(0, Math.min(rectanglePoint1Y, imageEditView.cursorY)); j < Math.min(imageEditView.image.height, Math.max(rectanglePoint1Y, imageEditView.cursorY) + 1); j++)
                imageEditView.image.setPixel(i, j, selPaletteIndex);
        imageEditView.showTarget = false;
        if (rectangleRunning) {
            rectangleRunning = false;
            initPalette();
        }
    }

    public void load(String filename) {
        for (ImageEditorFormat ief : ImageEditorFormat.supportedFormats) {
            ImageEditorImage iei = ief.loadFile(filename);
            if (iei != null) {
                imageEditView.setImage(iei);
                initPalette();
                return;
            }
        }
        AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Failed to load #A."), new RubyIO().setString(filename, true)));
    }

    private void initPalette() {
        paletteView.panelsClear();
        if (sanityButtonHolder != null) {
            sanityButtonHolder.release();
            sanityButtonHolder = null;
        }
        final String fbStrB = TXDB.get("Back");
        final String fbStrAL = TXDB.get("Load");
        final String fbStrAS = TXDB.get("Save");
        final String fbStrL = TXDB.get("Load: ");
        final String fbStrS = TXDB.get("Save: ");

        // <Load.><Save1>
        // <Save2><Save3>
        // <Save4> blank
        UITextButton firstButton = new UITextButton(TXDB.get("Load"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(AppMain.setFBSize(new UIFileBrowser(new IConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        if (s != null)
                            load(s);
                    }
                }, fbStrL, fbStrB, fbStrAL, FontSizes.schemaButtonTextHeight, FontSizes.generalScrollersize)));
            }
        });
        for (final ImageEditorFormat format : ImageEditorFormat.supportedFormats) {
            String tx = format.saveName(imageEditView.image);
            if (tx == null)
                continue;
            UITextButton button = new UITextButton(tx, FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    if (imageEditView.image.t1Lock) {
                        // SDL_SetColorKey if surface format is converted to RGB,
                        // R48 loading b/c of the cheat used to avoid implementing the entire PNG specification,
                        // probably DirectDraw...
                        int comparison = imageEditView.image.getPaletteRGB(0) | 0xFF000000;
                        int palSize = imageEditView.image.paletteSize();
                        for (int i = 1; i < palSize; i++) {
                            if (imageEditView.image.getPaletteRGB(i) == comparison) {
                                AppMain.launchDialog(TXDB.get("One of the colours in the palette is the same as the colourkey. This makes various software go haywire, including R48. Please create a new colour and swap it for the transparency colour."));
                                return;
                            }
                        }
                    }
                    if (imageEditView.image.usesPalette()) {
                        int palSize = imageEditView.image.paletteSize();
                        boolean done = false;
                        for (int i = 0; i < palSize; i++) {
                            int c = imageEditView.image.getPaletteRGB(i);
                            for (int j = i + 1; j < palSize; j++) {
                                if (c == imageEditView.image.getPaletteRGB(j)) {
                                    AppMain.pendingRunnables.add(new Runnable() {
                                        @Override
                                        public void run() {
                                            // this is a holdover until v1.1.1 or so
                                            AppMain.launchDialog(TXDB.get("Two colours in the palette are the same. This confuses R48, so if loaded again, uses of the second instance will become uses of the first. Just a warning."));
                                        }
                                    });
                                    done = true;
                                    break;
                                }
                            }
                            if (done)
                                break;
                        }
                    }
                    windowMaker.accept(AppMain.setFBSize(new UIFileBrowser(new IConsumer<String>() {
                        @Override
                        public void accept(String s) {
                            if (s != null) {
                                try {
                                    byte[] data = format.saveFile(imageEditView.image);
                                    OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(s));
                                    os.write(data);
                                    os.close();
                                } catch (Exception e) {
                                    AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Failed to save #A.") + "\n" + e, new RubyIO().setString(s, true)));
                                }
                            }
                        }
                    }, fbStrS, fbStrB, fbStrAS, FontSizes.schemaButtonTextHeight, FontSizes.generalScrollersize)));
                }
            });
            if (firstButton != null) {
                paletteView.panelsAdd(new UISplitterLayout(firstButton, button, false, 0.5d));
                firstButton = null;
            } else {
                firstButton = button;
            }
        }
        if (firstButton != null)
            paletteView.panelsAdd(new UISplitterLayout(firstButton, new UIPublicPanel(1, 1), false, 0.5d));

        paletteView.panelsAdd(new UISplitterLayout(new UITextButton(TXDB.get("Add Colour"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(new UIColourPicker(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == null)
                            return;
                        imageEditView.image.appendToPalette(integer);
                        initPalette();
                    }
                }, true));
            }
        }), new UITextButton(TXDB.get("From Image"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                imageEditView.image.appendToPalette(imageEditView.image.getRGB(imageEditView.cursorX, imageEditView.cursorY));
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
                showXYChanger(new Rect(0, 0, imageEditView.image.width, imageEditView.image.height), new IConsumer<Rect>() {
                    @Override
                    public void accept(Rect rect) {
                        // X/Y is where to put the input on the output.
                        int[] newImage = new int[rect.width * rect.height];
                        for (int i = 0; i < Math.min(rect.width - rect.x, imageEditView.image.width); i++) {
                            for (int j = 0; j < Math.min(rect.height - rect.y, imageEditView.image.height); j++) {
                                if (i + rect.x < 0)
                                    continue;
                                if (j + rect.y < 0)
                                    continue;
                                newImage[(i + rect.x) + ((j + rect.y) * rect.width)] = imageEditView.image.getRaw(i, j);
                            }
                        }
                        imageEditView.cursorX = rect.width / 2;
                        imageEditView.cursorY = rect.height / 2;
                        int[] oldPalette = new int[imageEditView.image.paletteSize()];
                        for (int i = 0; i < oldPalette.length; i++)
                            oldPalette[i] = imageEditView.image.getPaletteRGB(i);
                        imageEditView.setImage(new ImageEditorImage(rect.width, rect.height, newImage, imageEditView.image.usesPalette(), imageEditView.image.t1Lock));
                        imageEditView.image.changePalette(oldPalette);
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

        // mode details
        UILabel cType = new UILabel(imageEditView.image.describeColourFormat(), FontSizes.schemaFieldTextHeight);
        if (imageEditView.image.usesPalette()) {
            paletteView.panelsAdd(sanityButtonHolder = new UISplitterLayout(cType, sanityButton, false, 1));
        } else {
            paletteView.panelsAdd(cType);
        }

        for (int idx = 0; idx < imageEditView.image.paletteSize(); idx++) {
            final int fidx = idx;
            UIElement cPanel = new UIColourSwatch(imageEditView.image.getPaletteRGB(idx));
            if (selPaletteIndex == fidx) {
                cPanel = new UIAppendButton("X", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        if (imageEditView.image.paletteSize() > 1) {
                            selPaletteIndex--;
                            if (selPaletteIndex == -1)
                                selPaletteIndex++;
                            imageEditView.image.removeFromPalette(fidx, sanityButton.state);
                            initPalette();
                        }
                    }
                }, FontSizes.schemaButtonTextHeight);
            } else {
                cPanel = new UIAppendButton("~", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        imageEditView.image.swapInPalette(selPaletteIndex, fidx, sanityButton.state);
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

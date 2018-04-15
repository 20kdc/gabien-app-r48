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
import r48.imageio.ImageIOFormat;
import r48.imageio.ImageIOImage;
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
        GaBIEn.hintFlushAllTheCaches();
        ImageIOImage ioi = ImageIOFormat.tryToLoad(filename, ImageIOFormat.supportedFormats);
        if (ioi == null) {
            AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Failed to load #A."), new RubyIO().setString(filename, true)));
        } else {
            // Detect assets that use the tRNS chunk correctly
            boolean detected = false;
            if (ioi.palette != null) {
                if ((ioi.palette.get(0) & 0xFF000000) == 0) {
                    detected = true;
                    for (int i = 1; i < ioi.palette.size(); i++) {
                        if ((ioi.palette.get(i) & 0xFF000000) != 0xFF000000) {
                            detected = false;
                            break;
                        }
                    }
                }
            }
            imageEditView.setImage(new ImageEditorImage(ioi, detected));
            initPalette();
        }
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

        paletteView.panelsAdd(new UITextButton(TXDB.get("New"), FontSizes.schemaButtonTextHeight, AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to create a new image? This will unload the previous image, destroying unsaved changes."), new Runnable() {
            @Override
            public void run() {
                imageEditView.setImage(new ImageEditorImage(32, 32));
                initPalette();
            }
        })));

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
        for (final ImageIOFormat format : ImageIOFormat.supportedFormats) {
            String tx = format.saveName(imageEditView.image);
            if (tx == null)
                continue;
            UITextButton button = new UITextButton(tx, FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
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
                }, !imageEditView.image.t1Lock));
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
                        imageEditView.setImage(new ImageEditorImage(rect.width, rect.height, newImage, imageEditView.image.palette, imageEditView.image.t1Lock));
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

        UIElement cSwitch;

        // It's like Life Is Strange.
        // No matter which option you choose, we'll question your decision.
        if (imageEditView.image.palette != null) {
            final UITextButton ck = new UITextButton(TXDB.get("Colourkey"), FontSizes.schemaButtonTextHeight, null).togglable(imageEditView.image.t1Lock);
            ck.onClick = new Runnable() {
                @Override
                public void run() {
                    imageEditView.setImage(new ImageEditorImage(imageEditView.image, ck.state));
                    initPalette();
                }
            };
            if (!ck.state)
                ck.onClick = AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to enable Colourkey mode? This mode does not allow partial alpha, and the first colour in the palette becomes transparent."), ck.onClick);
            cSwitch = new UISplitterLayout(ck, new UITextButton(TXDB.get("-> 32-bit ARGB"), FontSizes.schemaButtonTextHeight, AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to switch to 32-bit ARGB? The image will no longer contain a palette, which may make editing inconvenient, and some formats will become unavailable."), new Runnable() {
                @Override
                public void run() {
                    ImageEditorImage wip = new ImageEditorImage(imageEditView.image, false, false);
                    imageEditView.setImage(wip);
                    initPalette();
                }
            })), false, 0.5d);
        } else {
            cSwitch = new UITextButton(TXDB.get("Use Palette"), FontSizes.schemaButtonTextHeight, AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to switch to using a palette? If an exceptional number of colours are used, the image may be hard to edit."), new Runnable() {
                @Override
                public void run() {
                    ImageEditorImage wip = new ImageEditorImage(imageEditView.image, false, true);
                    imageEditView.setImage(wip);
                    initPalette();
                }
            }));
        }
        paletteView.panelsAdd(cSwitch);

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

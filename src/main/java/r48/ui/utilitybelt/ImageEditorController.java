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
    public IConsumer<UIElement> windowMaker;

    // This holds a bit of state, so let's just attach it/detach it as we want
    private final UITextButton sanityButton = new UITextButton(TXDB.get("Adjust"), FontSizes.schemaButtonTextHeight, null).togglable(true);
    // The current thing holding the sanity button (needed so it can be broken apart on UI rebuild)
    private UISplitterLayout sanityButtonHolder = null;

    public ImageEditorController(IConsumer<UIElement> worldMachine) {
        windowMaker = worldMachine;
        imageEditView = new UIImageEditView(new RootImageEditorTool(), new Runnable() {
            @Override
            public void run() {
                while (true) {
                    IImageEditorTool tool = imageEditView.currentTool;
                    imageEditView.currentTool.enter(imageEditView);
                    if (tool == imageEditView.currentTool)
                        break;
                }
                initPalette();
            }
        });
        paletteView = new UIScrollLayout(true, FontSizes.generalScrollersize);
        initPalette();
        rootView = new UISplitterLayout(imageEditView, paletteView, false, 1.0d) {
            @Override
            public String toString() {
                if (imageEditView.eds.imageModified())
                    return TXDB.get("Image Editor (modified)");
                return TXDB.get("Image Editor");
            }
        };
    }

    public boolean imageModified() {
        return imageEditView.eds.imageModified();
    }

    public void save() {
        // Used by AppMain for Save All Modified (...)
        if (!imageEditView.eds.canSimplySave()) {
            AppMain.launchDialog(TXDB.get("While the image editor contents would be saved, there's nowhere to save them."));
            return;
        }
        // Save to existing location
        try {
            imageEditView.eds.simpleSave();
        } catch (Exception e) {
            e.printStackTrace();
            AppMain.launchDialog(TXDB.get("Failed to save.") + "\n" + e);
        }
        AppMain.performFullImageFlush();
    }

    private void load(String filename) {
        GaBIEn.hintFlushAllTheCaches();
        ImageIOFormat.TryToLoadResult ioi = ImageIOFormat.tryToLoad(filename, ImageIOFormat.supportedFormats);
        if (ioi == null) {
            AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Failed to load #A."), new RubyIO().setString(filename, true)));
        } else {
            // Detect assets that use the tRNS chunk correctly
            boolean detected = false;
            if (ioi.iei.palette != null) {
                if ((ioi.iei.palette.get(0) & 0xFF000000) == 0) {
                    detected = true;
                    for (int i = 1; i < ioi.iei.palette.size(); i++) {
                        if ((ioi.iei.palette.get(i) & 0xFF000000) != 0xFF000000) {
                            detected = false;
                            break;
                        }
                    }
                }
            }
            imageEditView.setImage(new ImageEditorImage(ioi.iei, detected));
            imageEditView.eds.didSuccessfulLoad(filename, ioi.format);
            initPalette();
        }
    }

    private void initPalette() {
        paletteView.panelsClear();
        if (sanityButtonHolder != null) {
            sanityButtonHolder.release();
            sanityButtonHolder = null;
        }
        final String fbStrAL = TXDB.get("Load");
        final String fbStrAS = TXDB.get("Save");

        UIElement ul = new UITextButton(imageEditView.image.width + "x" + imageEditView.image.height, FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                showXYChanger(new Rect(0, 0, imageEditView.image.width, imageEditView.image.height), new IConsumer<Rect>() {
                    @Override
                    public void accept(Rect rect) {
                        imageEditView.eds.startSection();
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
                        imageEditView.setImage(new ImageEditorImage(rect.width, rect.height, newImage, imageEditView.image.palette, imageEditView.image.t1Lock));
                        imageEditView.eds.endSection();
                    }
                }, TXDB.get("Resize..."));
            }
        });
        ul = new UIAppendButton(TXDB.get("New"), ul, AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to create a new image? This will unload the previous image, destroying unsaved changes."), new Runnable() {
            @Override
            public void run() {
                imageEditView.setImage(new ImageEditorImage(imageEditView.image.width, imageEditView.image.height));
                imageEditView.eds.newFile();
                initPalette();
            }
        }), FontSizes.schemaButtonTextHeight);
        ul = new UIAppendButton(TXDB.get("Open"), ul, new Runnable() {
            @Override
            public void run() {
                GaBIEn.startFileBrowser(fbStrAL, false, "", new IConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        if (s != null)
                            load(s);
                    }
                });
            }
        }, FontSizes.schemaButtonTextHeight);
        boolean canDoNormalSave = imageEditView.eds.canSimplySave();
        if (canDoNormalSave) {
            ul = new UIAppendButton(TXDB.get("Save"), ul, new Runnable() {
                @Override
                public void run() {
                    save();
                }
            }, FontSizes.schemaButtonTextHeight);
        }
        ul = new UIAppendButton(TXDB.get("Save As"), ul, new Runnable() {
            @Override
            public void run() {
                LinkedList<String> items = new LinkedList<String>();
                LinkedList<Runnable> runnables = new LinkedList<Runnable>();
                for (final ImageIOFormat format : ImageIOFormat.supportedFormats) {
                    String tx = format.saveName(imageEditView.image);
                    if (tx == null)
                        continue;
                    items.add(tx);
                    runnables.add(new Runnable() {
                        @Override
                        public void run() {
                            GaBIEn.startFileBrowser(fbStrAS, true, "", new IConsumer<String>() {
                                @Override
                                public void accept(String s) {
                                    if (s != null) {
                                        try {
                                            if (format.saveName(imageEditView.image) == null)
                                                throw new Exception("Became unable to save file between dialog launch and confirmation");
                                            byte[] data = format.saveFile(imageEditView.image);
                                            OutputStream os = GaBIEn.getOutFile(s);
                                            os.write(data);
                                            os.close();
                                            imageEditView.eds.didSuccessfulSave(s, format);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Failed to save #A.") + "\n" + e, new RubyIO().setString(s, true)));
                                        }
                                        AppMain.performFullImageFlush();
                                        initPalette();
                                    }
                                }
                            });
                        }
                    });
                }
                windowMaker.accept(new UIAutoclosingPopupMenu(items.toArray(new String[0]), runnables.toArray(new Runnable[0]), FontSizes.menuTextHeight, FontSizes.menuScrollersize, true));
            }
        }, FontSizes.schemaButtonTextHeight);
        paletteView.panelsAdd(ul);

        ul = new UISplitterLayout(new UITextButton(TXDB.get("Grid Size"), FontSizes.schemaButtonTextHeight, new Runnable() {
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
        }), false, 0.5d);

        ul = new UIAppendButton(TXDB.get("Undo"), ul, new Runnable() {
            @Override
            public void run() {
                if (imageEditView.eds.hasUndo()) {
                    imageEditView.setImage(imageEditView.eds.performUndo());
                    initPalette();
                } else {
                    AppMain.launchDialog(TXDB.get("There is nothing to undo."));
                }
            }
        }, FontSizes.schemaButtonTextHeight);

        ul = new UIAppendButton(TXDB.get("Redo"), ul, new Runnable() {
            @Override
            public void run() {
                if (imageEditView.eds.hasRedo()) {
                    imageEditView.setImage(imageEditView.eds.performRedo());
                    initPalette();
                } else {
                    AppMain.launchDialog(TXDB.get("There is nothing to redo."));
                }
            }
        }, FontSizes.schemaButtonTextHeight);
        paletteView.panelsAdd(ul);

        UIAppendButton ap = new UIAppendButton(TXDB.get("Grid Overlay"), new UITextButton(TXDB.get("Reset View"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                imageEditView.camX = 0;
                imageEditView.camY = 0;
                imageEditView.tiling = null;
            }
        }), new Runnable() {
            @Override
            public void run() {
                imageEditView.gridST = !imageEditView.gridST;
            }
        }, FontSizes.schemaButtonTextHeight);
        ap.button.togglable(imageEditView.gridST);
        paletteView.panelsAdd(ap);

        paletteView.panelsAdd(imageEditView.currentTool.createToolPalette(imageEditView));

        // mode details
        UILabel cType = new UILabel(imageEditView.image.describeColourFormat(), FontSizes.schemaFieldTextHeight);
        if (imageEditView.image.usesPalette()) {
            paletteView.panelsAdd(sanityButtonHolder = new UISplitterLayout(cType, sanityButton, false, 1));
        } else {
            paletteView.panelsAdd(cType);
        }

        paletteView.panelsAdd(new UISplitterLayout(new UITextButton(TXDB.get("Add Colour"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                windowMaker.accept(new UIColourPicker(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == null)
                            return;
                        imageEditView.eds.startSection();
                        imageEditView.image.appendToPalette(integer);
                        imageEditView.eds.endSection();
                        initPalette();
                    }
                }, !imageEditView.image.t1Lock));
            }
        }), new UITextButton(TXDB.get("From Image"), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                imageEditView.currentTool = new AddColourFromImageEditorTool(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        imageEditView.eds.startSection();
                        imageEditView.image.appendToPalette(integer);
                        imageEditView.eds.endSection();
                    }
                });
                imageEditView.newToolCallback.run();
            }
        }), false, 0.5d));


        UIElement cSwitch;

        // It's like Life Is Strange.
        // No matter which option you choose, we'll question your decision.
        if (imageEditView.image.palette != null) {
            final UITextButton ck = new UITextButton(TXDB.get("Colourkey"), FontSizes.schemaButtonTextHeight, null).togglable(imageEditView.image.t1Lock);
            ck.onClick = new Runnable() {
                @Override
                public void run() {
                    imageEditView.eds.startSection();
                    imageEditView.setImage(new ImageEditorImage(imageEditView.image, ck.state));
                    imageEditView.eds.endSection();
                    initPalette();
                }
            };
            if (!ck.state)
                ck.onClick = AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to enable Colourkey mode? This mode does not allow partial alpha, and the first colour in the palette becomes transparent."), ck.onClick);
            cSwitch = new UISplitterLayout(ck, new UITextButton(TXDB.get("-> 32-bit ARGB"), FontSizes.schemaButtonTextHeight, AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to switch to 32-bit ARGB? The image will no longer contain a palette, which may make editing inconvenient, and some formats will become unavailable."), new Runnable() {
                @Override
                public void run() {
                    imageEditView.eds.startSection();
                    ImageEditorImage wip = new ImageEditorImage(imageEditView.image, false, false);
                    imageEditView.setImage(wip);
                    imageEditView.eds.endSection();
                    initPalette();
                }
            })), false, 0.5d);
        } else {
            cSwitch = new UITextButton(TXDB.get("Use Palette"), FontSizes.schemaButtonTextHeight, AppMain.createLaunchConfirmation(TXDB.get("Are you sure you want to switch to using a palette? If an exceptional number of colours are used, the image may be hard to edit."), new Runnable() {
                @Override
                public void run() {
                    imageEditView.eds.startSection();
                    ImageEditorImage wip = new ImageEditorImage(imageEditView.image, false, true);
                    imageEditView.setImage(wip);
                    imageEditView.eds.endSection();
                    initPalette();
                }
            }));
        }
        paletteView.panelsAdd(cSwitch);

        for (int idx = 0; idx < imageEditView.image.paletteSize(); idx++) {
            final int fidx = idx;
            UIElement cPanel = new UIColourSwatch(imageEditView.image.getPaletteRGB(idx));
            if (imageEditView.selPaletteIndex == fidx) {
                cPanel = new UIAppendButton("X", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        if (imageEditView.image.paletteSize() > 1) {
                            imageEditView.eds.startSection();
                            imageEditView.selPaletteIndex--;
                            if (imageEditView.selPaletteIndex == -1)
                                imageEditView.selPaletteIndex++;
                            imageEditView.image.removeFromPalette(fidx, sanityButton.state);
                            imageEditView.eds.endSection();
                            initPalette();
                        }
                    }
                }, FontSizes.schemaButtonTextHeight);
            } else {
                cPanel = new UIAppendButton("~", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        imageEditView.eds.startSection();
                        imageEditView.image.swapInPalette(imageEditView.selPaletteIndex, fidx, sanityButton.state);
                        imageEditView.eds.endSection();
                        initPalette();
                    }
                }, FontSizes.schemaButtonTextHeight);
            }
            cPanel = new UISplitterLayout(new UITextButton("<", FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    if (fidx != imageEditView.selPaletteIndex) {
                        imageEditView.selPaletteIndex = fidx;
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

        res.forceToRecommended();
        windowMaker.accept(res);
    }
}

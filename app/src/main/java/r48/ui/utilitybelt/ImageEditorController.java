/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;
import r48.RubyIO;
import r48.imageio.ImageIOFormat;
import r48.maptools.UIMTBase;
import r48.ui.UIAppendButton;
import r48.ui.UIColourSwatchButton;
import r48.ui.UIMenuButton;
import r48.ui.Art.Symbol;
import r48.ui.UISymbolButton;
import r48.ui.dialog.UIColourPicker;
import r48.ui.dmicg.CharacterGeneratorController;

import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Oh, this can't be good news.
 * - 7th October, 2017
 */
public class ImageEditorController extends App.Svc {
    public UISplitterLayout rootView;
    private UIImageEditView imageEditView;
    private UIScrollLayout paletteView;

    // This holds a bit of state, so let's just attach it/detach it as we want
    private final UITextButton sanityButton = new UITextButton(app.ts("Adjust"), app.f.schemaFieldTextHeight, null).togglable(true);
    // The current thing holding the sanity button (needed so it can be broken apart on UI rebuild)
    private UISplitterLayout sanityButtonHolder = null;

    // Used by inner runnables
    private UIElement fileButtonMenuHook;
    private Object paletteThing;

    // Warnings
    private boolean hasWarnedUserAboutRM;

    public ImageEditorController(App app) {
        super(app);
        imageEditView = new UIImageEditView(app, new RootImageEditorTool(app), new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ImageEditorTool tool = imageEditView.currentTool;
                    imageEditView.currentTool.forceDifferentTool(imageEditView);
                    if (tool == imageEditView.currentTool)
                        break;
                }
                initPalette(0);
            }
        });
        paletteView = new UIScrollLayout(true, app.f.generalScrollersize);
        initPalette(0);
        rootView = new UISplitterLayout(imageEditView, paletteView, false, 1.0d) {
            @Override
            public String toString() {
                if (imageEditView.eds.imageModified())
                    return app.ts("Image Editor (modified)"); // + " " + imageEditView.eds.getSaveDepth();
                return app.ts("Image Editor"); // + " " + imageEditView.eds.getSaveDepth();
            }

            @Override
            public void onWindowClose() {
                super.onWindowClose();
                app.ui.imgContext.remove(ImageEditorController.this);
            }
        };
        app.ui.imgContext.add(this);
    }

    public boolean imageModified() {
        return imageEditView.eds.imageModified();
    }

    public void save() {
        // Used by AppMain for Save All Modified (...)
        if (!imageEditView.eds.canSimplySave()) {
            app.ui.launchDialog(app.ts("While the image editor contents would be saved, there's nowhere to save them."));
            return;
        }
        // Save to existing location
        try {
            imageEditView.eds.simpleSave();
        } catch (Exception e) {
            e.printStackTrace();
            app.ui.launchDialog(app.ts("Failed to save.") + "\n" + e);
        }
        app.ui.performFullImageFlush();
    }

    private void load(String filename) {
        GaBIEn.hintFlushAllTheCaches();
        ImageIOFormat.TryToLoadResult ioi = ImageIOFormat.tryToLoad(filename, app.imageIOFormats);
        if (ioi == null) {
            app.ui.launchDialog(app.fmt.formatExtended(app.ts("Failed to load #A."), new RubyIO().setString(filename, true)));
        } else {
            boolean detectedCK = false;
            if (ioi.wouldKnowIfColourKey) {
                // Detect assets that use the tRNS chunk correctly
                if (ioi.iei.palette != null) {
                    if ((ioi.iei.palette.get(0) & 0xFF000000) == 0) {
                        detectedCK = true;
                        for (int i = 1; i < ioi.iei.palette.size(); i++) {
                            if ((ioi.iei.palette.get(i) & 0xFF000000) != 0xFF000000) {
                                detectedCK = false;
                                break;
                            }
                        }
                    }
                }
            } else {
                // Heuristics time
                String filenameL = filename.toLowerCase().replace('\\', '/');
                detectedCK |= filenameL.contains("/battle/");
                detectedCK |= filenameL.contains("/battle2/");
                detectedCK |= filenameL.contains("/battlecharset/");
                detectedCK |= filenameL.contains("/battleweapon/");
                detectedCK |= filenameL.contains("/charset/");
                detectedCK |= filenameL.contains("/chipset/");
                detectedCK |= filenameL.contains("/faceset/");
                detectedCK |= filenameL.contains("/frame/");
                detectedCK |= filenameL.contains("/monster/");
                detectedCK |= filenameL.contains("/picture/");
                detectedCK |= filenameL.contains("/system/");
                detectedCK |= filenameL.contains("/system2/");
            }
            imageEditView.setImage(new ImageEditorImage(ioi.iei, detectedCK));
            imageEditView.eds.didSuccessfulLoad(filename, ioi.format);
            initPalette(0);
            Size sz = new Size(ioi.iei.width, ioi.iei.height);
            final Rect potentialGrid = app.system.getIdealGridForImage(filename, sz);
            if (potentialGrid != null) {
                if (!potentialGrid.rectEquals(imageEditView.grid)) {
                    app.ui.createLaunchConfirmation(app.ts("Change grid to suit this asset?"), new Runnable() {
                        @Override
                        public void run() {
                            imageEditView.grid = potentialGrid;
                        }
                    }).run();
                }
            }
        }
    }

    // 0: Any 1: Undo 2: Redo 3: New
    private void initPalette(int cause) {
        final Object currentPaletteThing = paletteThing = new Object();

        paletteView.panelsClear();
        if (sanityButtonHolder != null) {
            sanityButtonHolder.release();
            sanityButtonHolder = null;
        }
        final String fbStrAL = app.ts("Open");
        final String fbStrAS = app.ts("Save");

        LinkedList<String> menuDetails = new LinkedList<String>();
        LinkedList<Runnable> menuFuncs = new LinkedList<Runnable>();

        menuDetails.add(imageEditView.image.width + "x" + imageEditView.image.height);
        menuFuncs.add(new Runnable() {
            @Override
            public void run() {
                app.ui.wm.createMenu(fileButtonMenuHook, showXYChanger(new Rect(0, 0, imageEditView.image.width, imageEditView.image.height), new IConsumer<Rect>() {
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
                        initPalette(0);
                    }
                }, app.ts("Resize...")));
            }
        });
        if (imageEditView.image.usesPalette()) {
            menuDetails.add(app.ts("Indexed"));
            menuFuncs.add(app.ui.createLaunchConfirmation(app.ts("Are you sure you want to switch to 32-bit ARGB? The image will no longer contain a palette, which may make editing inconvenient, and some formats will become unavailable."), new Runnable() {
                @Override
                public void run() {
                    imageEditView.eds.startSection();
                    ImageEditorImage wip = new ImageEditorImage(imageEditView.image, false, false);
                    imageEditView.setImage(wip);
                    imageEditView.eds.endSection();
                    initPalette(0);
                }
            }));
            if (imageEditView.image.t1Lock) {
                menuDetails.add(app.ts("Colourkey On"));
            } else {
                menuDetails.add(app.ts("Colourkey Off"));
            }
            menuFuncs.add(new Runnable() {
                @Override
                public void run() {
                    imageEditView.eds.startSection();
                    imageEditView.setImage(new ImageEditorImage(imageEditView.image, !imageEditView.image.t1Lock));
                    imageEditView.eds.endSection();
                    initPalette(0);
                }
            });
        } else {
            menuDetails.add(app.ts("ARGB (32-bit)"));
            menuFuncs.add(app.ui.createLaunchConfirmation(app.ts("Are you sure? This will create a palette entry for every colour in the image."), new Runnable() {
                @Override
                public void run() {
                    imageEditView.eds.startSection();
                    ImageEditorImage wip = new ImageEditorImage(imageEditView.image, false, true);
                    imageEditView.setImage(wip);
                    imageEditView.eds.endSection();
                    initPalette(0);
                }
            }));
        }
        menuDetails.add(app.ts("New"));
        menuFuncs.add(new Runnable() {
            @Override
            public void run() {
                Runnable actualCore = new Runnable() {
                    @Override
                    public void run() {
                        imageEditView.setImage(new ImageEditorImage(imageEditView.image.width, imageEditView.image.height));
                        imageEditView.eds.newFile();
                        initPalette(3);
                        app.ui.launchDialog(app.ts("New image created (same size as the previous image)"));
                    }
                };
                if (imageEditView.eds.imageModified())
                    actualCore = app.ui.createLaunchConfirmation(app.ts("Are you sure you want to create a new image? This will unload the previous image, destroying unsaved changes."), actualCore);
                actualCore.run();
            }
        });
        menuDetails.add(fbStrAL);
        menuFuncs.add(new Runnable() {
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
        });
        boolean canDoNormalSave = imageEditView.eds.canSimplySave();
        if (canDoNormalSave) {
            menuDetails.add(fbStrAS);
            menuFuncs.add(addPresaveWarningWrapper(new Runnable() {
                @Override
                public void run() {
                    save();
                }
            }));
        }
        menuDetails.add(app.ts("Save As"));
        menuFuncs.add(addPresaveWarningWrapper(new Runnable() {
            @Override
            public void run() {
                LinkedList<String> items = new LinkedList<String>();
                LinkedList<Runnable> runnables = new LinkedList<Runnable>();
                for (final ImageIOFormat format : app.imageIOFormats) {
                    String tx = format.saveName(imageEditView.image);
                    if (tx == null)
                        continue;
                    items.add(tx);
                    runnables.add(new Runnable() {
                        @Override
                        public void run() {
                            String initialName = "";
                            String sss = imageEditView.eds.getSimpleSaveTarget();
                            if (sss != null) {
                                GaBIEn.setBrowserDirectory(GaBIEn.absolutePathOf(GaBIEn.parentOf(sss)));
                                if (imageEditView.eds.canSimplySave())
                                    initialName = GaBIEn.nameOf(sss);
                            }
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
                                            app.ui.launchDialog(app.fmt.formatExtended(app.ts("Failed to save #A."), new RubyIO().setString(s, true)) + "\n" + e);
                                        }
                                        app.ui.performFullImageFlush();
                                        initPalette(0);
                                    }
                                }
                            }, initialName);
                        }
                    });
                }
                app.ui.wm.createWindow(new UIAutoclosingPopupMenu(items.toArray(new String[0]), runnables.toArray(new Runnable[0]), app.f.menuTextHeight, app.f.menuScrollersize, true));
            }
        }));
        menuDetails.add(app.ts("CharacterGen..."));
        menuFuncs.add(new Runnable() {
            @Override
            public void run() {
                app.ui.wm.createWindow(new CharacterGeneratorController(app).rootView);
            }
        });

        fileButtonMenuHook = new UIMenuButton(app, app.ts("File..."), app.f.imageEditorTextHeight, new ISupplier<Boolean>() {
            @Override
            public Boolean get() {
                return paletteThing == currentPaletteThing;
            }
        }, menuDetails.toArray(new String[0]), menuFuncs.toArray(new Runnable[0]));
        paletteView.panelsAdd(fileButtonMenuHook);

        paletteView.panelsAdd(new UIMenuButton(app, app.ts("Grid..."), app.f.imageEditorTextHeight, new ISupplier<UIElement>() {
            @Override
            public UIElement get() {
                // The grid changer used to be using the same XY changer as resizing, then that became impractical.
                // Probably for the better, this may in some circumstances allow a runtime view...
                UIScrollLayout verticalLayout = new UIScrollLayout(true, app.f.generalScrollersize);
                final UINumberBox gridX, gridY, gridW, gridH;
                gridX = new UINumberBox(imageEditView.grid.x, app.f.imageEditorTextHeight);
                gridY = new UINumberBox(imageEditView.grid.y, app.f.imageEditorTextHeight);
                gridW = new UINumberBox(imageEditView.grid.width, app.f.imageEditorTextHeight);
                gridH = new UINumberBox(imageEditView.grid.height, app.f.imageEditorTextHeight);
                Runnable sendUpdates = new Runnable() {
                    @Override
                    public void run() {
                        imageEditView.grid = new Rect((int) gridX.number, (int) gridY.number, (int) gridW.number, (int) gridH.number);
                    }
                };
                gridX.onEdit = sendUpdates;
                gridY.onEdit = sendUpdates;
                gridW.onEdit = sendUpdates;
                gridH.onEdit = sendUpdates;
                verticalLayout.panelsAdd(new UILabel(app.ts("Grid Size:"), app.f.imageEditorTextHeight));
                verticalLayout.panelsAdd(new UISplitterLayout(gridX, gridY, false, 0.5d));
                verticalLayout.panelsAdd(new UILabel(app.ts("Grid Offset:"), app.f.imageEditorTextHeight));
                verticalLayout.panelsAdd(new UISplitterLayout(gridW, gridH, false, 0.5d));
                // This is the colour of the grid.
                final UIColourSwatchButton uicsb = new UIColourSwatchButton(imageEditView.gridColour, app.f.imageEditorTextHeight, null);
                uicsb.onClick = new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createMenu(uicsb, new UIColourPicker(app, app.ts("Grid Colour"), imageEditView.gridColour, new IConsumer<Integer>() {
                            @Override
                            public void accept(Integer t) {
                                if (t != null)
                                    imageEditView.gridColour = t & 0xFFFFFF;
                            }
                        }, false));
                    }
                };
                verticalLayout.panelsAdd(uicsb);
                // Finally, grid overlay control.
                verticalLayout.panelsAdd(new UITextButton(app.ts("Overlay"), app.f.imageEditorTextHeight, new Runnable() {
                    @Override
                    public void run() {
                        imageEditView.gridST = !imageEditView.gridST;
                    }
                }).togglable(imageEditView.gridST));
                // And finish.
                verticalLayout.forceToRecommended();
                return verticalLayout;
            }
        }));

        UIElement ul = new UISymbolButton(Symbol.Target, app.f.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                imageEditView.camX = 0;
                imageEditView.camY = 0;
                imageEditView.tiling = null;
            }
        });

        ul = pokeOnCause(cause, 1, new UIAppendButton(Symbol.Back, ul, new Runnable() {
            @Override
            public void run() {
                if (imageEditView.eds.hasUndo()) {
                    imageEditView.setImage(imageEditView.eds.performUndo());
                    initPalette(1);
                } else {
                    app.ui.launchDialog(app.ts("There is nothing to undo."));
                }
            }
        }, app.f.imageEditorTextHeight));

        ul = pokeOnCause(cause, 2, new UIAppendButton(Symbol.Forward, ul, new Runnable() {
            @Override
            public void run() {
                if (imageEditView.eds.hasRedo()) {
                    imageEditView.setImage(imageEditView.eds.performRedo());
                    initPalette(2);
                } else {
                    app.ui.launchDialog(app.ts("There is nothing to redo."));
                }
            }
        }, app.f.imageEditorTextHeight));

        paletteView.panelsAdd(ul);

        paletteView.panelsAdd(imageEditView.currentTool.createToolPalette(imageEditView));

        // mode details
        if (imageEditView.image.usesPalette()) {
            UILabel cType = new UILabel(app.ts("Pal. "), app.f.imageEditorTextHeight);
            paletteView.panelsAdd(sanityButtonHolder = new UISplitterLayout(cType, sanityButton, false, 1));
        }

        paletteView.panelsAdd(new UISplitterLayout(new UIMenuButton(app, "+", app.f.imageEditorTextHeight, new ISupplier<UIElement>() {
            @Override
            public UIElement get() {
                return new UIColourPicker(app, app.ts("Add Palette Colour..."), imageEditView.image.getPaletteRGB(imageEditView.selPaletteIndex) | 0xFF000000, new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer == null)
                            return;
                        imageEditView.eds.startSection();
                        imageEditView.image.appendToPalette(integer);
                        imageEditView.eds.endSection();
                        initPalette(0);
                    }
                }, !imageEditView.image.t1Lock);
            }
        }), new UISymbolButton(Symbol.Eyedropper, app.f.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                imageEditView.currentTool = new AddColourFromImageEditorTool(app, new IConsumer<Integer>() {
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

        for (int idx = 0; idx < imageEditView.image.paletteSize(); idx++) {
            final int fidx = idx;
            UIElement cPanel = new UIColourSwatchButton(imageEditView.image.getPaletteRGB(idx), app.f.imageEditorTextHeight, new Runnable() {
                @Override
                public void run() {
                    imageEditView.selPaletteIndex = fidx;
                    initPalette(0);
                }
            }).togglable(imageEditView.selPaletteIndex == fidx);
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
                            initPalette(0);
                        }
                    }
                }, app.f.imageEditorTextHeight);
            } else {
                cPanel = new UIAppendButton("~", cPanel, new Runnable() {
                    @Override
                    public void run() {
                        imageEditView.eds.startSection();
                        imageEditView.image.swapInPalette(imageEditView.selPaletteIndex, fidx, sanityButton.state);
                        imageEditView.eds.endSection();
                        initPalette(0);
                    }
                }, app.f.imageEditorTextHeight);
            }
            cPanel = new UISplitterLayout(new UIMenuButton(app, "=", app.f.imageEditorTextHeight, new ISupplier<UIElement>() {
                @Override
                public UIElement get() {
                    return new UIColourPicker(app, app.ts("Change Palette Colour..."), imageEditView.image.getPaletteRGB(fidx), new IConsumer<Integer>() {
                        @Override
                        public void accept(Integer integer) {
                            if (integer == null)
                                return;
                            imageEditView.eds.startSection();
                            if (fidx < imageEditView.image.paletteSize())
                                imageEditView.image.changePalette(fidx, integer);
                            imageEditView.eds.endSection();
                            initPalette(0);
                        }
                    }, true);
                }
            }), cPanel, false, 0.0d);
            paletteView.panelsAdd(cPanel);
        }
        paletteView.runLayoutLoop();
    }

    private Runnable addPresaveWarningWrapper(final Runnable runnable) {
        // Is this image invalid for the engine?
        return new Runnable() {
            @Override
            public void run() {
                if (app.system.engineUsesPal0Colourkeys() && imageEditView.image.usesPalette() && !imageEditView.image.t1Lock) {
                    if (!hasWarnedUserAboutRM) {
                        app.ui.createLaunchConfirmation(app.ts("The engine you're targetting expects indexed-colour images to be colour-keyed. Your image isn't colour-keyed. Continue?"), runnable).run();
                        hasWarnedUserAboutRM = true;
                        return;
                    }
                }
                // By default, just do it
                runnable.run();
            }
        };
    }

    private UIElement pokeOnCause(int cause, int i, UIAppendButton redo) {
        if (cause == i)
            redo.button.enableStateForClick();
        return redo;
    }

    private UIElement showXYChanger(Rect targetVal, final IConsumer<Rect> iConsumer, final String title) {
        UIScrollLayout xyChanger = new UIScrollLayout(true, app.f.generalScrollersize) {
            @Override
            public String toString() {
                return title;
            }
        };
        final UIMTBase res = UIMTBase.wrap(null, xyChanger);
        final UINumberBox wVal, hVal, xVal, yVal;
        final UITextButton acceptButton;
        wVal = new UINumberBox(targetVal.width, app.f.imageEditorTextHeight);
        hVal = new UINumberBox(targetVal.height, app.f.imageEditorTextHeight);
        xVal = new UINumberBox(targetVal.x, app.f.imageEditorTextHeight);
        yVal = new UINumberBox(targetVal.y, app.f.imageEditorTextHeight);
        hVal.onEdit = wVal.onEdit = new Runnable() {
            @Override
            public void run() {
                wVal.number = Math.max(1, wVal.number);
                hVal.number = Math.max(1, hVal.number);
            }
        };

        acceptButton = new UITextButton(app.ts("Accept"), app.f.imageEditorTextHeight, new Runnable() {
            @Override
            public void run() {
                Rect r = new Rect((int) xVal.number, (int) yVal.number, Math.max((int) wVal.number, 1), Math.max((int) hVal.number, 1));
                iConsumer.accept(r);
                res.selfClose = true;
            }
        });
        xyChanger.panelsAdd(new UILabel(app.ts("Size"), app.f.imageEditorTextHeight));
        xyChanger.panelsAdd(new UISplitterLayout(wVal, hVal, false, 1, 2));
        xyChanger.panelsAdd(new UILabel(app.ts("Offset"), app.f.imageEditorTextHeight));
        xyChanger.panelsAdd(new UISplitterLayout(xVal, yVal, false, 1, 2));
        xyChanger.panelsAdd(new UIAppendButton(app.ts("Cancel"), acceptButton, new Runnable() {
            @Override
            public void run() {
                res.selfClose = true;
            }
        }, app.f.imageEditorTextHeight));

        res.forceToRecommended();
        Size tgtSize = res.getSize();
        res.setForcedBounds(null, new Rect(0, 0, tgtSize.width * 3, tgtSize.height));
        return res;
    }
}

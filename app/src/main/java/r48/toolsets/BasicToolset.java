/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets;

import gabien.GaBIEn;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.*;
import r48.dbs.FormatSyntax;
import r48.dbs.ObjectInfo;
import r48.dbs.TXDB;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.map.systems.IRMMapSystem;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.schema.util.SchemaPath;
import r48.toolsets.utils.UITestGraphicsStuff;
import r48.ui.Coco;
import r48.ui.UIAppendButton;
import r48.ui.UIMenuButton;
import r48.ui.audioplayer.UIAudioPlayer;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.dialog.UITextPrompt;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.spacing.UIBorderedSubpanel;

import java.io.*;
import java.util.*;

/**
 * Provides some basic tools for changing the configuration of R48 and doing various bits and pieces.
 * Note that the "rebuild UI" button was removed - the UI (specifically MapInfos right now)
 * attaches modification notifiers which can't go away until the whole ObjectDB is gone.
 * Better to avoid potential leaks by just freeing everything - it's expected that the tabs survive for the lifetime of the application,
 * that is, until shutdown is called on AppMain which disconnects everything that could possibly cause such a leak.
 * Created on 04/06/17.
 */
public class BasicToolset extends App.Svc implements IToolset {

    public BasicToolset(App app) {
        super(app);
    }

    @Override
    public UIElement[] generateTabs() {
        UIElement menu4 = new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(TXDB.get("R48 Version"), FontSizes.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                Coco.launch();
            }
        }).centred(), FontSizes.menuTextHeight), new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(TXDB.get("Help"), FontSizes.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.startHelp(null, "0");
            }
        }).centred(), FontSizes.menuTextHeight), new UIBorderedSubpanel(new UITextButton(TXDB.get("Configuration"), FontSizes.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.wm.createWindow(new UIFontSizeConfigurator());
            }
        }).centred(), FontSizes.menuTextHeight), false, 0.5), false, 0.333333);
        UIElement menu5 = new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(TXDB.get("Image Editor"), FontSizes.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.startImgedit();
            }
        }).centred(), FontSizes.menuTextHeight), new UISplitterLayout(new UIBorderedSubpanel(createODBRMGestalt(), FontSizes.menuTextHeight), new UIBorderedSubpanel(createOtherButton(), FontSizes.menuTextHeight), false, 0.5), false, 1d / 3d);

        UISplitterLayout menu6 = new UISplitterLayout(menu5, createInitialHelp(), true, 0.5);

        UISplitterLayout menu3 = new UISplitterLayout(menu4, menu6, true, 1d / 3d);

        UISplitterLayout menu8 = new UISplitterLayout(menu3, createStatusBar(app), true, 1);

        UIBorderedSubpanel menu3b = new UIBorderedSubpanel(menu8, FontSizes.schemaFieldTextHeight * 4);

        UIElement menu2 = new UISplitterLayout(menu3b, new UIObjectDBMonitor(), true, 1) {
            @Override
            public String toString() {
                return TXDB.get("System Tools");
            }
        };

        UIElement fl = makeFileList();
        if (fl != null)
            return new UIElement[] {
                    menu2,
                    fl
            };
        return new UIElement[] {
                menu2
        };
    }

    private UIElement createInitialHelp() {
        UIHelpSystem uhs = new UIHelpSystem();
        final HelpSystemController hsc = new HelpSystemController(null, "Help/Tips/Entry", uhs);
        Date dt = new Date();
        @SuppressWarnings("deprecation")
        int h = dt.getHours();
        if (h < 7) {
            hsc.accept("Help/Tips/Secret:" + (990 + new Random().nextInt(10)));
        } else {
            hsc.loadPage(new Random().nextInt(10));
        }
        uhs.onLinkClick = hsc;
        return uhs;
    }

    private UIElement createODBRMGestalt() {
        if (app.system instanceof IRMMapSystem) {
            return new UISplitterLayout(createODBButton(), new RMTools(app).genButton(), true, 0.5);
        } else {
            return createODBButton();
        }
    }

    private UIElement createODBButton() {
        return new UIMenuButton(app, TXDB.get("Object Access"), FontSizes.menuTextHeight, null, new String[] {
                TXDB.get("Edit Object"),
                TXDB.get("Autocorrect Object By Name And Schema"),
                TXDB.get("Inspect Object (no Schema needed)"),
                TXDB.get("Object-Object Comparison"),
                TXDB.get("Retrieve all object strings"),
                TXDB.get("PRINT.txt Into Object"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject rio = AppMain.objectDB.getObject(s);
                                if (AppMain.schemas.hasSDBEntry("File." + s)) {
                                    AppMain.launchSchema("File." + s, rio, null);
                                    return;
                                }
                                if (rio != null) {
                                    IRIO r2 = rio.getObject();
                                    if (r2.getType() == 'o') {
                                        if (AppMain.schemas.hasSDBEntry(r2.getSymbol())) {
                                            AppMain.launchSchema(r2.getSymbol(), rio, null);
                                            return;
                                        }
                                    }
                                    app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
                                        @Override
                                        public void accept(String s) {
                                            AppMain.launchSchema(s, rio, null);
                                        }
                                    }));
                                } else {
                                    AppMain.launchDialog(TXDB.get("The file couldn't be read, and there's no schema to create it."));
                                }
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject rio = AppMain.objectDB.getObject(s);
                                app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        SchemaElement ise = AppMain.schemas.getSDBEntry(s);
                                        ise.modifyVal(rio.getObject(), new SchemaPath(ise, rio), false);
                                        AppMain.launchDialog(TXDB.get("OK!"));
                                    }
                                }));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                IObjectBackend.ILoadedObject obj = AppMain.objectDB.getObject(s);
                                if (obj == null) {
                                    AppMain.launchDialog(TXDB.get("The file couldn't be read, and R48 cannot create it."));
                                } else {
                                    app.ui.wm.createWindow(new UITest(obj.getObject()));
                                }
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Source Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject objA = AppMain.objectDB.getObject(s);
                                app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Target Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final IObjectBackend.ILoadedObject objB = AppMain.objectDB.getObject(s);
                                        if ((objA == null) || (objB == null)) {
                                            AppMain.launchDialog(TXDB.get("A file couldn't be read, and R48 cannot create it."));
                                        } else {
                                            try {
                                                OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "objcompareAB.txt"));
                                                byte[] cid = IMIUtils.createIMIData(objA.getObject(), objB.getObject(), "");
                                                if (cid != null)
                                                    os.write(cid);
                                                os.close();
                                                os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "objcompareBA.txt"));
                                                cid = IMIUtils.createIMIData(objB.getObject(), objA.getObject(), "");
                                                if (cid != null)
                                                    os.write(cid);
                                                os.close();
                                                AppMain.launchDialog(TXDB.get("objcompareAB.txt and objcompareBA.txt have been made."));
                                                return;
                                            } catch (Exception e) {
                                                AppMain.launchDialog(TXDB.get("There was an issue somewhere along the line."));
                                            }
                                        }
                                    }
                                }));
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream lm = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "locmaps.txt"));
                            final DataOutputStream dos = new DataOutputStream(lm);
                            final HashSet<String> text = new HashSet<String>();
                            for (String s : app.getAllObjects()) {
                                IObjectBackend.ILoadedObject obj = AppMain.objectDB.getObject(s, null);
                                if (obj != null) {
                                    universalStringLocator(obj.getObject(), new IFunction<IRIO, Integer>() {
                                        @Override
                                        public Integer apply(IRIO rubyIO) {
                                            text.add(rubyIO.decString());
                                            return 1;
                                        }
                                    }, false);
                                }
                            }
                            for (String st : text) {
                                dos.writeBytes("\"");
                                IMIUtils.writeIMIStringBody(dos, st.getBytes("UTF-8"), false);
                                dos.write('\n');
                            }
                            dos.write(';');
                            dos.write('\n');
                            dos.close();
                            if (app.dataPath.equals("Languages/")) {
                                AppMain.launchDialog(TXDB.get("Wrote locmaps.txt (NOTE: You probably don't actually want to do this! Press this in RXP mode to get the CRCs, then go back to this mode to actually start editing stuff.)"));
                            } else {
                                AppMain.launchDialog(TXDB.get("Wrote locmaps.txt"));
                            }
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject rio = AppMain.objectDB.getObject(s);
                                final InputStream is = GaBIEn.getInFile(UITest.getPrintPath());
                                if (rio == null) {
                                    AppMain.launchDialog(TXDB.get("The target file couldn't be read, and there's no schema to create it."));
                                } else if (is == null) {
                                    AppMain.launchDialog(TXDB.get("The PRINT.txt file couldn't be read."));
                                } else {
                                    try {
                                        IRIO irio = rio.getObject();
                                        IMIUtils.runIMISegment(is, irio);
                                        AppMain.objectDB.objectRootModified(rio, new SchemaPath(new OpaqueSchemaElement(), rio));
                                        AppMain.launchDialog(TXDB.get("It is done."));
                                    } catch (Exception ioe) {
                                        try {
                                            is.close();
                                        } catch (Exception ex) {
                                        }
                                        ioe.printStackTrace();
                                        AppMain.launchDialog(TXDB.get("There was an issue somewhere along the line."));
                                    }
                                }
                            }
                        }));
                    }
                }
        }).centred();
    }

    private UIElement createOtherButton() {
        return new UIMenuButton(app, TXDB.get("Other..."), FontSizes.menuTextHeight, null, new String[] {
                TXDB.get("Test Fonts"),
                TXDB.get("Test Graphics Stuff"),
                TXDB.get("Toggle Fullscreen"),
                TXDB.get("Dump Schemaside Translations"),
                TXDB.get("Recover data from R48 error <INCREDIBLY DAMAGING>..."),
                TXDB.get("Audio Player...")
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Font Size?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                try {
                                    Integer i = Integer.parseInt(s);
                                    app.ui.wm.createWindow(new UITextBox("", i).setMultiLine());
                                } catch (Exception e) {
                                    AppMain.launchDialog(TXDB.get("Not a valid number."));
                                }
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITestGraphicsStuff(app));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.toggleFullscreen();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        TXDB.performDump("Lang", "SDB@");
                        TXDB.performDump("Cmtx", "CMDB@");
                        PrintStream psA = null;
                        PrintStream psB = null;
                        try {
                            psA = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "Lang" + TXDB.getLanguage() + ".txt"), false, "UTF-8");
                            psB = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "Cmtx" + TXDB.getLanguage() + ".txt"), false, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        LinkedList<String> t = new LinkedList<String>(TXDB.ssTexts);
                        Collections.sort(t);
                        for (String s : t) {
                            String key = TXDB.stripContext(s);
                            String ctx = s.substring(0, s.length() - (key.length() + 1));
                            if (s.startsWith("SDB@")) {
                                psA.println("x \"" + s.substring(4) + "\"");
                                if (TXDB.has(s)) {
                                    psA.println("y \"" + TXDB.get(ctx, key) + "\"");
                                } else {
                                    psA.println(" TODO");
                                    psA.println("y \"" + key + "\"");
                                }
                            } else if (s.startsWith("CMDB@")) {
                                psB.println("x \"" + s.substring(5) + "\"");
                                if (TXDB.has(s)) {
                                    psB.println("y \"" + TXDB.get(ctx, key) + "\"");
                                } else {
                                    psB.println(" TODO");
                                    psB.println("y \"" + key + "\"");
                                }
                            }
                        }
                        psA.close();
                        psB.close();
                        AppMain.launchDialog(TXDB.get("Wrote Lang and Cmtx files to R48 startup directory (to be put in schema dir.)"));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Safety Confirmation Prompt"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                // Don't translate this, don't lax the restrictions.
                                //  (For a natively English user, the case-sensitivity and punctuation will be enough to stop them.
                                //   And just to clarify, I *am* essentially calling most native speakers of English these days,
                                //   myself included, the kind of people who would consider such a thing to be daunting.)
                                // If they aren't willing to put in the effort to type it, whatever that effort may be,
                                //  then they won't be careful enough using this - and will probably ruin even more of their data.
                                if (s.equals("I understand."))
                                    AppMain.reloadSystemDump();
                            }
                        }));
                        AppMain.launchDialog(TXDB.get("If the backup file is invalid, wasn't created, or is otherwise harmed, this can destroy more data than it saves.") +
                                "\n" + TXDB.get("Check *everything* before a final save.") + "\n" + TXDB.get("Type 'I understand.' at the prompt behind this window if you WILL do this."));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.wm.createWindow(new UITextPrompt(TXDB.get("Filename?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                app.ui.wm.createWindow(UIAudioPlayer.create(s, 1));
                            }
                        }));
                    }
                }
        }).centred();
    }

    private static UIElement createStatusBar(App app) {
        final UILabel uiStatusLabel = new UILabel(TXDB.get("Loading..."), FontSizes.statusBarTextHeight);
        app.uiPendingRunnables.add(new Runnable() {
            @Override
            public void run() {
                // Why throw the full format syntax parser on this? Consistency, plus I can extend this format further if need be.
                uiStatusLabel.text = FormatSyntax.formatExtended(TXDB.get("#A modified. Clipboard: #B"), new RubyIO().setFX(AppMain.objectDB.modifiedObjects.size()), (AppMain.theClipboard == null) ? new RubyIO().setNull() : AppMain.theClipboard);
                app.uiPendingRunnables.add(this);
            }
        });
        UIAppendButton workspace = new UIAppendButton(app, TXDB.get("Clipboard"), uiStatusLabel, null, new String[] {
                TXDB.get("Save Clipboard To 'clip.r48'"),
                TXDB.get("Load Clipboard From 'clip.r48'"),
                TXDB.get("Inspect Clipboard")
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        if (AppMain.theClipboard == null) {
                            AppMain.launchDialog(TXDB.get("There is nothing in the clipboard."));
                        } else {
                            AdHocSaveLoad.save("clip", AppMain.theClipboard);
                            AppMain.launchDialog(TXDB.get("The clipboard was saved."));
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        RubyIO newClip = AdHocSaveLoad.load("clip");
                        if (newClip == null) {
                            AppMain.launchDialog(TXDB.get("The clipboard file is invalid or does not exist."));
                        } else {
                            AppMain.theClipboard = newClip;
                            AppMain.launchDialog(TXDB.get("The clipboard file was loaded."));
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        if (AppMain.theClipboard == null) {
                            AppMain.launchDialog(TXDB.get("There is nothing in the clipboard."));
                        } else {
                            app.ui.wm.createWindow(new UITest(AppMain.theClipboard));
                        }
                    }
                }
        }, FontSizes.statusBarTextHeight);
        workspace = new UIAppendButton(TXDB.get("Quit"), workspace, app.ui.createLaunchConfirmation(TXDB.get("Are you sure you want to return to menu? This will lose unsaved data."), new Runnable() {
            @Override
            public void run() {
                AppMain.pleaseShutdown();
            }
        }), FontSizes.statusBarTextHeight);
        return workspace;
    }

    private static UIElement makeFileList() {
        LinkedList<ObjectInfo> s = AppMain.schemas.listFileDefs();
        if (s.size() == 0)
            return null;
        LinkedList<String> str = new LinkedList<String>();
        LinkedList<Runnable> r = new LinkedList<Runnable>();
        for (final ObjectInfo s2 : s) {
            str.add(s2.toString());
            r.add(new Runnable() {
                @Override
                public void run() {
                    AppMain.launchSchema(s2.schemaName, AppMain.objectDB.getObject(s2.idName), null);
                }
            });
        }
        return new UIPopupMenu(str.toArray(new String[0]), r.toArray(new Runnable[0]), FontSizes.menuTextHeight, FontSizes.menuScrollersize, false) {
            @Override
            public String toString() {
                return TXDB.get("Database Objects");
            }
        };
    }

    public static int universalStringLocator(IRIO rio, IFunction<IRIO, Integer> string, boolean writing) {
        // NOTE: Hash keys, ivar keys are not up for modification.
        int total = 0;
        int type = rio.getType();
        if (type == '"')
            total += string.apply(rio);
        if ((type == '{') || (type == '}'))
            for (IRIO me : rio.getHashKeys())
                total += universalStringLocator(rio.getHashVal(me), string, writing);
        if (type == '[') {
            int arrLen = rio.getALen();
            for (int i = 0; i < arrLen; i++)
                total += universalStringLocator(rio.getAElem(i), string, writing);
        }
        for (String k : rio.getIVars())
            total += universalStringLocator(rio.getIVar(k), string, writing);
        IMagicalBinder b = MagicalBinders.getBinderFor(rio);
        if (b != null) {
            IRIO bound = MagicalBinders.toBoundWithCache(b, rio);
            int c = universalStringLocator(bound, string, writing);
            total += c;
            if (writing)
                if (c != 0)
                    b.applyBoundToTarget(bound, rio);
        }
        return total;
    }
}

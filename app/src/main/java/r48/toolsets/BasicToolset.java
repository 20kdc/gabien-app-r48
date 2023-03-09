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
import r48.app.AppMain;
import r48.dbs.ObjectInfo;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.map.systems.IRMMapSystem;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.schema.util.SchemaPath;
import r48.toolsets.utils.UITestGraphicsStuff;
import r48.ui.UIAppendButton;
import r48.ui.UIMenuButton;
import r48.ui.audioplayer.UIAudioPlayer;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.dialog.UIReadEvaluatePrintLoop;
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
        UIElement menu4 = new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(app.ts("R48 Version"), app.f.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.wm.coco.launch();
            }
        }).centred(), app.f.menuTextHeight), new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(app.ts("Help"), app.f.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.startHelp(null, "0");
            }
        }).centred(), app.f.menuTextHeight), new UIBorderedSubpanel(new UITextButton(app.ts("Configuration"), app.f.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.wm.createWindow(new UIFontSizeConfigurator(app.c, app.t, app.applyConfigChange));
            }
        }).centred(), app.f.menuTextHeight), false, 0.5), false, 0.333333);
        UIElement menu5 = new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(app.ts("Image Editor"), app.f.menuTextHeight, new Runnable() {
            @Override
            public void run() {
                app.ui.startImgedit();
            }
        }).centred(), app.f.menuTextHeight), new UISplitterLayout(new UIBorderedSubpanel(createODBRMGestalt(), app.f.menuTextHeight), new UIBorderedSubpanel(createOtherButton(), app.f.menuTextHeight), false, 0.5), false, 1d / 3d);

        UISplitterLayout menu6 = new UISplitterLayout(menu5, createInitialHelp(), true, 0.5);

        UISplitterLayout menu3 = new UISplitterLayout(menu4, menu6, true, 1d / 3d);

        UISplitterLayout menu8 = new UISplitterLayout(menu3, createStatusBar(app), true, 1);

        UIBorderedSubpanel menu3b = new UIBorderedSubpanel(menu8, app.f.schemaFieldTextHeight * 4);

        UIElement menu2 = new UISplitterLayout(menu3b, new UIObjectDBMonitor(app), true, 1) {
            @Override
            public String toString() {
                return app.ts("System Tools");
            }
        };

        UIElement fl = makeFileList(app);
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
        UIHelpSystem uhs = new UIHelpSystem(app.ilg);
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
        return new UIMenuButton(app, app.ts("Object Access"), app.f.menuTextHeight, null, new String[] {
                app.ts("Edit Object"),
                app.ts("Autocorrect Object By Name And Schema"),
                app.ts("Inspect Object (no Schema needed)"),
                app.ts("Object-Object Comparison"),
                app.ts("Retrieve all object strings"),
                app.ts("PRINT.txt Into Object"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject rio = app.odb.getObject(s);
                                if (app.sdb.hasSDBEntry("File." + s)) {
                                    app.ui.launchSchema("File." + s, rio, null);
                                    return;
                                }
                                if (rio != null) {
                                    IRIO r2 = rio.getObject();
                                    if (r2.getType() == 'o') {
                                        if (app.sdb.hasSDBEntry(r2.getSymbol())) {
                                            app.ui.launchSchema(r2.getSymbol(), rio, null);
                                            return;
                                        }
                                    }
                                    app.ui.launchPrompt(app.ts("Schema ID?"), new IConsumer<String>() {
                                        @Override
                                        public void accept(String s) {
                                            app.ui.launchSchema(s, rio, null);
                                        }
                                    });
                                } else {
                                    app.ui.launchDialog(app.ts("The file couldn't be read, and there's no schema to create it."));
                                }
                            }
                        });
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject rio = app.odb.getObject(s);
                                app.ui.launchPrompt(app.ts("Schema ID?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        SchemaElement ise = app.sdb.getSDBEntry(s);
                                        ise.modifyVal(rio.getObject(), new SchemaPath(ise, rio), false);
                                        app.ui.launchDialog(app.ts("OK!"));
                                    }
                                });
                            }
                        });
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                IObjectBackend.ILoadedObject obj = app.odb.getObject(s);
                                if (obj == null) {
                                    app.ui.launchDialog(app.ts("The file couldn't be read, and R48 cannot create it."));
                                } else {
                                    app.ui.wm.createWindow(new UITest(app, obj.getObject()));
                                }
                            }
                        });
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Source Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject objA = app.odb.getObject(s);
                                app.ui.launchPrompt(app.ts("Target Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final IObjectBackend.ILoadedObject objB = app.odb.getObject(s);
                                        if ((objA == null) || (objB == null)) {
                                            app.ui.launchDialog(app.ts("A file couldn't be read, and R48 cannot create it."));
                                        } else {
                                            try {
                                                OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(app.rootPath + "objcompareAB.txt"));
                                                byte[] cid = IMIUtils.createIMIData(objA.getObject(), objB.getObject(), "");
                                                if (cid != null)
                                                    os.write(cid);
                                                os.close();
                                                os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(app.rootPath + "objcompareBA.txt"));
                                                cid = IMIUtils.createIMIData(objB.getObject(), objA.getObject(), "");
                                                if (cid != null)
                                                    os.write(cid);
                                                os.close();
                                                app.ui.launchDialog(app.ts("objcompareAB.txt and objcompareBA.txt have been made."));
                                                return;
                                            } catch (Exception e) {
                                                app.ui.launchDialog(app.ts("There was an issue somewhere along the line."));
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream lm = GaBIEn.getOutFile(PathUtils.autoDetectWindows(app.rootPath + "locmaps.txt"));
                            final DataOutputStream dos = new DataOutputStream(lm);
                            final HashSet<String> text = new HashSet<String>();
                            for (String s : app.getAllObjects()) {
                                IObjectBackend.ILoadedObject obj = app.odb.getObject(s, null);
                                if (obj != null) {
                                    universalStringLocator(app, obj.getObject(), new IFunction<IRIO, Integer>() {
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
                                app.ui.launchDialog(app.ts("Wrote locmaps.txt (NOTE: You probably don't actually want to do this! Press this in RXP mode to get the CRCs, then go back to this mode to actually start editing stuff.)"));
                            } else {
                                app.ui.launchDialog(app.ts("Wrote locmaps.txt"));
                            }
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Object Name?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                final IObjectBackend.ILoadedObject rio = app.odb.getObject(s);
                                final InputStream is = GaBIEn.getInFile(UITest.getPrintPath(app));
                                if (rio == null) {
                                    app.ui.launchDialog(app.ts("The target file couldn't be read, and there's no schema to create it."));
                                } else if (is == null) {
                                    app.ui.launchDialog(app.ts("The PRINT.txt file couldn't be read."));
                                } else {
                                    try {
                                        IRIO irio = rio.getObject();
                                        IMIUtils.runIMISegment(is, irio);
                                        app.odb.objectRootModified(rio, new SchemaPath(new OpaqueSchemaElement(app), rio));
                                        app.ui.launchDialog(app.ts("It is done."));
                                    } catch (Exception ioe) {
                                        try {
                                            is.close();
                                        } catch (Exception ex) {
                                        }
                                        ioe.printStackTrace();
                                        app.ui.launchDialog(app.ts("There was an issue somewhere along the line."));
                                    }
                                }
                            }
                        });
                    }
                }
        }).centred();
    }

    private UIElement createOtherButton() {
        return new UIMenuButton(app, app.ts("Other..."), app.f.menuTextHeight, null, new String[] {
                app.ts("Test Fonts"),
                app.ts("Test Graphics Stuff"),
                app.ts("Toggle Fullscreen"),
                app.ts("Dump Schemaside Translations"),
                app.ts("Recover data from R48 error <INCREDIBLY DAMAGING>..."),
                app.ts("Audio Player..."),
                app.ts("REPL..."),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Font Size?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                try {
                                    Integer i = Integer.parseInt(s);
                                    app.ui.wm.createWindow(new UITextBox("", i).setMultiLine());
                                } catch (Exception e) {
                                    app.ui.launchDialog(app.ts("Not a valid number."));
                                }
                            }
                        });
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
                        app.performTranslatorDump("Lang", "SDB@");
                        app.performTranslatorDump("Cmtx", "CMDB@");
                        app.ui.launchDialog(app.ts("Wrote Lang and Cmtx files to R48 startup directory (to be put in schema dir.)"));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Safety Confirmation Prompt"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                // Don't translate this, don't lax the restrictions.
                                // If they aren't willing to put in the effort to type it, whatever that effort may be,
                                //  then they won't be careful enough using this - and will probably ruin even more of their data.
                                if (s.equals("I understand."))
                                    AppMain.reloadSystemDump(app);
                            }
                        });
                        app.ui.launchDialog(app.ts("If the backup file is invalid, wasn't created, or is otherwise harmed, this can destroy more data than it saves.") +
                                "\n" + app.ts("Check *everything* before a final save.") + "\n" + app.ts("Type 'I understand.' at the prompt behind this window if you HAVE done this."));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        app.ui.launchPrompt(app.ts("Filename?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                app.ui.wm.createWindow(UIAudioPlayer.create(app, s, 1));
                            }
                        });
                    }
                },
                () -> {
                    String title = app.ts("R48 Application REPL");
                    UIReadEvaluatePrintLoop repl = new UIReadEvaluatePrintLoop(app.c, app.vmCtx, title);
                    app.ui.wm.createWindow(repl);
                }
        }).centred();
    }

    private static UIElement createStatusBar(App app) {
        final UILabel uiStatusLabel = new UILabel(app.ts("Loading..."), app.f.statusBarTextHeight);
        app.uiPendingRunnables.add(new Runnable() {
            @Override
            public void run() {
                // Why throw the full format syntax parser on this? Consistency, plus I can extend this format further if need be.
                IRIO clipGet = (app.theClipboard == null) ? new RubyIO().setNull() : app.theClipboard;
                uiStatusLabel.text = app.fmt.formatExtended(app.ts("#A modified. Clipboard: #B"), new IRIOFixnum(app.odb.modifiedObjects.size()), clipGet);
                app.uiPendingRunnables.add(this);
            }
        });
        UIAppendButton workspace = new UIAppendButton(app, app.ts("Clipboard"), uiStatusLabel, null, new String[] {
                app.ts("Save Clipboard To 'clip.r48'"),
                app.ts("Load Clipboard From 'clip.r48'"),
                app.ts("Inspect Clipboard")
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        if (app.theClipboard == null) {
                            app.ui.launchDialog(app.ts("There is nothing in the clipboard."));
                        } else {
                            AdHocSaveLoad.save("clip", app.theClipboard);
                            app.ui.launchDialog(app.ts("The clipboard was saved."));
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        RubyIO newClip = AdHocSaveLoad.load("clip");
                        if (newClip == null) {
                            app.ui.launchDialog(app.ts("The clipboard file is invalid or does not exist."));
                        } else {
                            app.theClipboard = newClip;
                            app.ui.launchDialog(app.ts("The clipboard file was loaded."));
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        if (app.theClipboard == null) {
                            app.ui.launchDialog(app.ts("There is nothing in the clipboard."));
                        } else {
                            app.ui.wm.createWindow(new UITest(app, app.theClipboard));
                        }
                    }
                }
        }, app.f.statusBarTextHeight);
        workspace = new UIAppendButton(app.ts("Quit"), workspace, app.ui.createLaunchConfirmation(app.ts("Are you sure you want to return to menu? This will lose unsaved data."), new Runnable() {
            @Override
            public void run() {
                app.ui.wm.pleaseShutdown();
            }
        }), app.f.statusBarTextHeight);
        return workspace;
    }

    private static UIElement makeFileList(App app) {
        LinkedList<ObjectInfo> s = app.sdb.listFileDefs();
        if (s.size() == 0)
            return null;
        LinkedList<String> str = new LinkedList<String>();
        LinkedList<Runnable> r = new LinkedList<Runnable>();
        for (final ObjectInfo s2 : s) {
            str.add(s2.toString());
            r.add(new Runnable() {
                @Override
                public void run() {
                    app.ui.launchSchema(s2.schemaName, app.odb.getObject(s2.idName), null);
                }
            });
        }
        return new UIPopupMenu(str.toArray(new String[0]), r.toArray(new Runnable[0]), app.f.menuTextHeight, app.f.menuScrollersize, false) {
            @Override
            public String toString() {
                return app.ts("Database Objects");
            }
        };
    }

    public static int universalStringLocator(App app, IRIO rio, IFunction<IRIO, Integer> string, boolean writing) {
        // NOTE: Hash keys, ivar keys are not up for modification.
        int total = 0;
        int type = rio.getType();
        if (type == '"')
            total += string.apply(rio);
        if ((type == '{') || (type == '}'))
            for (IRIO me : rio.getHashKeys())
                total += universalStringLocator(app, rio.getHashVal(me), string, writing);
        if (type == '[') {
            int arrLen = rio.getALen();
            for (int i = 0; i < arrLen; i++)
                total += universalStringLocator(app, rio.getAElem(i), string, writing);
        }
        for (String k : rio.getIVars())
            total += universalStringLocator(app, rio.getIVar(k), string, writing);
        IMagicalBinder b = MagicalBinders.getBinderFor(app, rio);
        if (b != null) {
            IRIO bound = MagicalBinders.toBoundWithCache(app, b, rio);
            int c = universalStringLocator(app, bound, string, writing);
            total += c;
            if (writing)
                if (c != 0)
                    b.applyBoundToTarget(bound, rio);
        }
        return total;
    }
}

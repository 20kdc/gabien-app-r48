/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets;

import gabien.GaBIEn;
import gabien.natives.BadGPU;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.*;
import r48.app.AppMain;
import r48.dbs.ObjectInfo;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.map.systems.IRMMapSystem;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.schema.util.SchemaPath;
import r48.toolsets.utils.UITestGraphicsStuff;
import r48.tr.pages.TrRoot;
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
        UIElement menu4 = new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(T.u.mR48Version, app.f.menuTH, () -> {
            app.ui.wm.coco.launch();
        }).centred(), app.f.menuTH), new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(T.u.mHelp, app.f.menuTH, () -> {
            app.ui.startHelp(null, "0");
        }).centred(), app.f.menuTH), new UIBorderedSubpanel(new UITextButton(T.u.mConfiguration, app.f.menuTH, () -> {
            app.ui.wm.createWindow(new UIFontSizeConfigurator(app.c, app.t, app.applyConfigChange));
        }).centred(), app.f.menuTH), false, 0.5), false, 0.333333);
        UIElement menu5 = new UISplitterLayout(new UIBorderedSubpanel(new UITextButton(T.u.mImgEdit, app.f.menuTH, () -> {
            app.ui.startImgedit();
        }).centred(), app.f.menuTH), new UISplitterLayout(new UIBorderedSubpanel(createODBRMGestalt(), app.f.menuTH), new UIBorderedSubpanel(createOtherButton(), app.f.menuTH), false, 0.5), false, 1d / 3d);

        UISplitterLayout menu6 = new UISplitterLayout(menu5, createInitialHelp(), true, 0.5);

        UISplitterLayout menu3 = new UISplitterLayout(menu4, menu6, true, 1d / 3d);

        UISplitterLayout menu8 = new UISplitterLayout(menu3, createStatusBar(app), true, 1);

        UIBorderedSubpanel menu3b = new UIBorderedSubpanel(menu8, app.f.schemaFieldTH * 4);

        UIElement menu2 = new UISplitterLayout(menu3b, new UIObjectDBMonitor(app), true, 1) {
            @Override
            public String toString() {
                return T.t.sysTools;
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
        return new UIMenuButton(app, T.u.mObjA, app.f.menuTH, null, new String[] {
                T.u.mEditObj,
                T.u.mCorrectObj,
                T.u.mInspectObj,
                T.u.mDiffObj,
                T.u.mAllStr,
                T.u.mLoadIMI,
                T.u.bts_ramObj,
        }, new Runnable[] {
                () -> {
                    app.ui.launchPrompt(T.u.prObjectName, (s) -> {
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
                            app.ui.launchPrompt(T.u.prSchemaID, new IConsumer<String>() {
                                @Override
                                public void accept(String s) {
                                    app.ui.launchSchema(s, rio, null);
                                }
                            });
                        } else {
                            app.ui.launchDialog(T.u.dFileUnreadableNoSchema);
                        }
                    });
                },
                () -> {
                    app.ui.launchPrompt(T.u.prObjectName, (s) -> {
                        final IObjectBackend.ILoadedObject rio = app.odb.getObject(s);
                        app.ui.launchPrompt(T.u.prSchemaID, new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                SchemaElement ise = app.sdb.getSDBEntry(s);
                                ise.modifyVal(rio.getObject(), new SchemaPath(ise, rio), false);
                                app.ui.launchDialog(T.u.done);
                            }
                        });
                    });
                },
                () -> {
                    app.ui.launchPrompt(T.u.prObjectName, (s) -> {
                        IObjectBackend.ILoadedObject obj = app.odb.getObject(s);
                        if (obj == null) {
                            app.ui.launchDialog(T.u.dFileUnreadable);
                        } else {
                            app.ui.wm.createWindow(new UITest(app, obj.getObject(), obj));
                        }
                    });
                },
                () -> {
                    app.ui.launchPrompt(T.u.prObjectSrc, (sA) -> {
                        final IObjectBackend.ILoadedObject objA = app.odb.getObject(sA);
                        app.ui.launchPrompt(T.u.prObjectDst, (sB) -> {
                            final IObjectBackend.ILoadedObject objB = app.odb.getObject(sB);
                            if ((objA == null) || (objB == null)) {
                                app.ui.launchDialog(T.u.dFileUnreadable);
                            } else {
                                try {
                                    OutputStream os = GaBIEn.getOutFile(AppMain.autoDetectWindows(app.rootPath + "objcompareAB.txt"));
                                    byte[] cid = IMIUtils.createIMIData(objA.getObject(), objB.getObject(), "");
                                    if (cid != null)
                                        os.write(cid);
                                    os.close();
                                    os = GaBIEn.getOutFile(AppMain.autoDetectWindows(app.rootPath + "objcompareBA.txt"));
                                    cid = IMIUtils.createIMIData(objB.getObject(), objA.getObject(), "");
                                    if (cid != null)
                                        os.write(cid);
                                    os.close();
                                    app.ui.launchDialog(T.u.dObjCompare);
                                    return;
                                } catch (Exception e) {
                                    app.ui.launchDialog(e);
                                }
                            }
                        });
                    });
                },
                () -> {
                    try {
                        OutputStream lm = GaBIEn.getOutFile(AppMain.autoDetectWindows(app.rootPath + "locmaps.txt"));
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
                        if (app.engine.dataPath.equals("Languages/")) {
                            app.ui.launchDialog(T.u.osLocmapsWarn);
                        } else {
                            app.ui.launchDialog(T.u.osLocmapsGen);
                        }
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                },
                () -> {
                    app.ui.launchPrompt(T.u.prObjectName, new IConsumer<String>() {
                        @Override
                        public void accept(String s) {
                            final IObjectBackend.ILoadedObject rio = app.odb.getObject(s);
                            final InputStream is = GaBIEn.getInFile(UITest.getPrintPath(app));
                            if (rio == null) {
                                app.ui.launchDialog(T.u.dFileUnreadableNoSchema);
                            } else if (is == null) {
                                app.ui.launchDialog(T.u.dCannotReadPRINT);
                            } else {
                                try {
                                    IRIO irio = rio.getObject();
                                    IMIUtils.runIMISegment(is, irio);
                                    app.odb.objectRootModified(rio, new SchemaPath(new OpaqueSchemaElement(app), rio));
                                    app.ui.launchDialog(T.u.done);
                                } catch (Exception ioe) {
                                    try {
                                        is.close();
                                    } catch (Exception ex) {
                                    }
                                    ioe.printStackTrace();
                                    app.ui.launchDialog(ioe);
                                }
                            }
                        }
                    });
                },
                () -> {
                    IRIOGeneric tmp = new IRIOGeneric(app.encoding);
                    final IObjectBackend.ILoadedObject rio = new IObjectBackend.MockLoadedObject(tmp);
                    app.ui.launchPrompt(T.u.prSchemaID, (s) -> {
                        SchemaElement se = app.sdb.getSDBEntry(s);
                        SchemaPath.setDefaultValue(tmp, se, DMKey.NULL);
                        app.ui.launchSchema(s, rio, null);
                    });
                }
        }).centred();
    }

    private UIElement createOtherButton() {
        return new UIMenuButton(app, T.u.mOther, app.f.menuTH, null, new String[] {
                T.u.mTestFonts,
                T.u.mTestGraphics,
                T.u.mGetGPUInfo,
                T.u.mToggleFull,
                T.u.mSchemaTranslator,
                T.u.mTryRecover,
                T.u.mAudPlay,
                T.u.mREPL,
        }, new Runnable[] {
                () -> {
                    app.ui.launchPrompt(T.u.dlgFontSize, (s) -> {
                        try {
                            Integer i = Integer.parseInt(s);
                            app.ui.wm.createWindow(new UITextBox("", i).setMultiLine());
                        } catch (Exception e) {
                            app.ui.launchDialog(T.u.dlgBadNum);
                        }
                    });
                },
                () -> {
                    app.ui.wm.createWindow(new UITestGraphicsStuff(app));
                },
                () -> {
                    StringBuilder sb = new StringBuilder();
                    try {
                        BadGPU.Instance i = BadGPU.newInstance(BadGPU.NewInstanceFlags.CanPrintf | BadGPU.NewInstanceFlags.BackendCheck);
                        sb.append(i.getMetaInfo(BadGPU.MetaInfoType.Vendor));
                        sb.append("\n");
                        sb.append(i.getMetaInfo(BadGPU.MetaInfoType.Renderer));
                        sb.append("\n");
                        sb.append(i.getMetaInfo(BadGPU.MetaInfoType.Version));
                        sb.append("\n");
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        sb.append(ex.toString());
                    }
                    app.ui.launchDialog(sb.toString());
                },
                () -> {
                    app.ui.wm.toggleFullscreen();
                },
                () -> {
                    GaBIEn.startFileBrowser(T.u.mSchemaTranslator, true, "", (str) -> {
                        if (str == null)
                            return;
                        app.performTranslatorDump(str);
                    });
                },
                () -> {
                    app.ui.launchPrompt(T.t.restoreSafetyConfirm, (s) -> {
                        // Don't translate this, don't lax the restrictions.
                        // If they aren't willing to put in the effort to type it, whatever that effort may be,
                        //  then they won't be careful enough using this - and will probably ruin even more of their data.
                        if (s.equals("I understand."))
                            AppMain.reloadSystemDump(app);
                    });
                    app.ui.launchDialog(T.u.warnRestoreSafety);
                },
                () -> {
                    GaBIEn.startFileBrowser(T.u.openAud, false, "", (res) -> {
                        app.ui.wm.createWindow(UIAudioPlayer.createAbsoluteName(app, res, 1));
                    });
                },
                () -> {
                    String title = T.t.appREPL;
                    UIReadEvaluatePrintLoop repl = new UIReadEvaluatePrintLoop(app.c, app.vmCtx, title);
                    app.ui.wm.createWindow(repl);
                }
        }).centred();
    }

    private static UIElement createStatusBar(App app) {
        final TrRoot T = app.t;
        final UILabel uiStatusLabel = new UILabel("", app.f.statusBarTH);
        // second time I've tried to lambda this - you can't because of the repetition
        app.uiPendingRunnables.add(new Runnable() {
            @Override
            public void run() {
                // Why throw the full format syntax parser on this? Consistency, plus I can extend this format further if need be.
                RORIO clipGet = (app.theClipboard == null) ? new IRIOGeneric(app.encoding) : app.theClipboard;
                String clipText = app.format(clipGet);
                uiStatusLabel.text = T.u.statusLine.r(app.odb.modifiedObjects.size(), clipText);
                app.uiPendingRunnables.add(this);
            }
        });
        UIAppendButton workspace = new UIAppendButton(app, T.u.mClipboard, uiStatusLabel, null, new String[] {
                T.u.mClipSave,
                T.u.mClipLoad,
                T.u.mClipInspect
        }, new Runnable[] {
                () -> {
                    if (app.theClipboard == null) {
                        app.ui.launchDialog(T.u.dlgClipEmpty);
                    } else {
                        AdHocSaveLoad.save("clip", app.theClipboard);
                        app.ui.launchDialog(T.u.dClipSaved);
                    }
                },
                () -> {
                    IRIOGeneric newClip = AdHocSaveLoad.load("clip");
                    if (newClip == null) {
                        app.ui.launchDialog(T.u.dClipBad);
                    } else {
                        app.theClipboard = newClip;
                        app.ui.launchDialog(T.u.dClipLoaded);
                    }
                },
                () -> {
                    if (app.theClipboard == null) {
                        app.ui.launchDialog(T.u.dlgClipEmpty);
                    } else {
                        app.ui.wm.createWindow(new UITest(app, (IRIO) app.theClipboard, new IObjectBackend.MockLoadedObject((IRIO) app.theClipboard)));
                    }
                }
        }, app.f.statusBarTH);
        workspace = new UIAppendButton(T.g.bQuit, workspace, app.ui.createLaunchConfirmation(T.u.dReturnMenuWarn, () -> {
            app.ui.wm.pleaseShutdown();
        }), app.f.statusBarTH);
        return workspace;
    }

    private static UIElement makeFileList(App app) {
        final TrRoot T = app.t;
        LinkedList<ObjectInfo> s = app.sdb.listFileDefs();
        if (s.size() == 0)
            return null;
        LinkedList<String> str = new LinkedList<String>();
        LinkedList<Runnable> r = new LinkedList<Runnable>();
        for (final ObjectInfo s2 : s) {
            str.add(s2.toString());
            r.add(() -> {
                app.ui.launchSchema(s2.schemaName, app.odb.getObject(s2.idName), null);
            });
        }
        return new UIPopupMenu(str.toArray(new String[0]), r.toArray(new Runnable[0]), app.f.menuTH, app.f.menuS, false) {
            @Override
            public String toString() {
                return T.t.mDBO;
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
            for (DMKey me : rio.getHashKeys())
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

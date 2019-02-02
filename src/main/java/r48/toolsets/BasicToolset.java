/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.UIObjectDBMonitor;
import r48.UITest;
import r48.dbs.TXDB;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.schema.util.SchemaPath;
import r48.ui.Coco;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.dialog.UITextPrompt;
import r48.ui.imi.IMIAssemblyController;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Provides some basic tools for changing the configuration of R48 and doing various bits and pieces.
 * Note that the "rebuild UI" button was removed - the UI (specifically MapInfos right now)
 * attaches modification notifiers which can't go away until the whole ObjectDB is gone.
 * Better to avoid potential leaks by just freeing everything - it's expected that the tabs survive for the lifetime of the application,
 * that is, until shutdown is called on AppMain which disconnects everything that could possibly cause such a leak.
 * Created on 04/06/17.
 */
public class BasicToolset implements IToolset {
    @Override
    public UIElement[] generateTabs() {
        final UIPopupMenu menu = new UIPopupMenu(new String[] {
                TXDB.get("About"),
                TXDB.get("Configuration"),
                TXDB.get("Test Fonts"),
                TXDB.get("Toggle Fullscreen"),
                TXDB.get("Object Control Centre"),
                TXDB.get("Create Mod Installer (IMI) Data"),
                TXDB.get("Show ODB Memstat"),
                TXDB.get("Dump Schemaside Translations"),
                TXDB.get("Recover data from R48 error <INCREDIBLY DAMAGING>..."),
                TXDB.get("Return to menu"),
        }, new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        Coco.launch();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UIFontSizeConfigurator());
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Font Size?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                try {
                                    Integer i = Integer.parseInt(s);
                                    AppMain.window.createWindow(new UITextBox("", i));
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
                        AppMain.window.toggleFullscreen();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UIAutoclosingPopupMenu(new String[] {
                                TXDB.get("Edit Object"),
                                TXDB.get("Autocorrect Object By Name And Schema"),
                                TXDB.get("Inspect Object (no Schema needed)"),
                                TXDB.get("Object-Object Comparison"),
                                TXDB.get("Retrieve all object strings"),
                        }, new Runnable[] {
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
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
                                                    AppMain.window.createWindow(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
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
                                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                final IObjectBackend.ILoadedObject rio = AppMain.objectDB.getObject(s);
                                                AppMain.window.createWindow(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
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
                                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                IObjectBackend.ILoadedObject obj = AppMain.objectDB.getObject(s);
                                                if (obj == null) {
                                                    AppMain.launchDialog(TXDB.get("The file couldn't be read, and R48 cannot create it."));
                                                } else {
                                                    AppMain.window.createWindow(new UITest(obj.getObject()));
                                                }
                                            }
                                        }));
                                    }
                                },
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Source Object Name?"), new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                final IObjectBackend.ILoadedObject objA = AppMain.objectDB.getObject(s);
                                                AppMain.window.createWindow(new UITextPrompt(TXDB.get("Target Object Name?"), new IConsumer<String>() {
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
                                            for (String s : AppMain.getAllObjects()) {
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
                                            if (AppMain.dataPath.equals("Languages/")) {
                                                AppMain.launchDialog(TXDB.get("Wrote locmaps.txt (NOTE: You probably don't actually want to do this! Press this in RXP mode to get the CRCs, then go back to this mode to actually start editing stuff.)"));
                                            } else {
                                                AppMain.launchDialog(TXDB.get("Wrote locmaps.txt"));
                                            }
                                        } catch (IOException ioe) {
                                            throw new RuntimeException(ioe);
                                        }
                                    }
                                }
                        }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, false));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Root path to original game?"), new IConsumer<String>() {
                            @Override
                            public void accept(String s) {
                                s = PathUtils.fixRootPath(s);
                                new IMIAssemblyController(s);
                            }
                        }));
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        AppMain.window.createWindow(new UIObjectDBMonitor());
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
                        AppMain.window.createWindow(new UITextPrompt(TXDB.get("Safety Confirmation Prompt"), new IConsumer<String>() {
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
                        AppMain.pleaseShutdown();
                    }
                }
        }, FontSizes.menuTextHeight, FontSizes.menuScrollersize, false) {
            @Override
            public String toString() {
                return TXDB.get("System Tools");
            }
        };
        UIElement fl = makeFileList();
        if (fl != null)
            return new UIElement[] {
                    fl,
                    menu
            };
        return new UIElement[] {
                menu
        };
    }

    private static UIElement makeFileList() {
        LinkedList<String> s = AppMain.schemas.listFileDefs();
        if (s.size() == 0)
            return null;
        LinkedList<Runnable> r = new LinkedList<Runnable>();
        for (final String s2 : s)
            r.add(new Runnable() {
                @Override
                public void run() {
                    AppMain.launchSchema("File." + s2, AppMain.objectDB.getObject(s2), null);
                }
            });
        return new UIPopupMenu(s.toArray(new String[0]), r.toArray(new Runnable[0]), FontSizes.menuTextHeight, FontSizes.menuScrollersize, false) {
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

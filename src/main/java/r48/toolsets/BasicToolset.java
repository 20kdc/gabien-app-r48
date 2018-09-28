/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.*;
import r48.dbs.TXDB;
import r48.io.IMIUtils;
import r48.io.PathUtils;
import r48.schema.SchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.schema.util.SchemaPath;
import r48.ui.Coco;
import r48.ui.UIFontSizeConfigurator;
import r48.ui.UITextPrompt;
import r48.ui.utilitybelt.IMIAssemblyController;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Provides some basic tools for changing the configuration of R48 and doing various bits and pieces.
 * Note that the "rebuild UI" button was removed - the UI (specifically MapInfos right now)
 * attaches modification notifiers which can't go away until the whole ObjectDB is gone.
 * Better to avoid potential leaks by just freeing everything - it's expected that the tabs survive for the lifetime of the application,
 * that is, until shutdown is called on AppMain which disconnects everything that could possibly cause such a leak.
 * Created on 04/06/17.
 */
public class BasicToolset implements IToolset {
    final IConsumer<Boolean> setWT;
    public BasicToolset(IConsumer<Boolean> setWindowType) {
        setWT = setWindowType;
    }

    @Override
    public UIElement[] generateTabs(final IConsumer<UIElement> windowMaker) {
        return new UIElement[] {
                makeFileList(),
                new UIPopupMenu(new String[] {
                        TXDB.get("Edit Object"),
                        TXDB.get("New Object via Schema, ODB'AnonObject'"),
                        TXDB.get("Autocorrect Object By Name And Schema"),
                        TXDB.get("Inspect Object (no Schema needed)"),
                        TXDB.get("Object-Object Comparison"),
                        TXDB.get("Compile IMI Data"),
                        TXDB.get("Retrieve all object strings"),
                        TXDB.get("Set Internal Windows (good)"),
                        TXDB.get("Set External Windows (bad)"),
                        TXDB.get("Configuration"),
                        TXDB.get("Test Fonts"),
                        TXDB.get("Show Version"),
                        TXDB.get("Show ODB Memstat"),
                        TXDB.get("Dump Schemaside Translations"),
                        TXDB.get("Recover data from R48 error <INCREDIBLY DAMAGING>"),
                        TXDB.get("Toggle Fullscreen"),
                        TXDB.get("Return to menu"),
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.accept(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final RubyIO rio = AppMain.objectDB.getObject(s);
                                        if (AppMain.schemas.hasSDBEntry("File." + s)) {
                                            AppMain.launchSchema("File." + s, rio, null);
                                            return;
                                        }
                                        if (rio != null) {
                                            if (rio.type == 'o') {
                                                if (rio.symVal != null) {
                                                    if (AppMain.schemas.hasSDBEntry(rio.symVal)) {
                                                        AppMain.launchSchema(rio.symVal, rio, null);
                                                        return;
                                                    }
                                                }
                                            }
                                            windowMaker.accept(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
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
                                windowMaker.accept(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        AppMain.launchSchema(s, SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry(s), new RubyIO().setFX(0)), null);
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.accept(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final RubyIO rio = AppMain.objectDB.getObject(s);
                                        windowMaker.accept(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                SchemaElement ise = AppMain.schemas.getSDBEntry(s);
                                                ise.modifyVal(rio, new SchemaPath(ise, rio), false);
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
                                windowMaker.accept(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        RubyIO obj = AppMain.objectDB.getObject(s);
                                        if (obj == null) {
                                            AppMain.launchDialog(TXDB.get("The file couldn't be read, and R48 cannot create it."));
                                        } else {
                                            windowMaker.accept(new UITest(obj));
                                        }
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.accept(new UITextPrompt(TXDB.get("Source Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final RubyIO objA = AppMain.objectDB.getObject(s);
                                        windowMaker.accept(new UITextPrompt(TXDB.get("Target Object Name?"), new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                final RubyIO objB = AppMain.objectDB.getObject(s);
                                                if ((objA == null) || (objB == null)) {
                                                    AppMain.launchDialog(TXDB.get("A file couldn't be read, and R48 cannot create it."));
                                                } else {
                                                    try {
                                                        OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "objcompareAB.txt"));
                                                        byte[] cid = IMIUtils.createIMIData(objA, objB, "");
                                                        if (cid != null)
                                                            os.write(cid);
                                                        os.close();
                                                        os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "objcompareBA.txt"));
                                                        cid = IMIUtils.createIMIData(objB, objA, "");
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
                                windowMaker.accept(new UITextPrompt(TXDB.get("Root path to original game?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        s = PathUtils.fixRootPath(s);
                                        new IMIAssemblyController(s, windowMaker);
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
                                        RubyIO obj = AppMain.objectDB.getObject(s, null);
                                        if (obj != null) {
                                            universalStringLocator(obj, new IFunction<RubyIO, Integer>() {
                                                @Override
                                                public Integer apply(RubyIO rubyIO) {
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
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                setWT.accept(false);
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                if (GaBIEn.singleWindowApp()) { // SWA means no multiple GrInDrivers.
                                    AppMain.launchDialog(TXDB.get("You are running on a platform which does not support multiple windows."));
                                } else {
                                    setWT.accept(true);
                                }
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.accept(new UIFontSizeConfigurator());
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.accept(new UITextPrompt(TXDB.get("Font Size?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        try {
                                            Integer i = Integer.parseInt(s);
                                            windowMaker.accept(new UITextBox("", i));
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
                                Coco.launch();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.accept(new UIObjectDBMonitor());
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
                                windowMaker.accept(new UITextPrompt(TXDB.get("Safety Confirmation Prompt"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        // Don't translate this, don't lax the restrictions.
                                        //  (For a natively English user, the case-sensitivity and punctuation will be enough to stop them.)
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
                                AppMain.toggleFullscreen.run();
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
                }
        };
    }

    private static UIElement makeFileList() {
        LinkedList<String> s = AppMain.schemas.listFileDefs();
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

    public static int universalStringLocator(RubyIO rio, IFunction<RubyIO, Integer> string, boolean writing) {
        // NOTE: Hash keys, ivar keys are not up for modification.
        int total = 0;
        if (rio.type == '"')
            total += string.apply(rio);
        if ((rio.type == '{') || (rio.type == '}'))
            for (Map.Entry<RubyIO, RubyIO> me : rio.hashVal.entrySet())
                total += universalStringLocator(me.getValue(), string, writing);
        if (rio.type == '[')
            for (RubyIO me : rio.arrVal)
                total += universalStringLocator(me, string, writing);
        if (rio.iVarVals != null)
            for (RubyIO val : rio.iVarVals)
                total += universalStringLocator(val, string, writing);
        IMagicalBinder b = MagicalBinders.getBinderFor(rio);
        if (b != null) {
            RubyIO bound = MagicalBinders.toBoundWithCache(b, rio);
            int c = universalStringLocator(bound, string, writing);
            total += c;
            if (writing)
                if (c != 0)
                    b.applyBoundToTarget(bound, rio);
        }
        return total;
    }
}

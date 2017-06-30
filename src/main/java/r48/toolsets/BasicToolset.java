/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.*;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.UIFontSizeConfigurator;
import gabien.ui.UIScrollLayout;
import r48.ui.UITextPrompt;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Provides some basic tools for changing the configuration of R48 and doing various bits and pieces.
 * Note that the "rebuild UI" button was removed - the UI (specifically MapInfos right now)
 *  attaches modification notifiers which can't go away until the whole ObjectDB is gone.
 * Better to avoid potential leaks by just freeing everything - it's expected that the tabs survive for the lifetime of the application,
 *  that is, until shutdown is called on AppMain which disconnects everything that could possibly cause such a leak.
 * Created on 04/06/17.
 */
public class BasicToolset implements IToolset {
    private final IConsumer<UIElement> virtWM, realWM;
    private final IConsumer<IConsumer<UIElement>> setWM;
    public BasicToolset(UIWindowView rootView, IConsumer<UIElement> uiTicker, IConsumer<IConsumer<UIElement>> swm) {
        virtWM = rootView;
        realWM = uiTicker;
        setWM = swm;
    }

    @Override
    public String[] tabNames() {
        return new String[] {
                TXDB.get("Database Objects"),
                TXDB.get("System Tools")
        };
    }

    @Override
    public UIElement[] generateTabs(final ISupplier<IConsumer<UIElement>> windowMaker) {
        return new UIElement[] {
                makeFileList(),
                new UIPopupMenu(new String[] {
                        TXDB.get("Edit Object"),
                        TXDB.get("New Object via Schema, ODB'AnonObject'"),
                        TXDB.get("Autocorrect Object By Name And Schema"),
                        TXDB.get("Inspect Object (no Schema needed)"),
                        TXDB.get("Set Internal Windows (good)"),
                        TXDB.get("Set External Windows (bad)"),
                        TXDB.get("Use normal in-built fonts"),
                        TXDB.get("Use system fonts for everything"),
                        TXDB.get("Configure font sizes"),
                        TXDB.get("Test Fonts"),
                        TXDB.get("Show ODB Memstat"),
                        TXDB.get("Dump Schemaside Translations")
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
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
                                            windowMaker.get().accept(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
                                                @Override
                                                public void accept(String s) {
                                                    AppMain.launchSchema(s, rio, null);
                                                }
                                            }));
                                        } else {
                                            AppMain.launchDialog(TXDB.get("No file, or schema to create it."));
                                        }
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
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
                                windowMaker.get().accept(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final RubyIO rio = AppMain.objectDB.getObject(s);
                                        windowMaker.get().accept(new UITextPrompt(TXDB.get("Schema ID?"), new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                SchemaElement ise = AppMain.schemas.getSDBEntry(s);
                                                ise.modifyVal(rio, new SchemaPath(ise, rio, null), false);
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
                                windowMaker.get().accept(new UITextPrompt(TXDB.get("Object Name?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        windowMaker.get().accept(new UITest(AppMain.objectDB.getObject(s)));
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                setWM.accept(virtWM);
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                setWM.accept(realWM);
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                UILabel.fontOverride = null;
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                UILabel.fontOverride = GaBIEn.getFontOverrides()[0];
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UIFontSizeConfigurator());
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt(TXDB.get("Font Size?"), new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        int fs = Integer.parseInt(s);
                                        windowMaker.get().accept(new UITextBox(fs));
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UIObjectDBMonitor());
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                PrintStream ps = null;
                                try {
                                    ps = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "Lang" + TXDB.getLanguage() + ".txt"), false, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
                                LinkedList<String> t = new LinkedList<String>(TXDB.ssTexts);
                                Collections.sort(t);
                                for (String s : t) {
                                    if (s.startsWith("SDB@")) {
                                        ps.println("x \"" + s.substring(4) + "\"");
                                        if (TXDB.has(s)) {
                                            ps.println("y \"" + TXDB.get(s) + "\"");
                                        } else {
                                            ps.println("y \"" + TXDB.stripContext(s) + "\"");
                                        }
                                    }
                                }
                                ps.close();
                                ps = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "Cmtx" + TXDB.getLanguage() + ".txt"));
                                t = new LinkedList<String>(TXDB.ssTexts);
                                Collections.sort(t);
                                for (String s : t) {
                                    if (s.startsWith("CMDB@")) {
                                        ps.println("x \"" + s.substring(5) + "\"");
                                        if (TXDB.has(s)) {
                                            ps.println("y \"" + TXDB.get(s) + "\"");
                                        } else {
                                            ps.println("y \"" + TXDB.stripContext(s) + "\"");
                                        }
                                    }
                                }
                                ps.close();
                                AppMain.launchDialog(TXDB.get("Wrote Lang and Cmtx files (to be put in schema dir.)"));
                            }
                        }
                }, FontSizes.menuTextHeight, false)
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
        return new UIPopupMenu(s.toArray(new String[0]), r.toArray(new Runnable[0]), FontSizes.menuTextHeight, false);
    }
}

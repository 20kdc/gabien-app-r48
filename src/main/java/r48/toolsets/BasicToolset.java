/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import gabien.ui.*;
import r48.*;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.UIFontSizeConfigurator;
import gabien.ui.UIScrollLayout;
import r48.ui.UITextPrompt;

import java.util.LinkedList;

/**
 * Created on 04/06/17.
 */
public class BasicToolset implements IToolset {
    private final IConsumer<UIElement> virtWM, realWM;
    private final IConsumer<IConsumer<UIElement>> setWM;
    private final Runnable rebuildUi;
    public BasicToolset(UIWindowView rootView, IConsumer<UIElement> uiTicker, IConsumer<IConsumer<UIElement>> swm, Runnable runnable) {
        virtWM = rootView;
        realWM = uiTicker;
        setWM = swm;
        rebuildUi = runnable;
    }

    @Override
    public String[] tabNames() {
        return new String[] {
                "Database Objects",
                "System Tools",
                " "
        };
    }

    @Override
    public UIElement[] generateTabs(final ISupplier<IConsumer<UIElement>> windowMaker) {
        return new UIElement[] {
                makeFileList(),
                new UIPopupMenu(new String[] {
                        "Edit Object",
                        "New Object via Schema, ODB'AnonObject'",
                        "Autocorrect Object By Name And Schema",
                        "Inspect Object (no Schema needed)",
                        "Set Internal Windows (good)",
                        "Set External Windows (bad)",
                        "Use normal in-built fonts",
                        "Use system fonts for everything",
                        "Configure font sizes",
                        "Rebuild UI",
                        "Test Fonts",
                        "Show ODB Memstat"
                }, new Runnable[] {
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt("Object Name?", new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final RubyIO rio = AppMain.objectDB.getObject(s);
                                        if (AppMain.schemas.hasSDBEntry("File." + s)) {
                                            AppMain.launchSchema("File." + s, rio);
                                            return;
                                        }
                                        if (rio != null) {
                                            if (rio.type == 'o') {
                                                if (rio.symVal != null) {
                                                    if (AppMain.schemas.hasSDBEntry(rio.symVal)) {
                                                        AppMain.launchSchema(rio.symVal, rio);
                                                        return;
                                                    }
                                                }
                                            }
                                            windowMaker.get().accept(new UITextPrompt("Schema ID?", new IConsumer<String>() {
                                                @Override
                                                public void accept(String s) {
                                                    AppMain.launchSchema(s, rio);
                                                }
                                            }));
                                        } else {
                                            AppMain.launchDialog("No file, or schema to create it.");
                                        }
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt("Schema ID?", new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        AppMain.launchSchema(s, SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry(s), new RubyIO().setFX(0)));
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt("Object Name?", new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        final RubyIO rio = AppMain.objectDB.getObject(s);
                                        windowMaker.get().accept(new UITextPrompt("Schema ID?", new IConsumer<String>() {
                                            @Override
                                            public void accept(String s) {
                                                SchemaElement ise = AppMain.schemas.getSDBEntry(s);
                                                ise.modifyVal(rio, new SchemaPath(ise, rio, null), false);
                                                AppMain.launchDialog("OK!");
                                            }
                                        }));
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt("Object Name?", new IConsumer<String>() {
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
                                UILabel.iAmAbsolutelySureIHateTheFont = false;
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                UILabel.iAmAbsolutelySureIHateTheFont = true;
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UIFontSizeConfigurator());
                            }
                        },
                        rebuildUi,
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UITextPrompt("Font Size?", new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        int fs = Integer.parseInt(s);
                                        UIScrollLayout svl = new UIScrollLayout(true);
                                        for (int i = 0; i < 128; i++)
                                            svl.panels.add(new UITextBox(fs));
                                        svl.setBounds(new Rect(0, 0, 320, 240));
                                        windowMaker.get().accept(svl);
                                    }
                                }));
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                windowMaker.get().accept(new UIObjectDBMonitor());
                            }
                        }
                }, FontSizes.menuTextHeight, false),
                new UIPanel()
        };
    }
    private static UIElement makeFileList() {
        LinkedList<String> s = AppMain.schemas.listFileDefs();
        LinkedList<Runnable> r = new LinkedList<Runnable>();
        for (final String s2 : s)
            r.add(new Runnable() {
                @Override
                public void run() {
                    AppMain.launchSchema("File." + s2, AppMain.objectDB.getObject(s2));
                }
            });
        return new UIPopupMenu(s.toArray(new String[0]), r.toArray(new Runnable[0]), FontSizes.menuTextHeight, false);
    }
}

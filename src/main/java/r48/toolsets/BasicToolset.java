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
import gabien.IGrDriver;
import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ui.*;
import r48.*;
import r48.dbs.TXDB;
import r48.imagefx.HueShiftImageEffect;
import r48.imagefx.ToneImageEffect;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.UIFontSizeConfigurator;
import r48.ui.UITextPrompt;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
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
                        TXDB.get("Configure fonts"),
                        TXDB.get("Test Fonts"),
                        TXDB.get("Test Tones"),
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
                                UIPanel panel = new UIPanel();
                                panel.setBounds(new Rect(0, 0, 512, 1280));
                                final IImage totem = GaBIEn.getImage("tonetotm.png");
                                UIElement hueChanger = new UIElement() {
                                    public double time = 0;

                                    @Override
                                    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
                                        double time2 = time;
                                        if (!selected)
                                            time += deltaTime;
                                        time2 -= Math.floor(time2);
                                        int hue = (int) (time2 * 360);
                                        igd.blitImage(0, 0, 256, 256, ox, oy, AppMain.imageFXCache.process(totem, new HueShiftImageEffect(hue)));
                                    }
                                };
                                hueChanger.setBounds(new Rect(128, 1024, 256, 256));
                                panel.allElements.add(hueChanger);
                                IGrDriver finalComposite = GaBIEn.makeOffscreenBuffer(512, 1024, false);
                                finalComposite.blitImage(0, 0, 256, 256, 0, 0, AppMain.imageFXCache.process(totem, new ToneImageEffect(128, 128, 128, 128)));
                                finalComposite.blitImage(0, 0, 256, 256, 256, 0, AppMain.imageFXCache.process(totem, new ToneImageEffect(0, 128, 128, 128)));

                                finalComposite.blitImage(0, 0, 256, 256, 0, 256, AppMain.imageFXCache.process(totem, new ToneImageEffect(128, 0, 128, 128)));
                                finalComposite.blitImage(0, 0, 256, 256, 256, 256, AppMain.imageFXCache.process(totem, new ToneImageEffect(128, 128, 0, 128)));

                                finalComposite.blitImage(0, 0, 256, 256, 0, 512, AppMain.imageFXCache.process(totem, new ToneImageEffect(128, 128, 128, 0)));
                                finalComposite.blitImage(0, 0, 256, 256, 256, 512, AppMain.imageFXCache.process(totem, new ToneImageEffect(0, 128, 128, 0)));

                                finalComposite.blitImage(0, 0, 256, 256, 0, 768, AppMain.imageFXCache.process(totem, new ToneImageEffect(128, 0, 128, 0)));
                                finalComposite.blitImage(0, 0, 256, 256, 256, 768, AppMain.imageFXCache.process(totem, new ToneImageEffect(128, 128, 0, 0)));
                                panel.baseImage = GaBIEn.createImage(finalComposite.getPixels(), 512, 1024);
                                finalComposite.shutdown();
                                UIScrollLayout holdsMain = new UIScrollLayout(true, FontSizes.generalScrollersize);
                                holdsMain.panels.add(panel);
                                holdsMain.setBounds(new Rect(0, 0, 544, 256));
                                windowMaker.get().accept(holdsMain);
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
                                PrintStream psA = null;
                                PrintStream psB = null;
                                try {
                                    psA = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "Lang" + TXDB.getLanguage() + ".txt"), false, "UTF-8");
                                    psB = new PrintStream(GaBIEn.getOutFile(AppMain.rootPath + "Cmtx" + TXDB.getLanguage() + ".txt"));
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

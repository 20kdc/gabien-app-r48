/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.map.UIMapToolWrapper;
import r48.ui.UIFontSizeConfigurator;
import r48.ui.UIHHalfsplit;
import r48.ui.UIScrollVertLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Created on 1/27/17.
 */
public class Application {

    private static IConsumer<Double> appTicker = null;

    public static void gabienmain() throws IOException {
        final WindowCreatingUIElementConsumer uiTicker = new WindowCreatingUIElementConsumer();

        final UIScrollVertLayout gamepaks = new UIScrollVertLayout();
        gamepaks.setBounds(new Rect(0, 0, 320, 200));
        // this can't be good
        final IConsumer<Runnable> closeHelper = new IConsumer<Runnable>() {
            private Runnable r;
            @Override
            public void accept(Runnable runnable) {
                if (runnable != null) {
                    r = runnable;
                } else {
                    r.run();
                    runnable = null;
                }
            }
        };

        UIAdjuster scaleAdjust = new UIAdjuster(FontSizes.launcherTextHeight, new ISupplier<String>() {
            @Override
            public String get() {
                return Integer.toString(++uiTicker.createScale);
            }
        }, new ISupplier<String>() {
            @Override
            public String get() {
                if (uiTicker.createScale == 1)
                    return Integer.toString(uiTicker.createScale);
                return Integer.toString(--uiTicker.createScale);
            }
        });
        gamepaks.panels.add(new UIHHalfsplit(1, 2, new UITextButton(FontSizes.launcherTextHeight, "Quit", new Runnable() {
            @Override
            public void run() {
                GaBIEn.ensureQuit();
            }
        }), new UITextButton(FontSizes.launcherTextHeight, "Font Cfg.", new Runnable() {
            @Override
            public void run() {
                uiTicker.accept(new UIFontSizeConfigurator());
            }
        })));
        gamepaks.panels.add(new UIHHalfsplit(3, 5, new UILabel("Scale:", FontSizes.launcherTextHeight), scaleAdjust));

        gamepaks.panels.add(new UILabel("Choose Gamepak:", FontSizes.launcherTextHeight));

        new DBLoader(new BufferedReader(new InputStreamReader(GaBIEn.getFile("Gamepaks.txt"))), new IDatabase() {

            UITextButton lastButton;

            @Override
            public void newObj(int objId, final String objName) throws IOException {
                InputStream tester = GaBIEn.getFile(objName + "/Schema.txt");
                if (tester != null) {
                    tester.close();
                } else {
                    return;
                }
                String status = "?";
                if (objId == 0)
                    status = "Working";
                if (objId == 1)
                    status = "Mostly OK";
                if (objId == 2)
                    status = "Minimal/Useless";
                if (objId == 3)
                    status = "Non-existent";
                lastButton = new UITextButton(FontSizes.enumChoiceTextHeight, objName + " (" + status + ")", new Runnable() {
                    @Override
                    public void run() {
                        if (appTicker == null) {
                            try {
                                AppMain.initialize(objName + "/");
                                appTicker = AppMain.initializeAndRun(uiTicker);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        closeHelper.accept(null);
                    }
                });
                gamepaks.panels.add(lastButton);
            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == '.') {
                    String rn = "";
                    for (String s : args)
                        rn += s + " ";
                    lastButton.Text = rn;
                }
            }
        });

        while (true) {
            final UIMapToolWrapper uimtw = new UIMapToolWrapper(gamepaks);
            closeHelper.accept(new Runnable() {
                @Override
                public void run() {
                    uimtw.selfClose = true;
                }
            });
            uiTicker.createScale = 1;
            scaleAdjust.accept("1");
            uiTicker.accept(uimtw);

            while (uiTicker.runningWindows() > 0) {
                double dT = GaBIEn.timeDelta(false);
                while (dT < 0.02d) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dT = GaBIEn.timeDelta(false);
                }
                dT = GaBIEn.timeDelta(true);
                if (appTicker != null)
                    appTicker.accept(dT);
                uiTicker.runTick(dT);
            }
            if (!uimtw.selfClose)
                break;
            appTicker = null;
        }
        GaBIEn.ensureQuit();
    }
}

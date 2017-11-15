/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.WindowSpecs;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.maptools.UIMTBase;
import r48.ui.Art;
import r48.ui.UIAppendButton;
import r48.ui.UIFontSizeConfigurator;
import r48.ui.help.UIHelpSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 1/27/17.
 */
public class Application {
    // The default value of this affects splash screen.
    // The actual value used for the main application is in gabienmain() below, set after the splash.
    public static int globalMS = 50;
    private static double compensationDT = 0;

    private static IGPMenuPanel rootGPMenuPanel;

    protected static IConsumer<Double> appTicker = null;
    protected static UITextBox rootBox;
    protected static WindowCreatingUIElementConsumer uiTicker;

    public static boolean mobileExtremelySpecialBehavior;

    public static String secondaryImageLoadLocation = "";

    public static void gabienmain() throws IOException {
        mobileExtremelySpecialBehavior = GaBIEn.singleWindowApp();
        uiTicker = new WindowCreatingUIElementConsumer();
        // runFontLoader tries to do as much loading as possible there
        Rect splashSize = runFontLoader();
        // Set globalMS to intended value
        globalMS = 33;
        // This must happen after waiting for the UILabel font override stuff
        boolean fontsLoaded = FontSizes.load();
        if (!fontsLoaded)
            if (GaBIEn.singleWindowApp()) // SWA always means we need to adapt to local screen size, and should generally cut down as many usability issues as possible
                autoDetectCorrectUISizeOnSWA(splashSize.width, splashSize.height);

        // Note the mass-recreate.
        while (true) {
            final UIScrollLayout gamepaks = new UIScrollLayout(true, FontSizes.generalScrollersize);
            gamepaks.setBounds(new Rect(0, 0, 400, 200));
            // this can't be good
            // Ok, explaination for this. Giving it a runnable, it will hold it until called again, and then it will run it and remove it.
            final IConsumer<Runnable> closeHelper = new IConsumer<Runnable>() {
                private Runnable r;

                @Override
                public void accept(Runnable runnable) {
                    if (runnable != null) {
                        r = runnable;
                    } else {
                        r.run();
                        r = null;
                    }
                }
            };

            UIAdjuster msAdjust = new UIAdjuster(FontSizes.launcherTextHeight, new ISupplier<String>() {
                @Override
                public String get() {
                    return Integer.toString(++globalMS);
                }
            }, new ISupplier<String>() {
                @Override
                public String get() {
                    if (globalMS == 1)
                        return Integer.toString(globalMS);
                    return Integer.toString(--globalMS);
                }
            });
            msAdjust.accept(Integer.toString(globalMS));

            gamepaks.panels.add(figureOutTopBar(uiTicker, closeHelper));

            gamepaks.panels.add(new UISplitterLayout(new UILabel(TXDB.get("MS per frame:"), FontSizes.launcherTextHeight), msAdjust, false, 3, 5));

            gamepaks.panels.add(new UILabel(TXDB.get("Root Path:"), FontSizes.launcherTextHeight));

            rootBox = new UITextBox(FontSizes.launcherTextHeight);

            /*
             * If single-window, assume we're on Android, so the user probably wants to be able to use EasyRPG Player
             * Regarding if I'm allowed to do this:
             */
            if (mobileExtremelySpecialBehavior)
                rootBox.text = "easyrpg/games/R48 Game";

            gamepaks.panels.add(rootBox);

            gamepaks.panels.add(new UILabel(TXDB.get("Secondary Image Load Location:"), FontSizes.launcherTextHeight));

            final UITextBox sillBox = new UITextBox(FontSizes.launcherTextHeight);
            sillBox.text = "";
            sillBox.onEdit = new Runnable() {
                @Override
                public void run() {
                    secondaryImageLoadLocation = sillBox.text.replace('\\', '/');
                    if (secondaryImageLoadLocation.length() != 0)
                        if (!secondaryImageLoadLocation.endsWith("/"))
                            secondaryImageLoadLocation += "/";
                    sillBox.text = secondaryImageLoadLocation;
                }
            };

            gamepaks.panels.add(new UISplitterLayout(sillBox, new UITextButton(FontSizes.launcherTextHeight, TXDB.get("Save"), new Runnable() {
                @Override
                public void run() {
                    FontSizes.save();
                }
            }), false, 1));

            gamepaks.panels.add(new UILabel(TXDB.get("Choose Target Engine:"), FontSizes.launcherTextHeight));

            final int firstGPI = gamepaks.panels.size();
            final IConsumer<IGPMenuPanel> menuConstructor = new IConsumer<IGPMenuPanel>() {
                @Override
                public void accept(IGPMenuPanel igpMenuPanel) {
                    while (gamepaks.panels.size() > firstGPI)
                        gamepaks.panels.removeLast();
                    if (igpMenuPanel == null) {
                        closeHelper.accept(null);
                        return;
                    }
                    String[] names = igpMenuPanel.getButtonText();
                    ISupplier<IGPMenuPanel>[] runs = igpMenuPanel.getButtonActs();
                    for (int i = 0; i < names.length; i++) {
                        final ISupplier<IGPMenuPanel> r = runs[i];
                        gamepaks.panels.add(new UITextButton(FontSizes.enumChoiceTextHeight, names[i], new Runnable() {
                            @Override
                            public void run() {
                                accept(r.get());
                            }
                        }));
                    }
                    gamepaks.setBounds(gamepaks.getBounds());
                }
            };
            // ...

            gamepaks.setBounds(new Rect(0, 0, FontSizes.scaleGuess(640), FontSizes.scaleGuess(480)));
            menuConstructor.accept(rootGPMenuPanel);
            final UIMTBase uimtw = UIMTBase.wrap(null, gamepaks, false);
            uiTicker.accept(uimtw);
            closeHelper.accept(new Runnable() {
                @Override
                public void run() {
                    uimtw.selfClose = true;
                }
            });

            // This is the identity of the error window that 'brings the system down softly'.
            UIElement failed = null;
            // ok, so, 'what is going on with the flags', you might ask?
            // Well:
            // backupAvailable describes the state of the LAST backup made
            // Emergency backups always occur JUST BEFORE message writing time
            // weHaveSecondary indicates if any secondary backup ever completed during this run,
            //  which makes it worth keeping.
            boolean backupAvailable = false;
            while (uiTicker.runningWindows().size() > 0) {
                double dT = handleTick();
                try {
                    if (appTicker != null)
                        appTicker.accept(dT);
                    uiTicker.runTick(dT);
                } catch (Exception e) {
                    if (failed == null) {
                        e.printStackTrace();
                        System.err.println("-- STARTING EMERGENCY PROCEDURES --");
                        System.err.println("Usually you don't need to see this text, but I do.");
                        System.err.println("See, performSystemDump sometimes locks up. Ironic, I know. (Later: Turns out what was happening was 1.9MB of data going through slow serialization routines.)");
                        Exception fErr = null;
                        try {
                            AppMain.performSystemDump(true);
                            backupAvailable = true;
                        } catch (Exception finalErr) {
                            fErr = finalErr;
                        }
                        System.err.println("This is the R48 'Everything is going down the toilet' display!");
                        System.err.println("Current status: BACKUP AVAILABLE? " + backupAvailable);
                        System.err.println("Preparing file...");
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream ps = new PrintStream(baos, false, "UTF-8");
                        ps.println(TXDB.get("An error has occurred in R48. This is always the result of a bug somewhere."));
                        ps.println(TXDB.get("If the rest of R48 disappeared, that means a second error occurred, and R48 has shut down to keep this message up."));
                        ps.println(TXDB.get("This is because, if backups failed, then Save would fail anyway - and without these instructions, you're kind of doomed."));
                        if (backupAvailable) {
                            ps.println(TXDB.get("A backup data file has been created."));
                            ps.println(TXDB.get("QUIT AFTER READING THIS MESSAGE IN IT'S ENTIRETY."));
                            ps.println(TXDB.get("MAKE A COPY OF THE ENTIRE DIRECTORY AND ALL R48 SYSTEM FILES IMMEDIATELY. (anything with .r48 extension is an r48 system file. clip.r48 counts but is probably unnecessary.)"));
                            ps.println(TXDB.get("DO NOT MODIFY OR DESTROY THIS COPY UNLESS YOUR CURRENT WORK IS COMPLETELY SAFE, VALID, NON-CORRUPT AND BACKED UP."));
                            ps.println(TXDB.get("PREFERABLY FORWARD THE ERROR TEXT FILE (r48.error.txt) TO YOUR DEVELOPMENT GROUP."));
                            ps.println(TXDB.get("I wrote that in caps since those are the most important instructions for recovering your work."));
                            ps.println(TXDB.get("Make a copy of r48.error.YOUR_SAVED_DATA.r48 - it contains your data at the time of the error."));
                            ps.println(TXDB.get("You can import the backup using 'Recover data from R48 error' - but copy the game first, as the data may be corrupt."));
                            ps.println(TXDB.get("You are encountering an error. Backup as much as you can, backup as often as you can."));
                        } else {
                            ps.println(TXDB.get("Unfortunately, R48 was unable to make a backup. If R48 is gone, this means that, basically, there's no way the current state could be recoverable."));
                            ps.println(TXDB.get("Unless you ran out of disk space, even attaching a debugger would not help you at this point, because the data is likely corrupt."));
                            ps.println(TXDB.get("Make a copy of the game immediately, then, if R48 is still around, try to save, but I will summarize by saying: it appears all hope is lost now."));
                            ps.println(TXDB.get("The reason for the failure to backup is below."));
                            ps.println(TXDB.get("----"));
                            fErr.printStackTrace(ps);
                            ps.println(TXDB.get("----"));
                        }
                        ps.println(TXDB.get("Error details follow."));
                        e.printStackTrace(ps);
                        System.err.println("Prepared contents...");
                        try {
                            OutputStream fos = GaBIEn.getOutFile("r48.error.txt");
                            baos.writeTo(fos);
                            fos.close();
                            System.err.println("Save OK!");
                        } catch (Exception ioe) {
                            ps.println("The error could not be saved.");
                            System.err.println("Failed to save!");
                        }
                        System.err.println("Displaying situation to user");
                        if (GaBIEn.singleWindowApp()) {
                            // SWA means this can't be handled sanely
                            shutdownAllAppMainWindows();
                        }

                        String r = baos.toString("UTF-8").replaceAll("\r", "");
                        UIHelpSystem uhs = new UIHelpSystem();
                        for (String s : r.split("\n"))
                            uhs.page.add(new UIHelpSystem.HelpElement('.', s.split(" ")));
                        UIScrollLayout scroll = new UIScrollLayout(true, FontSizes.generalScrollersize) {
                            @Override
                            public String toString() {
                                return "Error...";
                            }
                        };
                        scroll.panels.add(uhs);
                        uhs.setBounds(new Rect(0, 0, 640, 480));
                        scroll.setBounds(new Rect(0, 0, 640, 480));
                        uiTicker.accept(scroll);
                        failed = scroll;
                        System.err.println("Well, that worked at least");
                    } else {
                        e.printStackTrace();
                        // Shut down R48 to 'stem the bleeding'.
                        // Need to preserve the notice to the user. If all backups failed then the user is screwed anyway, so just tell the user what their options are.
                        for (UIElement uie : uiTicker.runningWindows()) {
                            if (uie != failed) {
                                try {
                                    uiTicker.forceRemove(uie);
                                } catch (Exception e3) {
                                    // just in case of rogue windowClosed
                                }
                            }
                        }
                        appTicker = null;
                        try {
                            AppMain.shutdown();
                        } catch (Exception e4) {

                        }
                    }
                }
            }
            if (failed != null)
                break;
            if (!uimtw.selfClose)
                break;
            appTicker = null;
            // Cleanup application memory
            AppMain.shutdown();
        }
        GaBIEn.ensureQuit();
    }

    private static double handleTick() {
        double dT = GaBIEn.timeDelta(false);
        double dTTarg = (globalMS / 1000d) - compensationDT;
        while (dT < dTTarg) {
            try {
                Thread.sleep(globalMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dT = GaBIEn.timeDelta(false);
        }
        double dTRes = GaBIEn.timeDelta(true);
        compensationDT = Math.min(dTTarg, dTRes - dTTarg);
        return dTRes;
    }

    private static Rect runFontLoader() {
        int frames = -10; // Fadeout
        int timer2 = 0; // Baton
        String movement = " "; // the baton is 'thrown'
        // Used for two reasons.
        // 1. to work out window size during a specific situation on Android.
        // 2. on certain Linux distributions, Java still freezes up during font load
        WindowSpecs ws = GaBIEn.defaultWindowSpecs("R48 Startup...", 800, 600);
        ws.scale = 1;
        ws.resizable = true;
        IGrInDriver gi = GaBIEn.makeGrIn("R48 Startup...", 800, 600, ws);
        // runs in parallel with font-load wait
        final AtomicBoolean txdbDonePrimaryTask = new AtomicBoolean(false);
        Thread txdbThread = new Thread() {
            @Override
            public void run() {
                TXDB.init();
                FontSizes.loadLanguage();
                // TXDB 'stable', spammed class refs
                txdbDonePrimaryTask.set(true);
                rootGPMenuPanel = new PrimaryGPMenuPanel();
            }
        };
        txdbThread.start();
        while (frames <= 15) {
            gi.flush(); // to kickstart w/h
            handleTick();
            gi.clearAll(255, 255, 255);
            int sz = (Math.min(gi.getWidth(), gi.getHeight()) / 4) * 2;
            Rect pos = new Rect((gi.getWidth() / 2) - (sz / 2), (gi.getHeight() / 2) - (sz / 2), sz, sz);
            Rect ltPos = Art.r48ico;
            gi.blitScaledImage(ltPos.x, ltPos.y, ltPos.width, ltPos.height, pos.x, pos.y, pos.width, pos.height, GaBIEn.getImage("layertab.png"));

            // Can't translate for several reasons (but especially no fonts).
            // This is really the only reason any of the messages are likely to be seen,
            //  because certain JRE implementations can't load fonts to save their lives
            String waitingFor = null;
            // Doesn't matter if it switches font on the last frame or something, just make sure the application remains running
            if (UILabel.fontOverride == null) {
                waitingFor = "Loading";
            } else if ((!txdbDonePrimaryTask.get()) || (rootGPMenuPanel == null)) {
                waitingFor = "Loading";
            }
            if (waitingFor == null) {
                frames++;
                waitingFor = TXDB.get("Fading");
            }
            char[] chars = {'|', '/', '-', '\\'};
            char ch = chars[timer2 % chars.length];
            timer2++;
            movement += "  ";
            // has to be internal-font-able
            int goodSize = 16;
            int goodSizeActual = UILabel.getRecommendedSize("", goodSize).height;
            UILabel.drawLabel(gi, gi.getWidth(), 0, gi.getHeight() - goodSizeActual, waitingFor + movement + ch, 1, goodSize);

            // fade
            int c = Math.max(0, Math.min(255, 25 * frames)) << 24;
            gi.blitScaledImage(0, 0, 1, 1, 0, 0, gi.getWidth(), gi.getHeight(), GaBIEn.createImage(new int[] {c}, 1, 1));
        }
        Rect r = new Rect(0, 0, gi.getWidth(), gi.getHeight());
        gi.shutdown();
        return r;
    }

    private static void autoDetectCorrectUISizeOnSWA(int w, int h) {
        // The above triggered a flush, which would cause the initial resize on SWPs
        FontSizes.uiGuessScaleTenths = Math.max(10, Math.min(w, h) / 30);

        for (FontSizes.FontSizeField fsf : FontSizes.getFields()) {
            // as this is a touch device, map 8 to 16 (6 is for things that really matter)
            if (fsf.get() == 8)
                fsf.accept(16);
            if (!fsf.name.equals("uiGuessScaleTenths"))
                fsf.accept(FontSizes.scaleGuess(fsf.get()));
        }
        // exceptions
        FontSizes.tilesTabTextHeight *= 2;
    }

    private static UIElement figureOutTopBar(final WindowCreatingUIElementConsumer uiTicker, final IConsumer<Runnable> closeHelper) {
        UIElement whatever = new UITextButton(FontSizes.launcherTextHeight, TXDB.get("Quit R48"), new Runnable() {
            @Override
            public void run() {
                GaBIEn.ensureQuit();
            }
        });
        if (!GaBIEn.singleWindowApp()) { // SWA means we can't create windows
            whatever = new UISplitterLayout(whatever, new UITextButton(FontSizes.launcherTextHeight, TXDB.get("Font Sizes"), new Runnable() {
                @Override
                public void run() {
                    uiTicker.accept(new UIFontSizeConfigurator());
                    closeHelper.accept(null);
                }
            }), false, 1, 2);
        }

        return new UIAppendButton(TXDB.getLanguage(), whatever, new Runnable() {
            @Override
            public void run() {
                // Unfortunately, if done quickly enough, the font will not load in time.
                // (Java "lazily" loads fonts.
                //  gabien-javase works around this bug - lazy loading appears to result in Java devs not caring about font load speed -
                //  and by the time it matters it's usually loaded, but, well, suffice to say this hurts my translatability plans a little.
                //  Not that it'll stop them, but it's annoying.)
                // This associates a lag with switching language, when it's actually due to Java being slow at loading a font.
                // (I'm slightly glad I'm not the only one this happens for, but unhappy that it's an issue.)
                // Unfortunately, a warning message cannot be shown to the user, as the warning message would itself trigger lag-for-font-load.
                TXDB.nextLanguage();
                closeHelper.accept(null);
            }
        }, FontSizes.launcherTextHeight);
    }

    // Magically handles case issues.
    public static String autoDetectWindows(String s) {
        final String giveUp = s;
        try {
            // '/' is 'universal'. Not supposed to be, but that doesn't matter.
            // Firstly convert to '/' form.
            // We will be dealing with the following kinds of paths.
            // Relative: "([$PATHCHARS]*/)*[$PATHCHARS]*"
            // Windows Absolute: "?:/.*"
            // MLA Absolute / Windows NT Special Path Absolute: "/.*"
            s = s.replace('\\', '/');
            if (s.equals(""))
                return s;
            if (!s.contains("/"))
                if (s.contains(":"))
                    return s; // A: / B: / C:
            if (GaBIEn.fileOrDirExists(s))
                return s;
            // Deal with earlier path components...
            String st = GaBIEn.basename(s);
            // Sanity check.
            if (s.contains("/")) {
                if (!s.endsWith("/" + st))
                    throw new RuntimeException("Weird inconsistency in gabien path sanitizer. 'Should never happen' but safety first.");
            } else {
                // Change things to make sense.
                s = "./" + st;
            }
            String parent = autoDetectWindows(s.substring(0, s.length() - (st.length() + 1)));
            String[] subfiles = GaBIEn.listEntries(parent);
            if (subfiles != null)
                for (String s2 : subfiles)
                    if (s2.equalsIgnoreCase(st))
                        return parent + "/" + s2;
            // Oh well.
            return parent + "/" + st;
        } catch (Exception e) {
            // This will likely result from permissions errors & IO errors.
            // As this is just meant as a workaround for devs who can't use consistent case, it's not necessary to R48 operation.
            return giveUp;
        }
    }

    // Only use from AppMain's "pleaseShutdown"
    public static void shutdownAllAppMainWindows() {
        for (UIElement uie : uiTicker.runningWindows())
            uiTicker.forceRemove(uie);
    }
}

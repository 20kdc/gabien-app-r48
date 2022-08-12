/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabienapp;

import gabien.FontManager;
import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.WindowSpecs;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.AdHocSaveLoad;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.imageio.ImageIOFormat;
import r48.ui.Art;
import r48.ui.Coco;
import r48.ui.UIAppendButton;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.spacing.UIBorderedSubpanel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 1/27/17.
 */
public class Application {
    public static int globalMS;
    private static double compensationDT;

    // ----- These are settings that are controlled in FontSizes. -----
    public static boolean allowBlending;
    public static boolean windowingExternal = false;
    // -----

    protected static IConsumer<Double> appTicker = null;
    protected static UITextBox rootBox;
    protected static WindowCreatingUIElementConsumer uiTicker;

    // This should be set to true if on a device where touch controls are used.
    public static boolean mobileExtremelySpecialBehavior;

    public static String secondaryImageLoadLocation = "";
    // This is the root path which is *defaulted to*.
    public static String rootPathBackup = "";

    // used for directory name so R48 stops polluting any workspace it's used in.
    public static final String BRAND = "r48";

    public static void gabienmain() throws IOException {
        globalMS = 50;
        FontSizes.reset();

        GaBIEn.appPrefixes = new String[] {BRAND + "/", ""};
        GaBIEn.sysCoreFontSize = FontSizes.gSysCoreTextHeight;

        mobileExtremelySpecialBehavior = GaBIEn.singleWindowApp();
        uiTicker = new WindowCreatingUIElementConsumer();
        // runFontLoader tries to do as much loading as possible there
        int uiScaleTenths = runFontLoader();
        // Set globalMS to intended value
        globalMS = 33;

        /*
         * If single-window, assume we're on Android,
         *  so the user probably wants to be able to use EasyRPG Player
         * If EasyRPG Player has an issue with this, please bring it up at any time, and I will change this.
         */
        if (mobileExtremelySpecialBehavior)
            rootPathBackup = "easyrpg/games/R48 Game";

        // The 'true' here is so that it will load in "late" defaults (fontOverride)
        boolean fontsLoaded = FontSizes.load(true);
        if (!fontsLoaded)
            autoDetectCorrectUISize(uiScaleTenths);

        // Note the mass-recreate.
        while (true) {
            final AtomicBoolean gamepaksRequestClose = new AtomicBoolean(false);
            final UIScrollLayout gamepaks = new UIScrollLayout(true, FontSizes.generalScrollersize) {
                @Override
                public boolean requestsUnparenting() {
                    return gamepaksRequestClose.get();
                }
            };
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

            UIAdjuster msAdjust = new UIAdjuster(FontSizes.launcherTextHeight, globalMS, new IFunction<Long, Long>() {
                @Override
                public Long apply(Long aLong) {
                    int gms = (int) (long) aLong;
                    if (gms < 1)
                        gms = 1;
                    globalMS = gms;
                    return (long) gms;
                }
            });
            msAdjust.accept(Integer.toString(globalMS));

            final LinkedList<UIElement> basePanels = new LinkedList<UIElement>();

            UIHelpSystem uhs = new UIHelpSystem();
            HelpSystemController hsc = new HelpSystemController(null, "Help/Launcher/Entry", uhs);
            hsc.loadPage(0);

            basePanels.add(new UIBorderedSubpanel(uhs, FontSizes.scaleGuess(8)));

            basePanels.add(figureOutTopBar(uiTicker, closeHelper));

            basePanels.add(new UISplitterLayout(new UILabel(TXDB.get("MS per frame:"), FontSizes.launcherTextHeight), msAdjust, false, 3, 5));

            basePanels.add(new UILabel(TXDB.get("Path To Game (if you aren't running R48 in the game folder):"), FontSizes.launcherTextHeight));

            rootBox = new UITextBox(rootPathBackup, FontSizes.launcherTextHeight);

            basePanels.add(new UISplitterLayout(rootBox, new UITextButton(TXDB.get("Save"), FontSizes.launcherTextHeight, new Runnable() {
                @Override
                public void run() {
                    rootPathBackup = rootBox.text;
                    FontSizes.save();
                }
            }), false, 1));

            basePanels.add(new UILabel(TXDB.get("Secondary Image Load Location:"), FontSizes.launcherTextHeight));

            final UITextBox sillBox = new UITextBox(secondaryImageLoadLocation, FontSizes.launcherTextHeight);
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

            basePanels.add(new UISplitterLayout(sillBox, new UITextButton(TXDB.get("Save"), FontSizes.launcherTextHeight, new Runnable() {
                @Override
                public void run() {
                    FontSizes.save();
                }
            }), false, 1));

            basePanels.add(new UILabel(TXDB.get("Choose Target Engine:"), FontSizes.launcherTextHeight));

            final IConsumer<IGPMenuPanel> menuConstructor = new IConsumer<IGPMenuPanel>() {
                @Override
                public void accept(IGPMenuPanel igpMenuPanel) {
                    gamepaks.panelsClear();
                    for (UIElement uie : basePanels)
                        gamepaks.panelsAdd(uie);
                    if (igpMenuPanel == null) {
                        closeHelper.accept(null);
                        return;
                    }
                    String[] names = igpMenuPanel.getButtonText();
                    ISupplier<IGPMenuPanel>[] runs = igpMenuPanel.getButtonActs();
                    for (int i = 0; i < names.length; i++) {
                        final ISupplier<IGPMenuPanel> r = runs[i];
                        gamepaks.panelsAdd(new UITextButton(names[i], FontSizes.launcherTextHeight, new Runnable() {
                            @Override
                            public void run() {
                                accept(r.get());
                            }
                        }));
                    }
                }
            };
            // ...

            gamepaks.setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(640), FontSizes.scaleGuess(480)));
            menuConstructor.accept(new PrimaryGPMenuPanel());
            uiTicker.accept(gamepaks);
            closeHelper.accept(new Runnable() {
                @Override
                public void run() {
                    gamepaksRequestClose.set(true);
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
                double dTTarg = (globalMS / 1000d) - compensationDT;
                double dT = GaBIEn.endFrame(dTTarg);
                compensationDT = Math.min(dTTarg, dT - dTTarg);
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
                            StringWriter sw = new StringWriter();
                            try {
                                PrintWriter pw = new PrintWriter(sw);
                                e.printStackTrace(pw);
                                pw.flush();
                            } catch (Exception e2) {
                                sw.append("\n(exception during exception print)\n");
                            }
                            AppMain.performSystemDump(true, "exception: " + sw.toString());
                            backupAvailable = true;
                        } catch (Exception finalErr) {
                            System.err.println("Failed to backup:");
                            finalErr.printStackTrace();
                            fErr = finalErr;
                        }
                        System.err.println("This is the R48 'Everything is going down the toilet' display!");
                        System.err.println("Current status: BACKUP AVAILABLE? " + backupAvailable);
                        System.err.println("Preparing file...");
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PrintStream ps = new PrintStream(baos, false, "UTF-8");
                        ps.println(TXDB.get("An error has occurred in R48. This is always the result of a bug somewhere."));
                        ps.println(TXDB.get("Version: ") + Coco.getVersion());
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
                        ps.flush();
                        System.err.println("Prepared contents...");
                        try {
                            OutputStream fos = GaBIEn.getOutFile(AdHocSaveLoad.PREFIX + "r48.error.txt");
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
                        UIScrollLayout scroll = new UIScrollLayout(true, FontSizes.generalScrollersize) {
                            @Override
                            public String toString() {
                                return "Error...";
                            }
                        };
                        scroll.panelsAdd(new UILabel(r, FontSizes.helpTextHeight));
                        scroll.setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(640), FontSizes.scaleGuess(480)));
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
            if (!gamepaksRequestClose.get())
                break;
            appTicker = null;
            // Cleanup application memory
            AppMain.shutdown();
        }
        GaBIEn.ensureQuit();
    }

    private static int runFontLoader() {
        int frames = -10; // Fadeout
        int timer2 = 0; // Baton
        String movement = " "; // the baton is 'thrown'
        // Used for two reasons.
        // 1. to work out window size during a specific situation on Android.
        // 2. on certain distributions (Arch Linux), Java still freezes up during font load,
        //     so try to pave over it and PRETEND EVERYTHING'S FINE!!!
        //    I suspect network connectivity is involved, which is odd.
        WindowSpecs ws = GaBIEn.defaultWindowSpecs("R48 Startup...", 800, 600);
        // Used to speed things up if possible
        final AtomicBoolean fontsNecessary = new AtomicBoolean(true);
        ws.scale = 1;
        ws.resizable = true;
        IGrInDriver gi = GaBIEn.makeGrIn("R48 Startup...", 800, 600, ws);
        // runs in parallel with font-load wait
        final AtomicBoolean txdbDonePrimaryTask = new AtomicBoolean(false);
        Thread txdbThread = new Thread() {
            @Override
            public void run() {
                ImageIOFormat.initializeFormats();
                TXDB.init();
                boolean canAvoidWait = FontSizes.loadLanguage();
                // TXDB 'stable', spammed class refs
                txdbDonePrimaryTask.set(true);
                // If we're setup correctly: English never needs the font-loading.
                // The reason it's important we use the correct language for this is because if font-loading is slow,
                //  we WILL (not may, WILL) freeze up until ready.
                if (canAvoidWait)
                    if (TXDB.getLanguage().equals("English"))
                        fontsNecessary.set(false);
            }
        };
        txdbThread.start();
        while (frames <= 15) {
            gi.flush(); // to kickstart w/h
            GaBIEn.endFrame(globalMS / 1000d);
            gi.clearAll(255, 255, 255);
            int sz = (Math.min(gi.getWidth(), gi.getHeight()) / 4) * 2;
            Rect ltPos = Art.r48ico;
            Rect ltPos2 = Art.r48ver;

            // note the swap on Y from - (sz / 2) because of the version
            Rect pos = new Rect((gi.getWidth() / 2) - (sz / 2), (gi.getHeight() / 2) - ((sz * 3) / 4), sz, sz);
            int fxRatio = ltPos2.width * ltPos2.height;
            int aspectMul = (ltPos2.height * fxRatio) / ltPos2.width;
            int szVHeight = (sz * aspectMul) / fxRatio;
            // this is where the "big version number" maths get changed to "little version number" maths
            Rect pos2 = new Rect(pos.x + (sz / 4), pos.y + (pos.height + (pos.height / 16)), sz / 2, szVHeight / 2);
            gi.blitScaledImage(ltPos.x, ltPos.y, ltPos.width, ltPos.height, pos.x, pos.y, pos.width, pos.height, GaBIEn.getImageEx("layertab.png", false, true));
            int margin = sz / 124;
            gi.clearRect(192, 192, 192, pos2.x - (margin * 3), pos2.y - (margin * 3), pos2.width + (margin * 6), pos2.height + (margin * 6));
            gi.clearRect(128, 128, 128, pos2.x - (margin * 2), pos2.y - (margin * 2), pos2.width + (margin * 4), pos2.height + (margin * 4));
            gi.clearRect(0, 0, 0, pos2.x - margin, pos2.y - margin, pos2.width + (margin * 2), pos2.height + (margin * 2));
            gi.blitScaledImage(ltPos2.x, ltPos2.y, ltPos2.width, ltPos2.height, pos2.x, pos2.y, pos2.width, pos2.height, GaBIEn.getImageEx("layertab.png", false, true));


            // Can't translate for several reasons (but especially no fonts).
            // This is really the only reason any of the messages are likely to be seen.
            String waitingFor = null;
            // Doesn't matter if it switches font on the last frame or something, just make sure the application remains running
            if ((!FontManager.fontsReady) && fontsNecessary.get()) {
                waitingFor = "Loading";
            } else if (!txdbDonePrimaryTask.get()) {
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
            // has to be internal-font-able, unless on Android
            int goodSize = 16;
            if (mobileExtremelySpecialBehavior)
                goodSize = gi.getHeight() / 32;
            if (goodSize < 8)
                goodSize = 8;
            int goodSizeActual = UILabel.getRecommendedTextSize("", goodSize).height;
            UILabel.drawLabel(gi, gi.getWidth(), 0, gi.getHeight() - goodSizeActual, waitingFor + movement + ch, 1, goodSize);

            // fade
            int c = Math.max(0, Math.min(255, 25 * frames)) << 24;
            gi.blitScaledImage(0, 0, 1, 1, 0, 0, gi.getWidth(), gi.getHeight(), GaBIEn.createImage(new int[] {c}, 1, 1));
        }
        int r = gi.estimateUIScaleTenths();
        gi.shutdown();
        return r;
    }

    private static void autoDetectCorrectUISize(int uiGuessScaleTenths) {
        // The above triggered a flush, which would cause the initial resize on SWPs.
        // That then allowed it to estimate a correct scale which ended up here.
        FontSizes.uiGuessScaleTenths = uiGuessScaleTenths;
        boolean mobile = GaBIEn.singleWindowApp();
        for (FontSizes.FontSizeField fsf : FontSizes.getFields()) {
            // as this is a touch device, map 8 to 16 (6 is for things that really matter)
            if (mobile)
                if (fsf.get() == 8)
                    fsf.accept(16);
            // uiGuessScaleTenths was set manually.
            if (!fsf.name.equals("uiGuessScaleTenths"))
                fsf.accept(FontSizes.scaleGuess(fsf.get()));
        }
        // exceptions
        if (mobile)
            FontSizes.tilesTabTextHeight *= 2;
    }

    private static UIElement figureOutTopBar(final WindowCreatingUIElementConsumer uiTicker, final IConsumer<Runnable> closeHelper) {
        UIElement whatever = new UITextButton(TXDB.get("Quit R48"), FontSizes.launcherTextHeight, new Runnable() {
            @Override
            public void run() {
                GaBIEn.ensureQuit();
            }
        });
        if (!GaBIEn.singleWindowApp()) { // SWA means we can't create windows
            whatever = new UISplitterLayout(whatever, new UITextButton(TXDB.get("Configuration"), FontSizes.launcherTextHeight, new Runnable() {
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

    // Only use from AppMain's "pleaseShutdown"
    public static void shutdownAllAppMainWindows() {
        for (UIElement uie : uiTicker.runningWindows())
            uiTicker.forceRemove(uie);
    }
}

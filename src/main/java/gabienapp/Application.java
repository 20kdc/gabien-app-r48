/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TXDB;
import r48.maptools.UIMTBase;
import r48.ui.UIAppendButton;
import r48.ui.UIFontSizeConfigurator;
import r48.ui.help.UIHelpSystem;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 1/27/17.
 */
public class Application {
    public static int globalMS = 33;
    private static IConsumer<Double> appTicker = null;

    public static void gabienmain() throws IOException {
        final WindowCreatingUIElementConsumer uiTicker = new WindowCreatingUIElementConsumer();
        // Load language list.
        TXDB.init();
        FontSizes.load();
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

            gamepaks.panels.add(new UISplitterLayout(new UITextButton(FontSizes.launcherTextHeight, TXDB.get("Quit R48"), new Runnable() {
                @Override
                public void run() {
                    GaBIEn.ensureQuit();
                }
            }), new UIAppendButton(TXDB.getLanguage(), new UITextButton(FontSizes.launcherTextHeight, TXDB.get("Font Sizes"), new Runnable() {
                @Override
                public void run() {
                    uiTicker.accept(new UIFontSizeConfigurator());
                    closeHelper.accept(null);
                }
            }), new Runnable() {
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
            }, FontSizes.launcherTextHeight), false, 1, 2));

            gamepaks.panels.add(new UISplitterLayout(new UILabel(TXDB.get("MS per frame:"), FontSizes.launcherTextHeight), msAdjust, false, 3, 5));

            gamepaks.panels.add(new UILabel(TXDB.get("Root Path:"), FontSizes.launcherTextHeight));

            final UITextBox rootBox = new UITextBox(FontSizes.launcherTextHeight);
            gamepaks.panels.add(rootBox);

            gamepaks.panels.add(new UILabel(TXDB.get("Choose Target Engine:"), FontSizes.launcherTextHeight));

            DBLoader.readFile("Gamepaks.txt", new IDatabase() {

                UITextButton lastButton;
                AtomicReference<String> boxedEncoding; // it's a boxed object, so...

                @Override
                public void newObj(int objId, final String objName) throws IOException {
                    final AtomicReference<String> box = new AtomicReference<String>();
                    boxedEncoding = box;
                    lastButton = new UITextButton(FontSizes.enumChoiceTextHeight, objName, new Runnable() {
                        @Override
                        public void run() {
                            if (appTicker == null) {
                                try {
                                    RubyIO.encoding = box.get();
                                    String rootPath = rootBox.text;
                                    if (!rootPath.equals(""))
                                        if (!rootPath.endsWith("/"))
                                            if (!rootPath.endsWith("\\"))
                                                rootPath += "/";
                                    TXDB.loadGamepakLanguage(objName + "/");
                                    appTicker = AppMain.initializeAndRun(rootPath, objName + "/", uiTicker);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            closeHelper.accept(null);
                        }
                    });
                    InputStream tester = GaBIEn.getFile(objName + "/Schema.txt");
                    if (tester != null) {
                        gamepaks.panels.add(lastButton);
                        tester.close();
                    }
                }

                @Override
                public void execCmd(char c, String[] args) throws IOException {
                    if (c == '.') {
                        String rn = "";
                        for (String s : args)
                            rn += s + " ";
                        lastButton.Text = rn;
                    }
                    if (c == 'e')
                        boxedEncoding.set(args[0]);
                    if (c == 'l')
                        if (args[0].equals(TXDB.getLanguage())) {
                            String rn = "";
                            for (int i = 1; i < args.length; i++)
                                rn += args[i] + " ";
                            lastButton.Text = rn;
                        }

                /*
                 * if (c == 'f')
                 *     if (!new File(args[0]).exists()) {
                 *         System.out.println("Can't use " + lastButton.Text + ": " + args[0] + " missing");
                 *         gamepaks.panels.remove(lastButton);
                 *     }
                 */
                }
            });

            gamepaks.setBounds(new Rect(0, 0, 640, 480));
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
                double dT = GaBIEn.timeDelta(false);
                while (dT < (globalMS / 1000d)) {
                    try {
                        Thread.sleep(globalMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dT = GaBIEn.timeDelta(false);
                }
                dT = GaBIEn.timeDelta(true);
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
                            fErr.printStackTrace();
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
                        } catch (IOException ioe) {
                            ps.println("The error could not be saved.");
                            System.err.println("Failed to save!");
                        }
                        System.err.println("Displaying situation to user");
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

    // Magically handles case issues.
    public static String autoDetectWindows(String s) {
        final String giveUp = s;
        try {
            // '/' is 'universal'. Not supposed to be, but that doesn't matter
            s = s.replace('\\', '/');
            if (s.equals(""))
                return s;
            if (!s.contains("/"))
                if (s.contains(":"))
                    return s;
            File f = new File(s);
            if (f.exists())
                return s;
            // Deal with earlier path components...
            String st = f.getName();
            // Sanity check.
            if (s.contains("/")) {
                if (!s.endsWith("/" + st))
                    throw new RuntimeException("Weird inconsistency in Java File. 'Should never happen' but safety first.");
            } else {
                // Change things to make sense.
                s = "./" + st;
            }
            String parent = autoDetectWindows(s.substring(0, s.length() - (st.length() + 1)));
            f = new File(parent);
            String[] subfiles = f.list();
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
}

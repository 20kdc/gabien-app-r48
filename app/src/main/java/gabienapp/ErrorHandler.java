/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.WindowCreatingUIElementConsumer;
import r48.AdHocSaveLoad;
import r48.App;
import r48.app.AppMain;
import r48.app.IAppAsSeenByLauncher;
import r48.dbs.TXDB;
import r48.wm.Coco;

/**
 * Error handling tracker.
 * Created 27th February 2023.
 */
public class ErrorHandler {
    // This is the identity of the error window that 'brings the system down softly'.
    public UIElement failed = null;
    public final Launcher lun;

    public ErrorHandler(Launcher lun) {
        this.lun = lun;
    }

    // If this returns true, the app must be stopped
    public boolean handle(@Nullable IAppAsSeenByLauncher app, Exception e, WindowCreatingUIElementConsumer uiTicker) {
        // ok, so, 'what is going on with the flags', you might ask?
        // Well:
        // backupAvailable describes the state of the LAST backup made
        // Emergency backups always occur JUST BEFORE message writing time
        // weHaveSecondary indicates if any secondary backup ever completed during this run,
        //  which makes it worth keeping.
        if (failed == null) {
            System.err.println("-- ERROR HANDLER --");
            e.printStackTrace();
            Exception fErr = null;
            if (app != null) {
                try {
                    StringWriter sw = new StringWriter();
                    try {
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        pw.flush();
                    } catch (Exception e2) {
                        sw.append("\n(exception during exception print)\n");
                    }
                    AppMain.performSystemDump((App) app, true, "exception: " + sw.toString());
                } catch (Exception finalErr) {
                    System.err.println("Failed to backup:");
                    finalErr.printStackTrace();
                    fErr = finalErr;
                }
                System.err.println("This is the R48 'Everything is going down the toilet' display!");
                System.err.println("Current status: BACKUP AVAILABLE? " + (fErr != null ? "NO" : "YES"));
            } else {
                System.err.println("Exception in launcher, so nothing to panic about");
            }
            System.err.println("Preparing file...");
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps;
            try {
                ps = new PrintStream(baos, false, "UTF-8");
            } catch (UnsupportedEncodingException e2) {
                throw new RuntimeException(e2);
            }
            ps.println(TXDB.get("An error has occurred in R48. This is always the result of a bug somewhere."));
            ps.println(TXDB.get("Version: ") + Coco.getVersion());
            if (app != null) {
                ps.println(TXDB.get("If the rest of R48 disappeared, that means a second error occurred, and R48 has shut down to keep this message up."));
                ps.println(TXDB.get("This is because, if backups failed, then Save would fail anyway - and without these instructions, you're kind of doomed."));
                if (fErr == null) {
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
                lun.shutdownAllAppMainWindows();
            }

            String r;
            try {
                r = baos.toString("UTF-8").replaceAll("\r", "");
            } catch (UnsupportedEncodingException e1) {
                throw new RuntimeException(e1);
            }
            UIScrollLayout scroll = new UIScrollLayout(true, lun.c.f.generalScrollersize) {
                @Override
                public String toString() {
                    return "Error...";
                }
            };
            scroll.panelsAdd(new UILabel(r, lun.c.f.helpTextHeight));
            scroll.setForcedBounds(null, new Rect(0, 0, lun.c.f.scaleGuess(640), lun.c.f.scaleGuess(480)));
            uiTicker.accept(scroll);
            failed = scroll;
            System.err.println("Well, that worked at least");
            return false;
        } else {
            e.printStackTrace();
            return true;
        }
    }

}

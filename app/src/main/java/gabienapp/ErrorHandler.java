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
import gabien.ui.UIElement;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UIScrollLayout;
import gabien.uslx.append.Rect;
import r48.AdHocSaveLoad;
import r48.App;
import r48.app.AppMain;
import r48.app.IAppAsSeenByLauncher;
import r48.tr.pages.TrRoot;
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
            TrRoot T = lun.ilg.t;
            // don't use MVM stuff around here
            ps.println(T.g.err_hasOccurred + Coco.getVersion());
            if (app != null) {
                ps.println(T.g.err_appWasStarted);
                if (fErr == null) {
                    ps.println(T.g.err_backupOk);
                } else {
                    ps.println(T.g.err_backupFail);
                    ps.println("----");
                    fErr.printStackTrace(ps);
                    ps.println("----");
                }
            }
            ps.println(T.g.err_footer);
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
            UIScrollLayout scroll = new UIScrollLayout(true, lun.c.f.generalS) {
                @Override
                public String toString() {
                    return "Error...";
                }
            };
            scroll.panelsAdd(new UILabel(r, lun.c.f.helpTH));
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

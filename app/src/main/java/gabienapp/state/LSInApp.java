/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp.state;

import gabien.ui.UIElement;
import gabien.uslx.append.IConsumer;
import gabienapp.ErrorHandler;
import gabienapp.Launcher;
import gabienapp.Launcher.State;
import r48.App;
import r48.app.AppMain;

/**
 * Main state of the launcher.
 * Created 27th February 2023.
 */
public class LSInApp extends State {
    final ErrorHandler errorHandler;

    public IConsumer<Double> appTicker = null;
    public App app = null;

    public LSInApp(Launcher lun, App app) {
        super(lun);
        this.app = app;
        errorHandler = new ErrorHandler(lun);
    }

    @Override
    public void tick(double dT) {
        if (lun.uiTicker.runningWindows().size() == 0) {
            // Cleanup application memory
            AppMain.shutdown(app);
            app = null;
            appTicker = null;
            // Next state
            lun.currentState = null;
            if (errorHandler.failed != null)
                return;
            lun.currentState = new LSMain(lun);
            return;
        }
        try {
            if (appTicker != null)
                appTicker.accept(dT);
            lun.uiTicker.runTick(dT);
        } catch (Exception e) {
            if (errorHandler.handle(app, e, lun.uiTicker)) {
                // Shut down R48 to 'stem the bleeding'.
                // Need to preserve the notice to the user. If all backups failed then the user is screwed anyway, so just tell the user what their options are.
                for (UIElement uie : lun.uiTicker.runningWindows()) {
                    if (uie != errorHandler.failed) {
                        try {
                            lun.uiTicker.forceRemove(uie);
                        } catch (Exception e3) {
                            // just in case of rogue windowClosed
                        }
                    }
                }
                appTicker = null;
                try {
                    AppMain.shutdown(app);
                } catch (Exception e4) {

                }
            }
        }
    }

}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp.state;

import gabien.ui.UIElement;
import gabienapp.ErrorHandler;
import gabienapp.Launcher;
import gabienapp.UILauncher;
import gabienapp.Launcher.State;

/**
 * Main state of the launcher.
 * Created 27th February 2023.
 */
public class LSMain extends State {
    final ErrorHandler errorHandler;
    public final UILauncher uiLauncher;

    public LSMain(Launcher lun) {
        super(lun);
        errorHandler = new ErrorHandler(lun);
        uiLauncher = new UILauncher(this);
        lun.uiTicker.accept(uiLauncher);
    }

    @Override
    public void tick(double dT) {
        if (lun.uiTicker.runningWindows().size() == 0) {
            // Shutdown
            lun.currentState = null;
            return;
        }
        try {
            lun.uiTicker.runTick(dT);
        } catch (Exception e) {
            if (errorHandler.handle(null, e, lun.uiTicker)) {
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
            }
        }
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import gabien.ui.WindowCreatingUIElementConsumer;
import gabienapp.state.LSInApp;
import r48.App;

/**
 * Created on April 18, 2019.
 */
public class GrandLauncherUtils {
    public final Launcher launcher;
    public GrandLauncherUtils(Launcher l) {
        launcher = l;
    }

    public WindowCreatingUIElementConsumer getTicker() {
        return launcher.uiTicker;
    }

    public App getApp() {
        Launcher.State cs = launcher.currentState;
        if (cs instanceof LSInApp)
            return (App) ((LSInApp) cs).app;
        return null;
    }
}

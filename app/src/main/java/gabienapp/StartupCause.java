/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import gabienapp.state.LSInApp;
import gabienapp.state.LSMain;
import r48.App;
import r48.app.AppMain;
import r48.app.EngineDef;
import r48.io.IObjectBackend;
import r48.io.PathUtils;

/**
 * Separated 5th April 2023.
 */
public class StartupCause implements Runnable {
    private final String box;
    private final String objName;
    private final LSMain ls;

    public StartupCause(LSMain ls, String box, String objName) {
        this.ls = ls;
        this.box = box;
        this.objName = objName;
    }

    @Override
    public void run() {
        if (ls.lun.currentState == ls) {
            try {
                IObjectBackend.Factory.encoding = Charset.forName(box);
            } catch (UnsupportedCharsetException uce) {
                throw new RuntimeException(uce);
            }
            final String rootPath = PathUtils.fixRootPath(ls.uiLauncher.rootBox.text.text);
            final String silPath = PathUtils.fixRootPath(ls.uiLauncher.sillBox.text.text);

            final LSInApp lia = new LSInApp(ls.lun);
            ls.lun.currentState = lia;

            // Start fancy loading screen.
            final UIFancyInit theKickstart = new UIFancyInit(ls.lun.c);
            ls.lun.uiTicker.accept(theKickstart);
            ls.uiLauncher.requestClose();
            new Thread(() -> {
                try {
                    EngineDef engine = ls.lun.ilg.getEngineDef(objName);
                    if (engine == null)
                        throw new RuntimeException("EngineDef " + objName + " missing!");
                    // Regarding thread safety, this should be safe enough because app is kept here.
                    // It's then transferred out.
                    App app = AppMain.initializeCore(ls.lun.ilg, rootPath, silPath, engine, theKickstart);
                    AppMain.initializeUI(app, ls.lun.uiTicker, ls.lun.isMobile);
                    theKickstart.doneInjector.set(() -> {
                        lia.app = app;
                        app.ui.finishInitialization();
                    });
                } catch (final RuntimeException e) {
                    theKickstart.doneInjector.set(() -> {
                        throw e;
                    });
                }
            }).start();
        }
    }
}
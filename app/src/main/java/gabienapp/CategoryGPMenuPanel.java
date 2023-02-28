/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import gabien.uslx.append.*;
import gabienapp.state.LSInApp;
import gabienapp.state.LSMain;
import r48.App;
import r48.app.AppMain;
import r48.cfg.Config;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.PathUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class CategoryGPMenuPanel implements IGPMenuPanel {
    public LinkedList<String> res1 = new LinkedList<String>();
    public LinkedList<IFunction<LauncherState, IGPMenuPanel>> res2 = new LinkedList<IFunction<LauncherState, IGPMenuPanel>>();
    public final LSMain ls;
    public final Config c;

    public CategoryGPMenuPanel(LSMain ls, final IGPMenuPanel root, final String category) {
        this.ls = ls;
        this.c = ls.lun.c;
        res1.add(TXDB.get("Back..."));
        res2.add(new IFunction<LauncherState, IGPMenuPanel>() {
            @Override
            public IGPMenuPanel apply(LauncherState ls) {
                return root;
            }
        });
        DBLoader.readFile(null, "Gamepaks.txt", new IDatabase() {
            AtomicReference<String> boxedEncoding; // it's a boxed object, so...
            boolean doWeCare = false;

            @Override
            public void newObj(int objId, final String objName) throws IOException {
                if (!doWeCare)
                    return;
                final AtomicReference<String> box = new AtomicReference<String>();
                boxedEncoding = box;
                res1.add(objName);
                res2.add(new StartupCause(ls, box, objName));
            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == '=') {
                    doWeCare = args[0].equals(category);
                    return;
                }
                if (!doWeCare)
                    return;
                if (c == '.') {
                    String rn = "";
                    for (String s : args)
                        rn += s + " ";
                    res1.set(res1.size() - 1, TXDB.get("launcher", rn));
                }
                if (c == 'e')
                    boxedEncoding.set(args[0]);

                /*
                 * if (c == 'f')
                 *     if (!new File(args[0]).exists()) {
                 *         System.out.println("Can't use " + lastButton.Text + ": " + args[0] + " missing");
                 *         gamepaks.panels.remove(lastButton);
                 *     }
                 */
            }
        });
    }

    @Override
    public String[] getButtonText() {
        return res1.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IFunction<LauncherState,IGPMenuPanel>[] getButtonActs() {
        // *sighs*
        return res2.toArray(new IFunction[0]);
    }

    public static class StartupCause implements IFunction<LauncherState, IGPMenuPanel> {
        private final AtomicReference<String> box;
        private final String objName;
        private final LSMain ls;

        public StartupCause(LSMain ls, AtomicReference<String> box, String objName) {
            this.ls = ls;
            this.box = box;
            this.objName = objName;
        }

        @Override
        public IGPMenuPanel apply(LauncherState ls2) {
            if (ls.lun.currentState == ls) {
                IObjectBackend.Factory.encoding = box.get();
                final String rootPath = PathUtils.fixRootPath(ls2.rootPath);
                final String silPath = PathUtils.fixRootPath(ls2.secondaryImagePath);

                final LSInApp lia = new LSInApp(ls.lun);
                ls.lun.currentState = lia;

                // Start fancy loading screen.
                final UIFancyInit theKickstart = new UIFancyInit(ls.lun.c);
                ls.lun.uiTicker.accept(theKickstart);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            TXDB.loadGamepakLanguage(objName + "/");
                            // Regarding thread safety, this should be safe enough because app is kept here.
                            // It's then transferred out.
                            App app = AppMain.initializeCore(ls.lun.ilg, rootPath, silPath, objName + "/", theKickstart);
                            AppMain.initializeUI(app, ls.lun.uiTicker, ls.lun.isMobile);
                            theKickstart.doneInjector.set(new Runnable() {
                                @Override
                                public void run() {
                                    lia.app = app;
                                    app.ui.finishInitialization();
                                }
                            });
                        } catch (final RuntimeException e) {
                            theKickstart.doneInjector.set(new Runnable() {
                                @Override
                                public void run() {
                                    throw e;
                                }
                            });
                        }
                    }
                }.start();
            }
            return null;
        }
    }
}

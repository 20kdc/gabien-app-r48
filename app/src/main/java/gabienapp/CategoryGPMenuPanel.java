/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import gabienapp.state.LSInApp;
import gabienapp.state.LSMain;
import r48.App;
import r48.app.AppMain;
import r48.app.EngineDef;
import r48.cfg.Config;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.tr.pages.TrRoot;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class CategoryGPMenuPanel implements IGPMenuPanel {
    public LinkedList<String> res1 = new LinkedList<String>();
    public LinkedList<Runnable> res2 = new LinkedList<Runnable>();
    public final LSMain ls;
    public final Config c;

    public CategoryGPMenuPanel(LSMain ls, final IGPMenuPanel root, final String category) {
        this.ls = ls;
        this.c = ls.lun.c;
        final TrRoot T = ls.lun.ilg.t;
        res1.add(T.g.bBack);
        res2.add(new Runnable() {
            @Override
            public void run() {
                ls.uiLauncher.setPanel(root);
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
                if (c == '.')
                    res1.set(res1.size() - 1, ls.trL(args[0]));
                if (c == 'e')
                    boxedEncoding.set(args[0]);
            }
        });
    }

    @Override
    public String[] getButtonText() {
        return res1.toArray(new String[0]);
    }

    @Override
    public Runnable[] getButtonActs() {
        // *sighs*
        return res2.toArray(new Runnable[0]);
    }

    public static class StartupCause implements Runnable {
        private final AtomicReference<String> box;
        private final String objName;
        private final LSMain ls;

        public StartupCause(LSMain ls, AtomicReference<String> box, String objName) {
            this.ls = ls;
            this.box = box;
            this.objName = objName;
        }

        @Override
        public void run() {
            if (ls.lun.currentState == ls) {
                IObjectBackend.Factory.encoding = box.get();
                final String rootPath = PathUtils.fixRootPath(ls.uiLauncher.rootBox.text.text);
                final String silPath = PathUtils.fixRootPath(ls.uiLauncher.sillBox.text.text);

                final LSInApp lia = new LSInApp(ls.lun);
                ls.lun.currentState = lia;

                // Start fancy loading screen.
                final UIFancyInit theKickstart = new UIFancyInit(ls.lun.c);
                ls.lun.uiTicker.accept(theKickstart);
                new Thread() {
                    @Override
                    public void run() {
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
                            ls.uiLauncher.requestClose();
                        } catch (final RuntimeException e) {
                            theKickstart.doneInjector.set(() -> {
                                throw e;
                            });
                        }
                    }
                }.start();
            }
        }
    }
}

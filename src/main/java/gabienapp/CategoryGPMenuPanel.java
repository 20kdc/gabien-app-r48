/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabienapp;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import r48.AppMain;
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
    public LinkedList<ISupplier<IGPMenuPanel>> res2 = new LinkedList<ISupplier<IGPMenuPanel>>();

    public CategoryGPMenuPanel(final IGPMenuPanel root, final String category) {
        res1.add(TXDB.get("Back..."));
        res2.add(new ISupplier<IGPMenuPanel>() {
            @Override
            public IGPMenuPanel get() {
                return root;
            }
        });
        DBLoader.readFile("Gamepaks.txt", new IDatabase() {
            AtomicReference<String> boxedEncoding; // it's a boxed object, so...
            boolean doWeCare = false;

            @Override
            public void newObj(int objId, final String objName) throws IOException {
                if (!doWeCare)
                    return;
                final AtomicReference<String> box = new AtomicReference<String>();
                boxedEncoding = box;
                res1.add(objName);
                res2.add(new StartupCause(box, objName));
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

    @Override
    public ISupplier<IGPMenuPanel>[] getButtonActs() {
        // *sighs*
        return res2.toArray(new ISupplier[0]);
    }

    public static class StartupCause implements ISupplier<IGPMenuPanel> {
        private final AtomicReference<String> box;
        private final String objName;

        public StartupCause(AtomicReference<String> box, String objName) {
            this.box = box;
            this.objName = objName;
        }

        @Override
        public IGPMenuPanel get() {
            if (Application.appTicker == null) {
                IObjectBackend.Factory.encoding = box.get();
                final String rootPath = PathUtils.fixRootPath(Application.rootBox.text);

                // Start fancy loading screen.
                final UIFancyInit theKickstart = new UIFancyInit();
                Application.uiTicker.accept(theKickstart);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            TXDB.loadGamepakLanguage(objName + "/");
                            AppMain.initializeCore(rootPath, objName + "/");
                            final ISupplier<IConsumer<Double>> appTickerGen = AppMain.initializeUI(Application.uiTicker);
                            theKickstart.doneInjector.set(new Runnable() {
                                @Override
                                public void run() {
                                    // The .get() must occur here, after the window is absolutely definitely gone.
                                    Application.appTicker = appTickerGen.get();
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

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.ui.ISupplier;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TXDB;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import static gabienapp.Application.mobileExtremelySpecialBehavior;

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
                res2.add(new ISupplier<IGPMenuPanel>() {
                    @Override
                    public IGPMenuPanel get() {
                        if (Application.appTicker == null) {
                            try {
                                RubyIO.encoding = box.get();
                                String rootPath = Application.rootBox.text;
                                if (!rootPath.equals(""))
                                    if (!rootPath.endsWith("/"))
                                        if (!rootPath.endsWith("\\"))
                                            rootPath += "/";
                                if (mobileExtremelySpecialBehavior)
                                    TXDB.loadGamepakLanguage(objName + "/");
                                Application.appTicker = AppMain.initializeAndRun(rootPath, objName + "/", Application.uiTicker);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                });
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
}

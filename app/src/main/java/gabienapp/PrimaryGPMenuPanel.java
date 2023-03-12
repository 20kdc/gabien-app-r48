/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import gabienapp.state.LSInApp;
import gabienapp.state.LSMain;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.tr.pages.TrRoot;
import r48.ui.dialog.UIReadEvaluatePrintLoop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class PrimaryGPMenuPanel implements IGPMenuPanel {
    public LinkedList<String> res1 = new LinkedList<String>();
    public LinkedList<Runnable> res2 = new LinkedList<Runnable>();

    public PrimaryGPMenuPanel(final LSMain ls) {
        final TrRoot T = ls.lun.ilg.t;
        // Loads everything
        DBLoader.readFile(null, "Gamepaks.txt", new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == '=') {
                    final CategoryGPMenuPanel cat = new CategoryGPMenuPanel(ls, PrimaryGPMenuPanel.this, args[0]);
                    res1.add(ls.trL(args[0]));
                    res2.add(() -> {
                        ls.uiLauncher.setPanel(cat);
                    });
                }
            }
        });
        res1.add(T.g.noGameMode);
        res2.add(new CategoryGPMenuPanel.StartupCause(ls, new AtomicReference<String>("UTF-8"), "null"));
        res1.add(T.g.accessLauncherREPL);
        res2.add(() -> {
            String title = T.t.launcherREPL;
            UIReadEvaluatePrintLoop repl = new UIReadEvaluatePrintLoop(ls.c, ls.lun.vmCtx, title);
            ls.lun.uiTicker.accept(repl);
            ls.lun.currentState = new LSInApp(ls.lun);
        });
        res1.add(T.g.dumpLLang);
        res2.add(() -> {
            ls.translationDump("L-", "launcher/");
            res1.removeLast();
            res2.removeLast();
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
}

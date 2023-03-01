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
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.ui.dialog.UIReadEvaluatePrintLoop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class PrimaryGPMenuPanel implements IGPMenuPanel {
    public LinkedList<String> res1 = new LinkedList<String>();
    public LinkedList<IFunction<LauncherState, IGPMenuPanel>> res2 = new LinkedList<IFunction<LauncherState, IGPMenuPanel>>();

    public PrimaryGPMenuPanel(final LSMain ls) {
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
                    res2.add(new IFunction<LauncherState, IGPMenuPanel>() {
                        @Override
                        public IGPMenuPanel apply(LauncherState ls) {
                            return cat;
                        }
                    });
                }
            }
        });
        res1.add(ls.tr("'No Game' Mode"));
        res2.add(new CategoryGPMenuPanel.StartupCause(ls, new AtomicReference<String>("UTF-8"), "Null"));
        res1.add(ls.tr("Access Launcher REPL"));
        res2.add(new IFunction<LauncherState, IGPMenuPanel>() {
            @Override
            public IGPMenuPanel apply(LauncherState ls2) {
                String title = ls.tr("R48 Launcher REPL");
                UIReadEvaluatePrintLoop repl = new UIReadEvaluatePrintLoop(ls.c, ls.lun.vmCtx, title);
                ls.lun.uiTicker.accept(repl);
                ls.lun.currentState = new LSInApp(ls.lun);
                return null;
            }
        });
        res1.add(ls.tr("Dump L-<lang>.txt"));
        res2.add(new IFunction<LauncherState, IGPMenuPanel>() {
            @Override
            public IGPMenuPanel apply(LauncherState ls2) {
                ls.translationDump("L-", "launcher/");
                res1.removeLast();
                res2.removeLast();
                return PrimaryGPMenuPanel.this;
            }
        });
    }

    @Override
    public String[] getButtonText() {
        return res1.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IFunction<LauncherState, IGPMenuPanel>[] getButtonActs() {
        // *sighs*
        return res2.toArray(new IFunction[0]);
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabienapp;

import gabien.ui.ISupplier;
import r48.dbs.DBLoader;
import r48.dbs.EscapedStringSyntax;
import r48.dbs.IDatabase;
import r48.dbs.TXDB;

import java.io.IOException;
import java.util.LinkedList;

public class PrimaryGPMenuPanel implements IGPMenuPanel {
    public LinkedList<String> res1 = new LinkedList<String>();
    public LinkedList<ISupplier<IGPMenuPanel>> res2 = new LinkedList<ISupplier<IGPMenuPanel>>();

    public PrimaryGPMenuPanel() {
        // Loads everything
        DBLoader.readFile("Gamepaks.txt", new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == '=') {
                    String tx = args[0];
                    final CategoryGPMenuPanel cat = new CategoryGPMenuPanel(PrimaryGPMenuPanel.this, args[0]);
                    res1.add(TXDB.get("launcher", tx));
                    res2.add(new ISupplier<IGPMenuPanel>() {
                        @Override
                        public IGPMenuPanel get() {
                            return cat;
                        }
                    });
                }
            }
        });
        res1.add(TXDB.get("Dump L-<lang>.txt"));
        res2.add(new ISupplier<IGPMenuPanel>() {
            @Override
            public IGPMenuPanel get() {

                TXDB.performDump("L-", "launcher/");
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

    @Override
    public ISupplier<IGPMenuPanel>[] getButtonActs() {
        // *sighs*
        return res2.toArray(new ISupplier[0]);
    }
}

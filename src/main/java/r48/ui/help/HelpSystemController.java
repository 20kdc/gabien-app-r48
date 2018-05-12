/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.help;

import gabien.GaBIEn;
import gabien.ui.UILabel;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.TXDB;

import java.io.IOException;
import java.io.InputStream;

/**
 * One of those mind-controlling classes that oversees everything!
 * Created on 04/06/17.
 */
public class HelpSystemController {
    private UILabel pageName;
    private String helpFile;
    private UIHelpSystem hs;
    public Runnable onLoad;

    public HelpSystemController(UILabel pName, String hFile, UIHelpSystem charge) {
        pageName = pName;
        helpFile = hFile;
        hs = charge;
    }

    public void loadPage(final int i) {
        hs.page.clear();
        InputStream helpStream = getHelpStream();
        if (helpStream != null) {
            DBLoader.readFile(helpStream, new IDatabase() {
                boolean working = false;

                @Override
                public void newObj(int objId, String objName) throws IOException {
                    if (objId == i) {
                        if (pageName != null)
                            pageName.text = objName;
                        working = true;
                    } else {
                        working = false;
                    }
                }

                @Override
                public void execCmd(char c, String[] args) throws IOException {
                    if (working)
                        hs.page.add(new UIHelpSystem.HelpElement(c, args));
                }
            });
            hs.runLayout();
        } else {
            System.err.println("Unable to get at help file");
        }
        if (onLoad != null)
            onLoad.run();
    }

    private InputStream getHelpStream() {
        if (helpFile == null) {
            // Local language?
            InputStream inp = GaBIEn.getResource("Help" + TXDB.getLanguage() + ".txt");
            if (inp == null)
                return GaBIEn.getResource("Help.txt");
            return inp;
        }
        InputStream inp = GaBIEn.getResource(helpFile + TXDB.getLanguage() + ".txt");
        if (inp == null)
            return GaBIEn.getResource(helpFile + ".txt");
        return inp;
    }

}

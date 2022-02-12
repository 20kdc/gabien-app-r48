/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.help;

import gabien.GaBIEn;
import gabien.uslx.append.*;
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
public class HelpSystemController implements IConsumer<String> {
    private UILabel pageName;
    private String helpFile;
    private UIHelpSystem hs;
    public Runnable onLoad;

    public HelpSystemController(UILabel pName, String hFile, UIHelpSystem charge) {
        pageName = pName;
        helpFile = hFile == null ? "Help/Main/Entry" : hFile;
        hs = charge;
    }

    public void accept(final String link) {
        if (link.contains(":")) {
            String[] coms = link.split(":");
            helpFile = coms[0];
            loadPage(Integer.parseInt(coms[1]));
        } else {
            loadPage(Integer.parseInt(link));
        }
    }

    public void loadPage(final int i) {
        hs.page.clear();
        String efl = TXDB.getLanguage();
        if (efl.equals("English"))
            efl = "";
        InputStream helpStream = GaBIEn.getResource(helpFile + efl + ".txt");
        if (helpStream == null) {
            hs.page.add(new UIHelpSystem.HelpElement('.', TXDB.get("This helpfile is unavailable in your language; the English version has been displayed.")));
            helpStream = GaBIEn.getResource(helpFile + ".txt");
        }
        if (helpStream != null) {
            DBLoader.readFile(helpStream, new IDatabase() {
                boolean working = false;
                UIHelpSystem.HelpElement workingElement;

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
                    if (working) {
                        StringBuilder argbuilder = new StringBuilder();
                        boolean first = true;
                        for (String s : args) {
                            if (first) {
                                first = false;
                            } else {
                                argbuilder.append(' ');
                            }
                            argbuilder.append(s);
                        }
                        if (c == ',') {
                            ((UILabel) workingElement.element).text += "\n" + argbuilder.toString();
                        } else {
                            hs.page.add(workingElement = new UIHelpSystem.HelpElement(c, argbuilder.toString()));
                        }
                    }
                }
            });
            hs.runLayoutLoop();
        }
        if (onLoad != null)
            onLoad.run();
    }

}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.help;

import gabien.GaBIEn;
import gabien.ui.elements.UILabel;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.tr.LanguageList;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * One of those mind-controlling classes that oversees everything!
 * Created on 04/06/17.
 */
public class HelpSystemController implements Consumer<String> {
    private UILabel pageName;
    private String helpFile;
    private UIHelpSystem hs;
    public Runnable onLoad;
    public final Config c;

    public HelpSystemController(UILabel pName, String hFile, UIHelpSystem charge) {
        c = charge.c;
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
        final InterlaunchGlobals ilg = hs.ilg;
        String efl = c.language;
        if (efl.equals(LanguageList.helpLang))
            efl = "";
        String actualFN = helpFile + efl + ".txt";
        InputStream helpStream = GaBIEn.getResource(actualFN);
        if (helpStream == null) {
            hs.page.add(new UIHelpSystem.HelpElement(ilg, '.', hs.ilg.t.g.helpUnavailable));
            actualFN = helpFile + ".txt";
            helpStream = GaBIEn.getResource(actualFN);
        }
        if (helpStream != null) {
            DBLoader.readFile(actualFN, helpStream, new IDatabase() {
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
                public void execCmd(char ch, String[] args) throws IOException {
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
                        if (ch == ',') {
                            ((UILabel) workingElement.element).text += "\n" + argbuilder.toString();
                        } else {
                            hs.page.add(workingElement = new UIHelpSystem.HelpElement(ilg, ch, argbuilder.toString()));
                        }
                    }
                }
            });
            hs.tightlyCoupledLayoutRecalculateMetrics();
        }
        if (onLoad != null)
            onLoad.run();
    }

}

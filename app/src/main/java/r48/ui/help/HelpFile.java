/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

import gabien.GaBIEn;
import gabien.ui.elements.UILabel;
import r48.app.InterlaunchGlobals;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.tr.LanguageList;

/**
 * Created 14th May, 2024.
 */
public class HelpFile {
    public final HashMap<Integer, Page> pages = new HashMap<>();

    public static class Page {
        public final String name;
        public final LinkedList<UIHelpSystem.HelpElement> contents = new LinkedList<>();

        public Page(String n) {
            name = n;
        }
    }

    public static HelpFile load(final InterlaunchGlobals ilg, final String helpFile) {
        HelpFile hf = new HelpFile();
        String efl = ilg.c.language;
        if (efl.equals(LanguageList.helpLang))
            efl = "";
        String actualFN = helpFile + efl;
        InputStream helpStream = GaBIEn.getResource(actualFN + ".scm");
        boolean wasLoadedInNativeLanguage = helpStream != null;
        if (helpStream == null) {
            actualFN = helpFile;
            helpStream = GaBIEn.getResource(actualFN + ".scm");
        }
        if (helpStream != null) {
            try {
                helpStream.close();
            } catch (Exception ex) {
                // nope
            }
        }
        DBLoader.readFile(actualFN, new IDatabase() {
            Page helpPage;
            UIHelpSystem.HelpElement workingElement;

            @Override
            public void newObj(int objId, String objName) throws IOException {
                helpPage = new Page(objName);
                if (!wasLoadedInNativeLanguage)
                    helpPage.contents.add(new UIHelpSystem.HelpElement(ilg, ".", ilg.t.g.helpUnavailable));
                hf.pages.put(objId, helpPage);
            }

            @Override
            public void execCmd(String ch, String[] args) throws IOException {
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
                if (ch.equals(",")) {
                    UILabel uil = ((UILabel) workingElement.element);
                    // this is bad code and I should feel bad
                    // luckily this only happens on pageswitch
                    // REST of layout should be faster now!
                    uil.setText(uil.getText() + "\n" + argbuilder.toString());
                } else {
                    helpPage.contents.add(workingElement = new UIHelpSystem.HelpElement(ilg, ch, argbuilder.toString()));
                }
            }
        });
        return hf;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.help;

import gabien.ui.elements.UILabel;
import r48.cfg.Config;

import java.util.function.Consumer;

/**
 * One of those mind-controlling classes that oversees everything!
 * Created on 04/06/17.
 */
public class HelpSystemController implements Consumer<String> {
    private UILabel pageName;
    private String helpFile;
    private HelpFile helpFileData;
    private UIHelpSystem hs;
    public Runnable onLoad;
    public final Config c;

    public HelpSystemController(UILabel pName, String hFile, UIHelpSystem charge) {
        c = charge.c;
        pageName = pName;
        helpFile = hFile == null ? "Help/Main/Entry" : hFile;
        helpFileData = HelpFile.load(charge.ilg, helpFile);
        hs = charge;
    }

    public void accept(final String link) {
        if (link.contains(":")) {
            String[] coms = link.split(":");
            helpFile = coms[0];
            helpFileData = HelpFile.load(hs.ilg, helpFile);
            loadPage(Integer.parseInt(coms[1]));
        } else {
            loadPage(Integer.parseInt(link));
        }
    }

    public void loadPage(final int i) {
        hs.page.clear();
        HelpFile.Page page = helpFileData.pages.get(i);
        if (page == null) {
            // uhhhhhhhhhh
            if (pageName != null)
                pageName.setText(helpFile + ":" + i);
        } else {
            if (pageName != null)
                pageName.setText(page.name);
            hs.page.addAll(page.contents);
        }
        hs.tightlyCoupledLayoutRecalculateMetrics();
        if (onLoad != null)
            onLoad.run();
    }

}

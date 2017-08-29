/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
        DBLoader.readFile(getHelpStream(), new IDatabase() {
            boolean working = false;

            @Override
            public void newObj(int objId, String objName) throws IOException {
                if (objId == i) {
                    if (pageName != null)
                        pageName.Text = objName;
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
        hs.setBounds(hs.getBounds());
        if (onLoad != null)
            onLoad.run();
    }

    private InputStream getHelpStream() {
        if (helpFile == null) {
            // Local language?
            InputStream inp = GaBIEn.getFile("Help" + TXDB.getLanguage() + ".txt");
            if (inp == null)
                return GaBIEn.getResource("Help.txt");
            return inp;
        }
        InputStream inp = GaBIEn.getFile(helpFile + TXDB.getLanguage() + ".txt");
        if (inp == null)
            return GaBIEn.getResource(helpFile + ".txt");
        return inp;
    }

}

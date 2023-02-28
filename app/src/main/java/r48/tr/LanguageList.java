/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tr;

import java.io.IOException;
import java.util.LinkedList;

import r48.dbs.DBLoader;
import r48.dbs.IDatabase;

/**
 * Intended to pull even more functionality out of TXDB.
 * Specifically, this provides the immutable parts of the static bits of TXDB.
 * Created 27th February 2023.
 */
public class LanguageList {
    private static final String[] languages = createLanguageList();

    private static String[] createLanguageList() {
        final LinkedList<String> languageLL = new LinkedList<String>();
        DBLoader.readFile(null, "Translations.txt", new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 'l')
                    languageLL.add(args[0]);
            }
        });
        return languageLL.toArray(new String[0]);
    }

    public static String getNextLanguage(String language) {
        boolean nextIsNext = false;
        for (int i = 0; i < languages.length; i++) {
            if (nextIsNext)
                return languages[i];
            if (languages[i].equals(language))
                nextIsNext = true;
        }
        return languages[0];
    }

    public static boolean hasLanguage(String language) {
        for (int i = 0; i < languages.length; i++)
            if (languages[i].equals(language))
                return true;
        return false;
    }
}

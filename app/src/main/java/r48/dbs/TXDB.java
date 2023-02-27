/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.GaBIEn;
import r48.tr.ITranslator;
import r48.tr.NullTranslator;
import r48.tr.Translator;

import java.io.IOException;
import java.util.*;

/**
 * Text Database. This is NOT a per-system database, and is static for a reason. This covers R48 Javaside strings.
 * Idea is to move every single user visible string NOT controlled by datafiles to going through this.
 * Cnames/etc, can be dealt with by their respective databases.
 * Created on 10/06/17.
 */
public class TXDB {
    private static ITranslator currentTranslator = new NullTranslator();
    private static String[] languages = new String[] {"English"};
    private static int languageId = 0;

    public static void init() {
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
        languages = languageLL.toArray(new String[0]);
    }

    // NOTE: Translation items of the form get("Blahblah") (note: comments are scanned too) cannot include backslash escapes.

    // Parameters to this function should be constant.
    public static String get(String english) {
        return currentTranslator.tr("r48", english);
    }

    // This function must be called *consistently* for a given schema.
    public static String get(String context, String english) {
        return currentTranslator.tr(context.replace('/', '-'), english);
    }

    public static String getExUnderscore(String context, String english) {
        if (english.equals("_"))
            return "_";
        return TXDB.get(context, english);
    }

    public static String stripContext(String s) {
        return s.substring(s.indexOf('/') + 1);
    }

    public static String getNextLanguage() {
        int nli = languageId + 1;
        nli %= languages.length;
        return languages[nli];
    }
    public static void setLanguage(String s) {
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(s)) {
                languageId = i;
                setLanguage();
                break;
            }
        }
    }
    public static void setLanguage() {
        if (languageId == 0) {
            currentTranslator = new NullTranslator();
        } else {
            currentTranslator = new Translator(languages[languageId]);
        }
        currentTranslator.read("Systerms/" + languages[languageId] + ".txt", "r48/");
        currentTranslator.read("Systerms/L-" + languages[languageId] + ".txt", "launcher/");
        GaBIEn.wordLoad = TXDB.get("Load");
        GaBIEn.wordSave = TXDB.get("Save");
        GaBIEn.wordInvalidFileName = TXDB.get("Invalid or missing file name.");
    }

    public static void loadGamepakLanguage(String gp) {
        // think: R2k/LangTest.txt
        setLanguage();
        try {
            currentTranslator.read(gp + "Lang" + languages[languageId] + ".txt", "SDB@");
        } catch (Exception e) {
        }
        try {
            currentTranslator.read(gp + "Cmtx" + languages[languageId] + ".txt", "CMDB@");
        } catch (Exception e) {
        }
    }

    public static String getLanguage() {
        return languages[languageId];
    }

    public static void performDump(String fnPrefix, String ctxPrefix) {
        currentTranslator.dump(fnPrefix, ctxPrefix);
    }
}

/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Text Database. This is NOT a per-system database, and is static for a reason. This covers R48 Javaside strings.
 * Idea is to move every single user visible string NOT controlled by datafiles to going through this.
 * Cnames/etc, can be dealt with by their respective databases.
 * Created on 10/06/17.
 */
public class TXDB {
    public static HashSet<String> ssTexts = new HashSet<String>();
    private static String[] languages = new String[] {"English"};
    private static int languageId = 0;
    private static HashMap<String, String> subspace = new HashMap<String, String>();

    public static void init() {
        final LinkedList<String> languageLL = new LinkedList<String>();
        DBLoader.readFile("Translations.txt", new IDatabase() {
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
        return get("r48/" + english, true);
    }

    // This function must be called *consistently* for a given schema.
    public static String get(String context, String english) {
        return get(context.replace('/', '-') + "/" + english, false);
    }

    private static String get(String english, boolean constant) {
        if (languageId == 0)
            return english;
        String replace = subspace.get(english);
        if (!constant)
            ssTexts.add(english);
        if (replace == null)
            return ":NT:" + english;
        return replace;
    }

    public static void nextLanguage() {
        languageId++;
        languageId %= languages.length;
        subspace.clear();
        ssTexts.clear();
        if (languageId == 0)
            return;
        DBLoader.readFile("Systerms/" + languages[languageId] + ".txt", new LangLoadDatabase("r48/"));
    }

    public static void loadGamepakLanguage(String gp) {
        // think: R2k/LangTest.txt
        subspace.clear();
        ssTexts.clear();
        if (languageId == 0)
            return;
        DBLoader.readFile("Systerms/" + languages[languageId] + ".txt", new LangLoadDatabase("r48/"));
        try {
            DBLoader.readFile(gp + "Lang" + languages[languageId] + ".txt", new LangLoadDatabase(""));
        } catch (Exception e) {
            // ignore any exceptions from the specific gamepak language
        }
    }

    public static String getLanguage() {
        return languages[languageId];
    }

    private static class LangLoadDatabase implements IDatabase {
        String target = "";

        String impliedPrefix;

        public LangLoadDatabase(String s) {
            impliedPrefix = s;
        }

        @Override
        public void newObj(int objId, String objName) throws IOException {

        }

        @Override
        public void execCmd(char c, String[] args) throws IOException {
            if (c == 'x')
                target = mergeArgs(args);
            if (c == 'y')
                subspace.put(impliedPrefix + target, mergeArgs(args));
        }

        private String mergeArgs(String[] args) throws IOException {
            String s = "";
            for (String a : args)
                s += " " + a;
            s = s.trim();
            if ((!s.startsWith("\"")) || (!s.endsWith("\"")))
                throw new IOException("The translation table string " + s + " does not have standard quotation (used to allow prefix/postfix spaces).");
            return s.substring(1, s.length() - 1);
        }
    }
}

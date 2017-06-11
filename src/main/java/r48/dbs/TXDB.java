/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Text Database. This is NOT a per-system database, and is static for a reason. This covers R48 Javaside strings.
 * Idea is to move every single user visible string NOT controlled by datafiles to going through this.
 * Cnames/etc, can be dealt with by their respective databases.
 * Created on 10/06/17.
 */
public class TXDB {
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

    // NOTE: Translation items cannot include backslash escapes.
    public static String get(String english) {
        if (languageId == 0)
            return english;
        String replace = subspace.get(english);
        if (replace == null)
            return ":NT:" + english;
        return replace;
    }

    public static void nextLanguage() {
        languageId++;
        languageId %= languages.length;
        subspace.clear();
        if (languageId == 0)
            return;
        DBLoader.readFile("Systerms/" + languages[languageId] + ".txt", new IDatabase() {
            String target = "";
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 'x')
                    target = mergeArgs(args);
                if (c == 'y')
                    subspace.put(target, mergeArgs(args));
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
        });
    }

    public static String getLanguage() {
        return languages[languageId];
    }
}

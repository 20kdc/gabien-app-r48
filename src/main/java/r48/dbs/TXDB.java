/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.GaBIEn;
import gabien.ui.IFunction;
import r48.RubyIO;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
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

    // This gets stuff inserted into it by AppMain (via sdb & co), so it has to be flushed in shutdown() there.
    public static HashMap<String, IFunction<RubyIO, String>> nameDB = new HashMap<String, IFunction<RubyIO, String>>();

    public static void flushNameDB() {
        nameDB.clear();
        // Usage: {@[lang-Russian-pluralRange][#A #B]|0|plural-form-0|1|plural-form-1|2|plural-form-2}
        // Explicitly for Set Variables use and similar.
        // Yes, if you request it, I'll make a similar TXDB routine for you,
        //  assuming it's not ridiculously complicated.
        nameDB.put("Interp.lang-Russian-pluralRange", new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                String[] range = rubyIO.decString().split(" ");
                int v = Integer.valueOf(range[1]);
                v -= Integer.valueOf(range[0]) - 1;
                int i = v % 10;
                if ((i == 1) && (v != 11))
                    return "0";
                if ((i >= 2) && (i <= 4) && ((v / 10) != 1))
                    return "1";
                return "2";
            }
        });
        nameDB.put("Interp.lang-Common-arrayLen", new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                return Integer.toString(rubyIO.arrVal.length);
            }
        });
    }

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
        flushNameDB();
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

    public static String getExUnderscore(String context, String english) {
        if (english.equals("_"))
            return "_";
        return TXDB.get(context, english);
    }

    private static String get(String english, boolean constant) {
        if (languageId == 0)
            return stripContext(english);
        String replace = subspace.get(english);
        if (!constant)
            ssTexts.add(english);
        if (replace == null)
            return ":NT:" + english;
        return replace;
    }

    // Checks if subspace contains a key. Assumes context-syntax is already done.
    public static boolean has(String s) {
        return subspace.get(s) != null;
    }

    public static String stripContext(String s) {
        return s.substring(s.indexOf('/') + 1);
    }

    public static void nextLanguage() {
        languageId++;
        languageId %= languages.length;
        setLanguage();
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
        subspace.clear();
        ssTexts.clear();
        if (languageId == 0)
            return;
        DBLoader.readFile("Systerms/" + languages[languageId] + ".txt", new LangLoadDatabase("r48/"));
        DBLoader.readFile("Systerms/L-" + languages[languageId] + ".txt", new LangLoadDatabase("launcher/"));
    }

    public static void loadGamepakLanguage(String gp) {
        // think: R2k/LangTest.txt
        subspace.clear();
        ssTexts.clear();
        if (languageId == 0)
            return;
        DBLoader.readFile("Systerms/" + languages[languageId] + ".txt", new LangLoadDatabase("r48/"));
        DBLoader.readFile("Systerms/L-" + languages[languageId] + ".txt", new LangLoadDatabase("launcher/"));
        try {
            DBLoader.readFile(gp + "Lang" + languages[languageId] + ".txt", new LangLoadDatabase("SDB@"));
        } catch (Exception e) {
        }
        try {
            DBLoader.readFile(gp + "Cmtx" + languages[languageId] + ".txt", new LangLoadDatabase("CMDB@"));
        } catch (Exception e) {
        }
    }

    public static String getLanguage() {
        return languages[languageId];
    }

    public static void performDump(String group, String prefix) {
        PrintStream psA = null;
        try {
            psA = new PrintStream(GaBIEn.getOutFile(group + TXDB.getLanguage() + ".txt"), false, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        LinkedList<String> t = new LinkedList<String>(TXDB.ssTexts);
        Collections.sort(t);
        for (String s : t) {
            String key = TXDB.stripContext(s);
            String ctx = s.substring(0, s.length() - (key.length() + 1));
            if (s.startsWith(prefix)) {
                psA.println("x \"" + s.substring(prefix.length()) + "\"");
                if (TXDB.has(s)) {
                    psA.println("y \"" + TXDB.get(ctx, key) + "\"");
                } else {
                    psA.println(" TODO");
                    psA.println("y \"" + key + "\"");
                }
            }
        }
        psA.close();
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

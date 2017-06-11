/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

/**
 * Text Database. This is NOT a per-system database, and is static for a reason. This covers R48 Javaside strings.
 * Idea is to move every single user visible string NOT controlled by datafiles to going through this.
 * Cnames/etc, can be dealt with by their respective databases.
 * Created on 10/06/17.
 */
public class TXDB {
    public static boolean langTestMode = false;
    public static String get(String english) {
        if (langTestMode)
            return "Eggnog.";
        return english;
    }

    public static void nextLanguage() {
        langTestMode = !langTestMode;
    }

    public static String getLanguage() {
        return langTestMode ? "Test" : "English";
    }
}

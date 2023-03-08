/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.tr.ITranslator;
import r48.tr.NullTranslator;

/**
 * Text Database. This is NOT a per-system database, and is static for a reason. This covers R48 Javaside strings.
 * Idea is to move every single user visible string NOT controlled by datafiles to going through this.
 * Cnames/etc, can be dealt with by their respective databases.
 * Created on 10/06/17.
 */
public class TXDB {
    private static ITranslator currentTranslator = new NullTranslator();

    public static void inject(ITranslator dynTr) {
        currentTranslator = dynTr;
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

    public static void performDump(String fnPrefix, String ctxPrefix) {
        currentTranslator.dump(fnPrefix, ctxPrefix);
    }
}

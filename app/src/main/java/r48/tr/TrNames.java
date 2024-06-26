/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

/**
 * Names for translation slots.
 * This doesn't include static translations.
 * Created 13th March 2023.
 */
public class TrNames {

    /**
     * Dynamic launcher string.
     */
    public static String dynLn(String string) {
        return "TrDynLauncher." + string;
    }

    public static String cmdbCat(String dbId, int cat) {
        return "TrCMDB_" + dbId + "_cat.c" + cat;
    }

    public static String cmdbName(String dbId, int commandId) {
        return "TrCMDB_" + dbId + "_c" + commandId + ".n";
    }

    public static String cmdbDesc(String dbId, int commandId) {
        return "TrCMDB_" + dbId + "_c" + commandId + ".d";
    }

    // has "." and more appended for disambiguation
    public static String cmdbParam(String dbId, int commandId, int paramIdx) {
        return "TrCMDB_" + dbId + "_c" + commandId + ".p" + paramIdx;
    }

    public static String cmdbCommandTag(String id) {
        return "TrCMDBTag." + id;
    }

    // pretty much just whatever could be made up
    public static String sdbAnon(String ovc, String text) {
        return "TrSDB_" + ovc + ".$" + text;
    }

    public static String sdbEnum(String string, String k) {
        return "TrSDB_" + string + ".e" + k;
    }

    public static String sdbWindowTitle(String ovc) {
        return "TrSDB_" + ovc + ".title";
    }

    public static String sdbLabel(String ovc, String topic) {
        return "TrSDB_" + ovc + ".label." + topic;
    }

    public static String nameRoutine(String name) {
        return "TrName." + name;
    }

    public static String sdbSpritesheet(String imgPfx) {
        return "TrSpritesheet." + imgPfx;
    }
}

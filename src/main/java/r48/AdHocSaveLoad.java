/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48;

import r48.io.R48ObjectBackend;

import java.io.IOException;

/**
 * Similar to the Art and ArrayUtils classes, this is a class with all static methods just for holding purposes.
 * The reason for this in particular is that it must work outside of a R48 application context (Read as : anything static in AppMain)
 * Notably, the R48ObjectBackend is used because it is meant to be capable of serializing *any valid object* that might stumble into R48,
 *  since R48 values are based on an *unextended* variant of Ruby Marshal 4.8 objects
 *  (possibly with altering to make them more DAG-like).
 * And also note this whole thing implies object backends are "outside" of application context (which is true).
 *
 * Created on 12/08/17.
 */
public class AdHocSaveLoad {
    public static void save(String fonts, RubyIO prepare) {
        R48ObjectBackend rob = new R48ObjectBackend("", ".r48", false);
        try {
            rob.saveObjectToFile(fonts, prepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void saveLua(String fonts, RubyIO prepare) {
        R48ObjectBackend rob = new R48ObjectBackend("", ".r48", true, true);
        try {
            rob.saveObjectToFile(fonts, prepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static RubyIO load(String fonts) {
        R48ObjectBackend rob = new R48ObjectBackend("", ".r48", false);
        return rob.loadObjectFromFile(fonts);
    }
}

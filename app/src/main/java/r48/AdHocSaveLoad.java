/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.GaBIEn;
import gabienapp.Application;
import r48.io.JsonObjectBackend;
import r48.io.R48ObjectBackend;
import r48.io.data.DMContext;
import r48.io.data.DMChangeTracker;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Similar to the Art and ArrayUtils classes, this is a class with all static methods just for holding purposes.
 * The reason for this in particular is that it must work outside of a R48 application context (Read as : anything static in AppMain)
 * Notably, the R48ObjectBackend is used because it is meant to be capable of serializing *any valid object* that might stumble into R48,
 * since R48 values are based on an *unextended* variant of Ruby Marshal 4.8 objects
 * (possibly with altering to make them more DAG-like).
 * And also note this whole thing implies object backends are "outside" of application context (which is true).
 * <p>
 * Created on 12/08/17.
 */
public class AdHocSaveLoad {
    public static String PREFIX = Application.BRAND + "/";
    public static void save(String fonts, RORIO prepare) {
        prepare();
        // workaround because R48ObjectBackend still hasn't undergone some sort of reform
        R48ObjectBackend rob = new R48ObjectBackend(GaBIEn.mutableDataFS, PREFIX, ".r48");
        try {
            rob.saveObjectToFile(fonts, prepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable IRIOGeneric load(String fonts) {
        R48ObjectBackend rob = new R48ObjectBackend(GaBIEn.mutableDataFS, PREFIX, ".r48");
        return rob.loadObjectFromFile(fonts, newContext());
    }

    public static void saveJSON(String fonts, RORIO prepare) {
        prepare();
        JsonObjectBackend rob = new JsonObjectBackend(GaBIEn.mutableDataFS, PREFIX, ".json");
        try {
            rob.saveObjectToFile(fonts, prepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void prepare() {
        GaBIEn.makeDirectories(PREFIX);
    }

    public static DMContext newContext() {
        return new DMContext(DMChangeTracker.Null.ADHOC_IO, StandardCharsets.UTF_8);
    }
}

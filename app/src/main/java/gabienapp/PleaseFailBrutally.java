/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

/**
 * Debugs error handling mechanisms.
 * Created 13th May 2024.
 */
public class PleaseFailBrutally {
    private static void checkFailBrutally(String locale) {
        String checkme = System.getenv("R48_DEBUG_PLEASE_FAIL_BRUTALLY");
        if (checkme != null)
            if (checkme.equals(locale))
                throw new RuntimeException("told to fail at " + locale);
    }

    public static void checkFailBrutallyAtSplash() {
        checkFailBrutally("splash");
    }

    public static void checkFailBrutallyAtLoader() {
        checkFailBrutally("loader");
    }

    public static void checkFailBrutallyAtAppInit() {
        checkFailBrutally("appinit");
    }
}

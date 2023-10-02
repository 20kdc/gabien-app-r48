/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

/**
 * Created on 1/27/17.
 */
public class Application {
    // used for directory name so R48 stops polluting any workspace it's used in.
    public static final String BRAND = "r48";
    public static final String BRAND_C = "R48";
    public static final String[] appPrefixes = new String[] {BRAND + "/"};

    public static void gabienmain() {
        Launcher lun = new Launcher();
        lun.run();
    }
}

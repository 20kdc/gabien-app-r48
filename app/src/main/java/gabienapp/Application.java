/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.uslx.licensing.LicenseComponent;
import gabien.uslx.licensing.LicenseManager;

/**
 * Created on 1/27/17.
 */
public class Application {
    // used for directory name so R48 stops polluting any workspace it's used in.
    public static final String BRAND = "r48";
    public static final String BRAND_C = "R48";
    public static final String[] appPrefixes = new String[] {Application.BRAND + "/"};

    public static final LicenseComponent LC_R48 = new LicenseComponent("R48", "https://github.com/20kdc/gabien-app-r48/", "COPYING.txt", "CREDITS.txt");

    static {
        LicenseManager.I.register(LC_R48);
        LicenseManager.I.dependency(LC_R48, LicenseComponent.LC_GABIEN);
    }

    public static void gabienmain() {
        if (!GaBIEn.hasStoragePermission())
            Android23.run();
        Launcher lun = new Launcher(false);
        lun.run();
    }
}

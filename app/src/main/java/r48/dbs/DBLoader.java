/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.app.AppCore;
import r48.ioplus.DatumLoader;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Handles the basic database syntax.
 * Created on 12/30/16.
 */
public class DBLoader {

    public static void readFile(@Nullable AppCore app, @NonNull String s, @NonNull IDatabase db) {
        DatumLoader.readEssential(s, app != null ? app.loadProgress : null, db);
    }

    public static void readFile(String fn, IDatabase db) {
        readFile(null, fn, db);
    }
}

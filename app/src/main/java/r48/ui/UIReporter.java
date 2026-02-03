/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.R48;
import r48.ioplus.Reporter;

/**
 * Created 2nd February (barely)
 */
public class UIReporter extends Reporter implements AutoCloseable {
    private final @Nullable AppUI U;
    public boolean nothingToReport = true;

    public UIReporter(@NonNull R48 app) {
        super(app.t);
        U = null;
    }

    public UIReporter(@NonNull AppUI aui) {
        super(aui.T);
        U = aui;
    }

    public UIReporter(@NonNull R48 app, @Nullable AppUI aui) {
        super(app.t);
        U = aui;
    }

    @Override
    public void report(String msg) {
        if (U != null)
            U.launchDialog(msg);
        nothingToReport = false;
    }

    @Override
    public void report(String msg, Throwable err) {
        if (U != null)
            U.launchDialog(msg, err);
        nothingToReport = false;
    }

    @Override
    public void report(Throwable err) {
        if (U != null)
            U.launchDialog(err);
        nothingToReport = false;
    }

    @Override
    public void close() {
    }
}

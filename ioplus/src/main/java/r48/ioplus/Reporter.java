/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ioplus;

import r48.tr.pages.TrRoot;

/**
 * Reports errors. Also contains a TrRoot for error messages.
 * Created February 1st, 2026.
 */
public abstract class Reporter {
    public final TrRoot t;
    public Reporter(TrRoot t) {
        this.t = t;
    }

    public abstract void report(String msg);
    public abstract void report(String msg, Throwable err);
    public abstract void report(Throwable err);

    public static class Dummy extends Reporter {
        public Dummy(TrRoot t) {
            super(t);
        }
        @Override
        public void report(String msg) {
        }
        @Override
        public void report(String msg, Throwable err) {
        }
        @Override
        public void report(Throwable err) {
        }
    }
}

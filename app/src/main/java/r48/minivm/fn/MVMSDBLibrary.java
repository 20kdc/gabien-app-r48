/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import gabien.datum.DatumSymbol;
import r48.App;
import r48.dbs.SDBOldParser;
import r48.minivm.MVMEnv;

/**
 * MiniVM standard library.
 * Created 10th March 2023.
 */
public class MVMSDBLibrary {
    public static void add(MVMEnv ctx, App app) {
        ctx.defineSlot(new DatumSymbol("sdb-load-old")).v = new SDBLoadOld(app)
                .attachHelp("(sdb-load-old FILE) : Read old-format SDB file.");
    }
    public static final class SDBLoadOld extends MVMFn.Fixed {
        public final App app;
        public SDBLoadOld(App a) {
            super("sdb-load-old");
            app = a;
        }
        @Override
        public Object callDirect(Object a0) {
            SDBOldParser.readFile(app, (String) a0);
            return null;
        }
    }
}

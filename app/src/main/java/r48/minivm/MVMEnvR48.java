/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import gabien.uslx.append.IConsumer;
import r48.dbs.DatumLoader;

/**
 * MiniVM environment.
 * Created 26th February 2023 but only fleshed out 28th.
 * Include/loadProgress split from MiniVM core 1st March 2023.
 */
public final class MVMEnvR48 extends MVMEnv {
    private final IConsumer<String> loadProgress;

    public MVMEnvR48(IConsumer<String> loadProgress) {
        super();
        this.loadProgress = loadProgress;
    }

    protected MVMEnvR48(MVMEnvR48 p) {
        super(p);
        loadProgress = p.loadProgress;
    }

    /**
     * Loads the given file into this context.
     */
    public void include(String filename, boolean opt) {
        IConsumer<Object> eval = (obj) -> {
            evalObject(obj);
        }; 
        boolean attempt = DatumLoader.read(filename + ".scm", loadProgress, eval);
        if (!attempt)
            attempt = DatumLoader.read(filename + ".txt", loadProgress, eval);
        if ((!opt) && !attempt)
            throw new RuntimeException("Expected " + filename + "(.scm|.txt) to exist");
        // don't care if this doesn't exist
        DatumLoader.read(filename + ".aux.scm", loadProgress, (obj) -> {
            evalObject(obj);
        });
    }
}

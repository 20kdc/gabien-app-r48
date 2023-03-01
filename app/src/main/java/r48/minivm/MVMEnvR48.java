/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.io.InputStreamReader;

import gabien.GaBIEn;
import gabien.datum.DatumDecodingVisitor;
import gabien.datum.DatumReaderTokenSource;
import gabien.uslx.append.IConsumer;

/**
 * MiniVM environment.
 * Created 26th February 2023 but only fleshed out 28th.
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
    public void include(String filename) {
        System.out.println(">>" + filename);
        if (loadProgress != null)
            loadProgress.accept(filename);
        try {
            InputStreamReader ins = GaBIEn.getTextResource(filename);
            DatumDecodingVisitor ddv = new DatumDecodingVisitor() {
                @Override
                public void visitTree(Object obj) {
                    evalObject(obj);
                }
                @Override
                public void visitEnd() {
                }
            };
            DatumReaderTokenSource drts = new DatumReaderTokenSource(ins);
            drts.visit(ddv);
        } catch (Exception ex) {
            throw new RuntimeException("During MVM read-in @ " + filename, ex);
        }
        System.out.println("<<" + filename);
    }
}

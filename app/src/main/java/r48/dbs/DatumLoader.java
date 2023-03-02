/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.dbs;

import java.io.InputStreamReader;

import gabien.GaBIEn;
import gabien.datum.DatumDecodingVisitor;
import gabien.datum.DatumReaderTokenSource;
import gabien.uslx.append.IConsumer;

/**
 * DBLoader, but not DBLoader!
 * Created 1st March 2023.
 */
public class DatumLoader {
    /**
     * Loads the given file into this context.
     */
    public static boolean read(String filename, IConsumer<String> loadProgress, IConsumer<Object> eval) {
        try (InputStreamReader ins = GaBIEn.getTextResource(filename)) {
            if (ins == null) {
                System.out.println("X " + filename);
                return false;
            }
            System.out.println(">>" + filename);
            if (loadProgress != null)
                loadProgress.accept(filename);
            DatumDecodingVisitor ddv = new DatumDecodingVisitor() {
                @Override
                public void visitTree(Object obj) {
                    eval.accept(obj);
                }
                @Override
                public void visitEnd() {
                }
            };
            DatumReaderTokenSource drts = new DatumReaderTokenSource(ins);
            drts.visit(ddv);
        } catch (Exception ex) {
            throw new RuntimeException("During read-in @ " + filename, ex);
        }
        System.out.println("<<" + filename);
        return true;
    }
}

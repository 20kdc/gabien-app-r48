/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.dbs;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import static datum.DatumTreeUtils.*;

import datum.DatumReaderTokenSource;
import datum.DatumSrcLoc;
import datum.DatumTreeUtils;
import datum.DatumVisitor;
import gabien.GaBIEn;

/**
 * DBLoader, but not DBLoader!
 * Created 1st March 2023.
 */
public class DatumLoader {
    // DatumLoader and DBLoader are so critical that I couldn't put this behind a config option even if I wanted to.
    // But this is very, very spammy.
    public static final boolean reportLoadSE = false;

    /**
     * Loads an inline value.
     */
    public static Object readInline(DatumSrcLoc base, String text) {
        LinkedList<Object> result = readInlineList(base, text);
        if (result.size() == 0)
            throw new RuntimeException("No object at inline Datum at " + base);
        if (result.size() > 1)
            throw new RuntimeException("Too many objects at inline Datum at " + base);
        return result.get(0);
    }

    /**
     * Loads an inline value.
     */
    public static LinkedList<Object> readInlineList(DatumSrcLoc base, String text) {
        LinkedList<Object> result = new LinkedList<>();
        new DatumReaderTokenSource(base.toString(), text).visit(decVisitor((obj, srcLoc) -> {
            result.add(obj);
        }));
        return result;
    }

    /**
     * Loads an essential Datum file.
     */
    public static void readEssential(String filename, @Nullable Consumer<String> loadProgress, DatumTreeUtils.VisitorLambda eval) {
        if (!read(filename, loadProgress, eval))
            throw new RuntimeException("Expected " + filename + " but it did not exist!");
    }

    /**
     * Loads an essential Datum file.
     */
    public static void readEssential(String filename, @Nullable Consumer<String> loadProgress, DatumVisitor ddv) {
        if (!read(filename, loadProgress, ddv))
            throw new RuntimeException("Expected " + filename + " but it did not exist!");
    }

    /**
     * Loads an optional Datum file.
     * Returns true on success.
     */
    public static boolean read(String filename, @Nullable Consumer<String> loadProgress, DatumTreeUtils.VisitorLambda eval) {
        return read(filename, loadProgress, decVisitor(eval));
    }

    /**
     * Loads an optional Datum file.
     * Returns true on success.
     */
    public static boolean read(String filename, @Nullable Consumer<String> loadProgress, DatumVisitor ddv) {
        filename += ".scm";
        try (InputStreamReader ins = GaBIEn.getTextResource(filename)) {
            if (ins == null) {
                if (reportLoadSE)
                    System.out.println("X " + filename);
                return false;
            }
            if (reportLoadSE)
                System.out.println(">>" + filename);
            if (loadProgress != null)
                loadProgress.accept(filename);
            DatumReaderTokenSource drts = new DatumReaderTokenSource(filename, ins);
            drts.visit(ddv);
        } catch (Exception ex) {
            throw new RuntimeException("During read-in @ " + filename, ex);
        }
        if (reportLoadSE)
            System.out.println("<<" + filename);
        return true;
    }
}

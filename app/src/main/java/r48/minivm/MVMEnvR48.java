/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import gabien.uslx.append.IConsumer;
import r48.dbs.DatumLoader;
import r48.tr.DynTrSlot;
import r48.tr.IDynTrProxy;

/**
 * MiniVM environment.
 * Created 26th February 2023 but only fleshed out 28th.
 * Include/loadProgress split from MiniVM core 1st March 2023.
 */
public final class MVMEnvR48 extends MVMEnv implements IDynTrProxy {
    private final IConsumer<String> loadProgress, logTrIssues;
    private final HashMap<String, DynTrSlot> dynMap;
    private final LinkedList<String> dynList;

    public MVMEnvR48(IConsumer<String> loadProgress, IConsumer<String> logTrIssues) {
        super();
        this.loadProgress = loadProgress;
        this.logTrIssues = logTrIssues;
        dynMap = new HashMap<>();
        dynList = new LinkedList<>();
    }

    protected MVMEnvR48(MVMEnvR48 p) {
        super(p);
        loadProgress = p.loadProgress;
        logTrIssues = p.logTrIssues;
        dynMap = p.dynMap;
        dynList = p.dynList;
    }

    /**
     * Loads the given file into this context.
     */
    public void include(String filename, boolean opt) {
        DatumLoader.Handler eval = this::evalObject; 
        boolean attempt = DatumLoader.read(filename + ".scm", loadProgress, eval);
        if (!attempt)
            attempt = DatumLoader.read(filename + ".txt", loadProgress, eval);
        if ((!opt) && !attempt)
            throw new RuntimeException("Expected " + filename + "(.scm|.txt) to exist");
        // don't care if this doesn't exist
        DatumLoader.read(filename + ".aux.scm", loadProgress, eval);
    }

    /**
     * Dynamic translation slot.
     */
    @Override
    public DynTrSlot dynTrBase(DatumSrcLoc srcLoc, String id, Object base) {
        DynTrSlot res = dynMap.get(id);
        if (res == null) {
            MVMSlot slot = ensureSlot(new DatumSymbol(id));
            res = new DynTrSlot(srcLoc, slot);
            dynMap.put(id, res);
            dynList.add(id);
        }
        Object intoMVM = DynTrSlot.translateIntoMVM(id, base);
        if (res.underlyingSlot.v != null)
            if (!res.underlyingSlot.v.equals(intoMVM))
                logTrIssues.accept("dynTr ID " + id + " changed value between two dynTrBase calls. DON'T do this.");
        res.underlyingSlot.v = intoMVM;
        return res;
    }

    private LinkedList<TrDumpSection> dynTrDumpSections() {
        LinkedList<TrDumpSection> sections = new LinkedList<>();
        TrDumpSection currentSection = null;
        for (String s : dynList) {
            String estPfx = "";
            if (s.contains("."))
                estPfx = s.substring(0, s.indexOf('.') + 1);
            if (currentSection == null || !currentSection.prefix.equals(estPfx)) {
                currentSection = new TrDumpSection(estPfx);
                sections.add(currentSection);
            }
            currentSection.sectionContent.add(s);
        }
        return sections;
    }

    public void dynTrDump(String fn) {
        try (OutputStream os = GaBIEn.getOutFile(fn)) {
            for (TrDumpSection section : dynTrDumpSections()) {
                if (section.prefix.equals("")) {
                    os.write("(define \n".getBytes(StandardCharsets.UTF_8));
                } else if (section.prefix.endsWith(".")) {
                    DatumSymbol ds = new DatumSymbol(section.prefix.substring(0, section.prefix.length() - 1));
                    os.write(("(define-group " + DatumWriter.objectToString(ds) + " \n").getBytes(StandardCharsets.UTF_8));
                } else {
                    DatumSymbol ds = new DatumSymbol(section.prefix);
                    os.write(("(define-prefix " + DatumWriter.objectToString(ds) + " \n").getBytes(StandardCharsets.UTF_8));
                }
                for (String s : section.sectionContent) {
                    DynTrSlot slot = dynMap.get(s);
                    DatumSymbol symK = new DatumSymbol(s.substring(section.prefix.length()));
                    String v = slot.sourceDump();
                    String ln = "\t; " + slot.sourceLoc  + "\n\t" + DatumWriter.objectToString(symK) + " " + v + "\n";
                    os.write(ln.getBytes(StandardCharsets.UTF_8));
                }
                os.write(")\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class TrDumpSection {
        String prefix;
        LinkedList<String> sectionContent = new LinkedList<>();
        TrDumpSection(String hdr) {
            prefix = hdr;
        }
    }
}

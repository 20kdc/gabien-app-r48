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
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import datum.DatumDecToLambdaVisitor;
import datum.DatumSrcLoc;
import datum.DatumSymbol;
import datum.DatumWriter;
import gabien.GaBIEn;
import gabienapp.Application;
import r48.app.Coco;
import r48.dbs.DatumLoader;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.tr.DynTrBase;
import r48.tr.DynTrSlot;
import r48.tr.IDynTrProxy;
import r48.tr.NLSTr;

/**
 * MiniVM environment.
 * Created 26th February 2023 but only fleshed out 28th.
 * Include/loadProgress split from MiniVM core 1st March 2023.
 */
public final class MVMEnvR48 extends MVMEnv implements IDynTrProxy {
    public static final MVMType IRIO_TYPE = MVMType.typeOfClass(IRIO.class);
    public static final MVMType RORIO_TYPE = MVMType.typeOfClass(RORIO.class);

    private final Consumer<String> loadProgress, logTrIssues;
    private final HashMap<String, DynTrSlot> dynMap;
    private final LinkedList<String> dynList;
    private final String langID;
    public final boolean strict;

    public MVMEnvR48(Consumer<String> loadProgress, Consumer<String> logTrIssues, String lid, boolean strict) {
        super();
        defineType(new DatumSymbol("rorio"), RORIO_TYPE);
        defineType(new DatumSymbol("irio"), IRIO_TYPE);
        this.loadProgress = loadProgress;
        this.logTrIssues = logTrIssues;
        dynMap = new HashMap<>();
        dynList = new LinkedList<>();
        langID = lid;
        this.strict = strict;
    }

    /**
     * Determines the correct type to cast to for an accessor.
     */
    public static MVMType irioOrRORIOForAccessor(MVMType input, Object context) {
        if (input.canImplicitlyCastTo(IRIO_TYPE))
            return IRIO_TYPE;
        input.assertCanImplicitlyCastTo(RORIO_TYPE, context);
        return RORIO_TYPE;
    }

    /**
     * Loads the given file into this context.
     */
    public void include(String filename, boolean opt) {
        DatumDecToLambdaVisitor.Handler eval = this::evalObject; 
        boolean attempt = DatumLoader.read(filename, loadProgress, eval);
        if ((!opt) && !attempt)
            throw new RuntimeException("Expected " + filename + ".scm to exist");
        // don't care if this doesn't exist
        DatumLoader.read(filename + ".aux", loadProgress, eval);
    }

    /**
     * Dynamic translation slot.
     */
    @Override
    public DynTrBase dynTrBase(DatumSrcLoc srcLoc, String id, @Nullable DatumSymbol mode, Object base, boolean isNLS) {
        DynTrSlot res = dynMap.get(id);
        if (res == null) {
            MVMSlot slot = ensureSlot(new DatumSymbol(id));
            if (slot.v != null)
                throw new RuntimeException("DynTr can't overwrite unrelated (NLS?) value " + id + ".");
            if (isNLS) {
                NLSTr nls = new NLSTr(this, srcLoc, id, mode, base);
                slot.v = nls;
                return nls;
            }
            res = new DynTrSlot(this, srcLoc, id, mode, base);
            slot.v = res;
            dynMap.put(id, res);
            dynList.add(id);
        } else {
            if (isNLS)
                throw new RuntimeException("Can't overwrite " + id + " with an NLS slot.");
            String srcOld = res.sourceDump();
            res.setValue(base);
            if (!srcOld.equals(res.sourceDump()))
                logTrIssues.accept("dynTr ID " + id + " changed value between two dynTrBase calls. DON'T do this.");
        }
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
            StringBuilder sb = new StringBuilder();
            // we're not actually in even an ILG context, so we can't translate this
            new DatumWriter(sb).visitComment(Application.BRAND_C + " Version: " + Coco.getVersion() + ", Language: " + langID);
            for (TrDumpSection section : dynTrDumpSections()) {
                if (section.prefix.equals("")) {
                    sb.append("(tr-prefix #{}#\n");
                } else if (section.prefix.endsWith(".")) {
                    DatumSymbol ds = new DatumSymbol(section.prefix.substring(0, section.prefix.length() - 1));
                    sb.append("(tr-group " + DatumWriter.objectToString(ds) + "\n");
                } else {
                    DatumSymbol ds = new DatumSymbol(section.prefix);
                    sb.append("(tr-prefix " + DatumWriter.objectToString(ds) + "\n");
                }
                for (String s : section.sectionContent) {
                    DynTrSlot slot = dynMap.get(s);
                    DatumSymbol symK = new DatumSymbol(s.substring(section.prefix.length()));
                    String v = slot.sourceDump();
                    sb.append("\t; " + slot.sourceLoc  + ": " + slot.originalSrc + "\n");
                    sb.append("\t" + DatumWriter.objectToString(symK) + " " + v + "\n");
                }
                sb.append(")\n");
            }
            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
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

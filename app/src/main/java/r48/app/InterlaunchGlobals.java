/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.util.HashMap;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.uslx.append.IConsumer;
import r48.cfg.Config;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.fn.MVMR48GlobalLibraries;
import r48.tr.DynTrBase;
import r48.tr.IDynTrProxy;
import r48.tr.LanguageList;
import r48.tr.pages.TrRoot;

/**
 * Globals shared between launcher and app.
 * Notably, updateLanguage must be called at least once before this can really be called finished.
 * Created 28th February, 2023
 */
public class InterlaunchGlobals implements IDynTrProxy {
    public final Config c;
    public final TrRoot t = new TrRoot();
    private MVMEnvR48 langVM;
    private IConsumer<MVMEnv> reportVMChanges;
    private HashMap<String, EngineDef> engineDefs;
    public final IConsumer<String> logTrIssues;

    public InterlaunchGlobals(Config c, IConsumer<MVMEnv> report, IConsumer<String> loadProgress, IConsumer<String> trIssues) {
        this.c = c;
        logTrIssues = trIssues;
        reportVMChanges = report;
        updateLanguage(loadProgress);
        engineDefs = EnginesList.getEngines(loadProgress);
    }

    @Override
    public DynTrBase dynTrBase(DatumSrcLoc srcLoc, String id, @Nullable DatumSymbol mode, Object text, @Nullable Object addCtx, boolean isNLS) {
        return langVM.dynTrBase(srcLoc, id, mode, text, addCtx, isNLS);
    }

    public void launcherDynTrDump(String fn) {
        langVM.dynTrDump(fn);
    }

    /**
     * Gets an engine definition (or null if it doesn't exist, but do pretend it always does)
     */
    public EngineDef getEngineDef(String engineId) {
        return engineDefs.get(engineId);
    }

    /**
     * Sets the language of ILG to the given one.
     * Will reset language if not found.
     * If the app is running you're expected to figure that out yourself.
     */
    public void updateLanguage(IConsumer<String> loadProgress) {
        String lang = c.language;
        if (LanguageList.getLangInfo(lang) == null)
            lang = LanguageList.defaultLang;
        c.language = lang;
        // ---
        langVM = new MVMEnvR48(loadProgress, logTrIssues, lang);
        MVMR48GlobalLibraries.add(langVM);
        langVM.include("vm/global", false);
        // if the language author wants English fallback, they'll just (include "terms/eng/init")
        langVM.include("terms/" + lang + "/init", true);
        t.fillFromVM(langVM, logTrIssues);
        reportVMChanges.accept(langVM);
        GaBIEn.wordLoad = t.g.wordLoad;
        GaBIEn.wordSave = t.g.wordSave;
        GaBIEn.wordInvalidFileName = t.g.wordInvalidFileName;
    }
}

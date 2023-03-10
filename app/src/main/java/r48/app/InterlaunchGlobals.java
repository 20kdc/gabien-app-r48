/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import gabien.GaBIEn;
import gabien.uslx.append.IConsumer;
import r48.cfg.Config;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.fn.MVMR48GlobalLibraries;
import r48.tr.ITranslator;
import r48.tr.LanguageList;
import r48.tr.NullTranslator;
import r48.tr.Translator;
import r48.tr.pages.TrRoot;

/**
 * Globals shared between launcher and app.
 * Notably, updateLanguage must be called at least once before this can really be called finished.
 * Created 28th February, 2023
 */
public class InterlaunchGlobals {
    public final Config c;
    public final TrRoot t = new TrRoot();
    private ITranslator translator = new NullTranslator();
    private MVMEnvR48 langVM;
    private IConsumer<MVMEnv> reportVMChanges;

    public InterlaunchGlobals(Config c, IConsumer<MVMEnv> report, IConsumer<String> loadProgress) {
        this.c = c;
        reportVMChanges = report;
        updateLanguage(loadProgress);
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
        langVM = new MVMEnvR48(loadProgress);
        MVMR48GlobalLibraries.add(langVM);
        langVM.include("vm/global", false);
        // if the language author wants English fallback, they'll just (include "terms/eng/init")
        langVM.include("terms/" + c.language + "/init", true);
        t.fillFromVM(langVM);
        reportVMChanges.accept(langVM);
        if (lang.equals(LanguageList.hardcodedLang)) {
            translator = new NullTranslator();
        } else {
            translator = new Translator(lang);
        }
        translator.read("Systerms/" + lang + ".txt", "r48/");
        translator.read("Systerms/L-" + lang + ".txt", "launcher/");
        GaBIEn.wordLoad = t.g.wordLoad;
        GaBIEn.wordSave = t.g.wordSave;
        GaBIEn.wordInvalidFileName = t.g.wordInvalidFileName;
    }

    /**
     * Translates an internal string.
     */
    public String tr(String text) {
        return translator.tr("r48", text);
    }

    /**
     * Translates a launcher metadata string.
     */
    public String trL(String text) {
        return translator.tr("launcher", text);
    }

    /**
     * Passthrough to translation dumper
     */
    public void translationDump(String string, String string2) {
        translator.dump(string, string2);
    }
}

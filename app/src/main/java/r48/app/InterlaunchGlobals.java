/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import gabien.GaBIEn;
import r48.cfg.Config;
import r48.minivm.MVMContext;
import r48.minivm.MVMGlobalLibrary;
import r48.tr.ITranslator;
import r48.tr.LanguageList;
import r48.tr.NullTranslator;
import r48.tr.Translator;

/**
 * Globals shared between launcher and app
 * Created 28th February, 2023
 */
public class InterlaunchGlobals {
    public final Config c;
    private MVMContext langVm;
    private ITranslator translator = new NullTranslator();

    public InterlaunchGlobals(Config c) {
        this.c = c;
        langVm = new MVMContext();
        MVMGlobalLibrary.add(langVm, this);
    }

    /**
     * Sets the language of ILG to the given one.
     * Will reset language to English if not found.
     * If the app is running you're expected to figure that out yourself.
     */
    public void updateLanguage() {
        String lang = c.language;
        if (!LanguageList.hasLanguage(lang))
            lang = "English";
        c.language = lang;
        if (lang.equals("English")) {
            translator = new NullTranslator();
        } else {
            translator = new Translator(lang);
        }
        translator.read("Systerms/" + lang + ".txt", "r48/");
        translator.read("Systerms/L-" + lang + ".txt", "launcher/");
        GaBIEn.wordLoad = tr("Load");
        GaBIEn.wordSave = tr("Save");
        GaBIEn.wordInvalidFileName = tr("Invalid or missing file name.");
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

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.cfg;

import java.util.LinkedList;

import gabien.GaBIEn;
import r48.AdHocSaveLoad;
import r48.RubyIO;
import r48.cfg.FontSizes.FontSizeField;
import r48.io.data.IRIO;

/**
 * Here goes nothing...
 * Created 27th February 2023
 */
public class ConfigIO {
    // Notably, THESE IGNORE ROOT PATH!!!!
    // This is on purpose.

    public static void save(Config c) {
        RubyIO prepare = new RubyIO();
        prepare.type = 'o';
        prepare.symVal = "R48::FontConfig";
        for (FontSizeField fsf : c.f.getFields())
            prepare.addIVar("@" + fsf.configID, new RubyIO().setFX(fsf.get()));

        prepare.addIVar("@secondary_images_list", encodeStringList(c.secondaryImageLoadLocationBackup));
        prepare.addIVar("@saved_rootpath_list", encodeStringList(c.rootPathBackup));

        prepare.addIVar("@lang", new RubyIO().setString(c.language, true));
        if (c.fontOverride != null) {
            prepare.addIVar("@sysfont", new RubyIO().setString(c.fontOverride, true));
            prepare.addIVar("@sysfont_ue8", new RubyIO().setBool(c.fontOverrideUE8));
        }
        prepare.addIVar("@theme_variant", new RubyIO().setFX(c.borderTheme));
        prepare.addIVar("@actual_blending", new RubyIO().setBool(c.allowBlending));
        prepare.addIVar("@windowing_external", new RubyIO().setBool(c.windowingExternal));
        AdHocSaveLoad.save("fonts", prepare);
    }

    private static RubyIO encodeStringList(LinkedList<String> values) {
        RubyIO arr = new RubyIO().setArray();
        arr.arrVal = new IRIO[values.size()];
        int idx = 0;
        for (String s : values)
            arr.arrVal[idx++] = new RubyIO().setString(s, true);
        return arr;
    }

    public static boolean load(boolean first, Config c) {
        // NOTE: Use internal string methods here, this is a game-independent file
        RubyIO dat = AdHocSaveLoad.load("fonts");
        if (dat != null) {
            // Compatibility flags
            boolean shouldResetIETH = false;
            boolean shouldResetWSZ = false;

            for (FontSizeField fsf : c.f.getFields()) {
                RubyIO f = dat.getInstVarBySymbol("@" + fsf.configID);
                if (f != null) {
                    fsf.accept((int) f.fixnumVal);
                } else {
                    if (fsf.configID.equals("imageEditorTextHeight"))
                        shouldResetIETH = true;
                    if (fsf.configID.equals("maintabsScrollersize"))
                        shouldResetWSZ = true;
                }
            }

            if (shouldResetIETH)
                c.f.imageEditorTH = c.f.schemaFieldTH;
            if (shouldResetWSZ)
                c.f.maintabsS = c.f.mapToolbarS;

            RubyIO sys = dat.getInstVarBySymbol("@sysfont");
            if (sys != null) {
                c.fontOverride = sys.decString();
            } else {
                c.fontOverride = null;
            }
            RubyIO sys2 = dat.getInstVarBySymbol("@sysfont_ue8");
            if (sys2 != null)
                c.fontOverrideUE8 = sys2.type == 'T';
            // old paths
            RubyIO sys3 = dat.getInstVarBySymbol("@secondary_images");
            if (sys3 != null)
                c.secondaryImageLoadLocationBackup.add(sys3.decString());
            RubyIO sys4 = dat.getInstVarBySymbol("@saved_rootpath");
            if (sys4 != null)
                c.rootPathBackup.add(sys4.decString());
            // new paths
            RubyIO sys3a = dat.getInstVarBySymbol("@secondary_images_list");
            if (sys3a != null) {
                c.secondaryImageLoadLocationBackup.clear();
                for (IRIO rio : sys3a.arrVal)
                    c.secondaryImageLoadLocationBackup.add(rio.decString());
            }
            RubyIO sys4a = dat.getInstVarBySymbol("@saved_rootpath_list");
            if (sys4a != null) {
                c.rootPathBackup.clear();
                for (IRIO rio : sys4a.arrVal)
                    c.rootPathBackup.add(rio.decString());
            }
            // ...
            RubyIO sys5 = dat.getInstVarBySymbol("@theme_variant");
            if (sys5 != null)
                c.borderTheme = (int) sys5.fixnumVal;
            RubyIO sys6 = dat.getInstVarBySymbol("@actual_blending");
            if (sys6 != null)
                c.allowBlending = sys6.type == 'T';
            RubyIO sys7 = dat.getInstVarBySymbol("@windowing_external");
            if (sys7 != null)
                c.windowingExternal = sys7.type == 'T';
            return true;
        } else if (first) {
            c.fontOverride = GaBIEn.getFontOverrides()[0];
        }
        return false;
    }
}

/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.FontManager;
import gabien.GaBIEn;
import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIBorderedElement;
import gabienapp.Application;
import r48.dbs.TXDB;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Font size configuration.
 * Created on 1/29/17.
 */
public class FontSizes {
    // I'm unsure how these stay in order, but they do.
    // NOTE: TXDB ANNOTATIONS REQUIRED HERE! The comments are still picked up by the translation aid.

    @FontSizeDefault(16)
    public static int schemaPathTextHeight;
    @FontSizeDefault(16)
    public static int schemaFieldTextHeight; // also class names
    @FontSizeDefault(16)
    public static int schemaArrayAddTextHeight;
    @FontSizeDefault(16)
    public static int enumChoiceTextHeight;
    @FontSizeDefault(16)
    public static int blobTextHeight;

    @FontSizeDefault(6)
    public static int gridTextHeight;
    @FontSizeDefault(16)
    public static int tableElementTextHeight;
    @FontSizeDefault(16)
    public static int tableSizeTextHeight;
    @FontSizeDefault(16)
    public static int tableResizeTextHeight;

    @FontSizeDefault(16)
    public static int mapPositionTextHeight;
    @FontSizeDefault(16)
    public static int mapInfosTextHeight; // separate tab!
    @FontSizeDefault(16)
    public static int mapLayertabTextHeight;

    @FontSizeDefault(16)
    public static int eventPickerEntryTextHeight;
    @FontSizeDefault(6)
    public static int tilesTabTextHeight;
    @FontSizeDefault(8)
    public static int atSubtoolTextHeight;

    @FontSizeDefault(16)
    public static int rmaTimeframeTextHeight;
    @FontSizeDefault(16)
    public static int rmaCellTextHeight;

    @FontSizeDefault(6)
    public static int tonePickerTextHeight;

    @FontSizeDefault(16)
    public static int imiAsmButtonsTextHeight;
    @FontSizeDefault(16)
    public static int imiAsmAssetTextHeight;

    @FontSizeDefault(16)
    public static int dialogWindowTextHeight;
    @FontSizeDefault(16)
    public static int textDialogFieldTextHeight;
    @FontSizeDefault(16)
    public static int textDialogDescTextHeight;

    @FontSizeDefault(16)
    public static int helpTextHeight;
    @FontSizeDefault(16)
    public static int helpLinkHeight;
    @FontSizeDefault(16)
    public static int helpPathHeight;

    @FontSizeDefault(16)
    public static int inspectorBackTextHeight;
    @FontSizeDefault(8)
    public static int inspectorTextHeight;

    @FontSizeDefault(16)
    public static int windowFrameHeight;
    @FontSizeDefault(16)
    public static int statusBarTextHeight;
    @FontSizeDefault(16)
    public static int tabTextHeight;
    @FontSizeDefault(16)
    public static int menuTextHeight;

    @FontSizeDefault(8)
    public static int maintabsScrollersize;

    @FontSizeDefault(16)
    public static int objectDBMonitorTextHeight;
    @FontSizeDefault(16)
    public static int fontSizerTextHeight;
    @FontSizeDefault(16)
    public static int gSysCoreTextHeight; // Has a special hook needed to make this work
    @FontSizeDefault(16)
    public static int launcherTextHeight;
    @FontSizeDefault(16)
    public static int imageEditorTextHeight; // Compat. check is performed to ensure this doesn't get reset to 16 - see load

    @FontSizeDefault(8)
    public static int mapToolbarScrollersize;
    @FontSizeDefault(8)
    public static int tilesTabScrollersize;
    @FontSizeDefault(8)
    public static int schemaPagerTabScrollersize;

    @FontSizeDefault(24)
    public static int gridScrollersize;
    @FontSizeDefault(8)
    public static int cellSelectScrollersize;
    @FontSizeDefault(24)
    public static int generalScrollersize;
    @FontSizeDefault(24)
    public static int menuScrollersize;

    @FontSizeDefault(10)
    public static int uiGuessScaleTenths;
    @FontSizeDefault(10)
    public static int uiGridScaleTenths;

    public static void reset() {
        Application.allowBlending = true;
        Application.windowingExternal = false;
        try {
            for (final Field field : FontSizes.class.getFields())
                field.setInt(null, field.getAnnotation(FontSizeDefault.class).value());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        FontManager.fontOverride = GaBIEn.getFontOverrides()[0];
        FontManager.fontOverrideUE8 = false;
        Application.secondaryImageLoadLocation = "";
        Application.rootPathBackup = "";
        Application.windowingExternal = false;
    }

    // This hides the implied reflection for simplicity
    public static LinkedList<FontSizeField> getFields() {
        LinkedList<FontSizeField> fields = new LinkedList<FontSizeField>();
        for (final Field field : FontSizes.class.getFields())
            if (field.getType() == int.class)
                fields.add(new FontSizeField(field));
        return fields;
    }

    // Notably, THESE IGNORE ROOT PATH!!!!
    // This is on purpose.

    public static void save() {
        RubyIO prepare = new RubyIO();
        prepare.type = 'o';
        prepare.symVal = "R48::FontConfig";
        for (FontSizeField fsf : getFields())
            prepare.addIVar("@" + fsf.name, new RubyIO().setFX(fsf.get()));

        prepare.addIVar("@secondary_images", new RubyIO().setString(Application.secondaryImageLoadLocation, true));
        prepare.addIVar("@saved_rootpath", new RubyIO().setString(Application.rootPathBackup, true));
        prepare.addIVar("@lang", new RubyIO().setString(TXDB.getLanguage(), true));
        if (FontManager.fontOverride != null) {
            prepare.addIVar("@sysfont", new RubyIO().setString(FontManager.fontOverride, true));
            prepare.addIVar("@sysfont_ue8", new RubyIO().setBool(FontManager.fontOverrideUE8));
        }
        prepare.addIVar("@theme_variant", new RubyIO().setFX(UIBorderedElement.borderTheme));
        prepare.addIVar("@actual_blending", new RubyIO().setBool(Application.allowBlending));
        prepare.addIVar("@windowing_external", new RubyIO().setBool(Application.windowingExternal));
        AdHocSaveLoad.save("fonts", prepare);
    }

    public static boolean load(boolean first) {
        // NOTE: Use internal string methods here, this is a game-independent file
        RubyIO dat = AdHocSaveLoad.load("fonts");
        if (dat != null) {
            // Compatibility flags
            boolean shouldResetIETH = false;
            boolean shouldResetWSZ = false;

            for (FontSizeField fsf : getFields()) {
                RubyIO f = dat.getInstVarBySymbol("@" + fsf.name);
                if (f != null) {
                    fsf.accept((int) f.fixnumVal);
                } else {
                    if (fsf.name.equals("imageEditorTextHeight"))
                        shouldResetIETH = true;
                    if (fsf.name.equals("maintabsScrollersize"))
                        shouldResetWSZ = true;
                }
            }

            if (shouldResetIETH)
                imageEditorTextHeight = schemaFieldTextHeight;
            if (shouldResetWSZ)
                maintabsScrollersize = mapToolbarScrollersize;

            RubyIO sys = dat.getInstVarBySymbol("@sysfont");
            if (sys != null) {
                FontManager.fontOverride = sys.decString();
            } else {
                FontManager.fontOverride = null;
            }
            RubyIO sys2 = dat.getInstVarBySymbol("@sysfont_ue8");
            if (sys2 != null)
                FontManager.fontOverrideUE8 = sys2.type == 'T';
            RubyIO sys3 = dat.getInstVarBySymbol("@secondary_images");
            if (sys3 != null)
                Application.secondaryImageLoadLocation = sys3.decString();
            RubyIO sys4 = dat.getInstVarBySymbol("@saved_rootpath");
            if (sys4 != null)
                Application.rootPathBackup = sys4.decString();
            RubyIO sys5 = dat.getInstVarBySymbol("@theme_variant");
            if (sys5 != null)
                UIBorderedElement.borderTheme = (int) sys5.fixnumVal;
            RubyIO sys6 = dat.getInstVarBySymbol("@actual_blending");
            if (sys6 != null)
                Application.allowBlending = sys6.type == 'T';
            RubyIO sys7 = dat.getInstVarBySymbol("@windowing_external");
            if (sys7 != null)
                Application.windowingExternal = sys7.type == 'T';
            return true;
        } else if (first) {
            FontManager.fontOverride = GaBIEn.getFontOverrides()[0];
        }
        return false;
    }

    // Notably, language is loaded early, and is not loaded along with font sizes in general.
    // This is so that TXDB & such can start up.
    // Returns true if sysfont is disabled (see caller in Application)
    public static boolean loadLanguage() {
        RubyIO dat = AdHocSaveLoad.load("fonts");
        boolean sysfontDisabled = false;
        if (dat != null) {
            sysfontDisabled = dat.getInstVarBySymbol("@sysfont") == null;
            RubyIO sys = dat.getInstVarBySymbol("@lang");
            if (sys != null)
                TXDB.setLanguage(sys.decString());
        }
        return sysfontDisabled;
    }

    public static class FontSizeField implements IConsumer<Integer>, ISupplier<Integer> {
        // untranslated
        public final String name;
        private final Field intern;

        public FontSizeField(Field i) {
            name = i.getName();
            intern = i;
        }

        @Override
        public void accept(Integer integer) {
            try {
                if (name.equals("gSysCoreTextHeight"))
                    GaBIEn.sysCoreFontSize = integer;
                intern.setInt(null, integer);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Integer get() {
            try {
                return intern.getInt(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int scaleGuess(int defaultVal) {
        return (defaultVal * uiGuessScaleTenths) / 10;
    }

    public static int scaleGrid(int defaultVal) {
        return (defaultVal * uiGridScaleTenths) / 10;
    }

    public static int getSpriteScale() {
        return ((FontSizes.uiGuessScaleTenths + 5) / 10);
    }
}

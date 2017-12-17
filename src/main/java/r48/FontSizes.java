/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UILabel;
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

    // TXDB.get("schemaPathTextHeight")
    public static int schemaPathTextHeight = 16;
    // TXDB.get("schemaFieldTextHeight")
    public static int schemaFieldTextHeight = 16; // also class names
    // TXDB.get("schemaButtonTextHeight")
    public static int schemaButtonTextHeight = 16;
    // TXDB.get("schemaArrayAddTextHeight")
    public static int schemaArrayAddTextHeight = 16;
    // TXDB.get("enumChoiceTextHeight")
    public static int enumChoiceTextHeight = 16;
    // TXDB.get("blobTextHeight")
    public static int blobTextHeight = 16;

    // TXDB.get("gridTextHeight")
    public static int gridTextHeight = 8;
    // TXDB.get("tableElementTextHeight")
    public static int tableElementTextHeight = 16;
    // TXDB.get("tableSizeTextHeight")
    public static int tableSizeTextHeight = 16;
    // TXDB.get("tableResizeTextHeight")
    public static int tableResizeTextHeight = 16;

    // TXDB.get("mapPositionTextHeight")
    public static int mapPositionTextHeight = 16;
    // TXDB.get("mapInfosTextHeight")
    public static int mapInfosTextHeight = 16; // separate tab!
    // TXDB.get("mapLayertabTextHeight")
    public static int mapLayertabTextHeight = 16;

    // TXDB.get("eventPickerEntryTextHeight")
    public static int eventPickerEntryTextHeight = 16;
    // TXDB.get("tilesTabTextHeight")
    public static int tilesTabTextHeight = 6;
    // TXDB.get("atSubtoolTextHeight")
    public static int atSubtoolTextHeight = 8;

    // TXDB.get("rmaTimeframeTextHeight")
    public static int rmaTimeframeTextHeight = 8;
    // TXDB.get("rmaCellTextHeight")
    public static int rmaCellTextHeight = 16;

    // TXDB.get("tonePickerTextHeight")
    public static int tonePickerTextHeight = 6;

    // TXDB.get("imiAsmButtonsTextHeight")
    public static int imiAsmButtonsTextHeight = 16;
    // TXDB.get("imiAsmAssetTextHeight")
    public static int imiAsmAssetTextHeight = 16;

    // TXDB.get("dialogWindowTextHeight")
    public static int dialogWindowTextHeight = 16;
    // TXDB.get("textDialogFieldTextHeight")
    public static int textDialogFieldTextHeight = 16;
    // TXDB.get("textDialogDescTextHeight")
    public static int textDialogDescTextHeight = 16;

    // TXDB.get("helpParagraphStartHeight")
    public static int helpParagraphStartHeight = 16;
    // TXDB.get("helpTextHeight")
    public static int helpTextHeight = 16;
    // TXDB.get("helpLinkHeight")
    public static int helpLinkHeight = 16;
    // TXDB.get("helpPathHeight")
    public static int helpPathHeight = 16;

    // TXDB.get("inspectorBackTextHeight")
    public static int inspectorBackTextHeight = 16;
    // TXDB.get("inspectorTextHeight")
    public static int inspectorTextHeight = 8;

    // TXDB.get("windowFrameHeight")
    public static int windowFrameHeight = 16;
    // TXDB.get("statusBarTextHeight")
    public static int statusBarTextHeight = 16;
    // TXDB.get("tabTextHeight")
    public static int tabTextHeight = 16;

    // TXDB.get("objectDBMonitorTextHeight")
    public static int objectDBMonitorTextHeight = 16;
    // TXDB.get("menuTextHeight")
    public static int menuTextHeight = 16;

    // TXDB.get("fontSizerTextHeight")
    public static int fontSizerTextHeight = 16;
    // TXDB.get("timeWasterTextHeight")
    public static int timeWasterTextHeight = 16;

    // TXDB.get("launcherTextHeight")
    public static int launcherTextHeight = 16;

    // TXDB.get("mapToolbarScrollersize")
    public static int mapToolbarScrollersize = 8;

    // TXDB.get("gridScrollersize")
    public static int gridScrollersize = 24;

    // TXDB.get("cellSelectScrollersize")
    public static int cellSelectScrollersize = 8;

    // TXDB.get("generalScrollersize")
    public static int generalScrollersize = 24;

    // TXDB.get("uiGuessScaleTenths")
    public static int uiGuessScaleTenths = 10;

    // TXDB.get("uiGridScaleTenths")
    public static int uiGridScaleTenths = 10;

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

        prepare.addIVar("@secondary_images", new RubyIO().setString(Application.secondaryImageLoadLocation));
        prepare.addIVar("@saved_rootpath", new RubyIO().setString(Application.rootPathBackup));
        prepare.addIVar("@lang", new RubyIO().setString(TXDB.getLanguage()));
        if (UILabel.fontOverride != null) {
            prepare.addIVar("@sysfont", new RubyIO().setString(UILabel.fontOverride));
            prepare.addIVar("@sysfont_ue8", new RubyIO().setBool(UILabel.fontOverrideUE8));
        }
        AdHocSaveLoad.save("fonts", prepare);
    }

    public static boolean load() {
        // NOTE: Use internal string methods here, this is a game-independent file
        RubyIO dat = AdHocSaveLoad.load("fonts");
        if (dat != null) {
            for (FontSizeField fsf : getFields()) {
                RubyIO f = dat.getInstVarBySymbol("@" + fsf.name);
                if (f != null)
                    fsf.accept((int) f.fixnumVal);
            }
            RubyIO sys = dat.getInstVarBySymbol("@sysfont");
            if (sys != null) {
                UILabel.fontOverride = sys.decString();
            } else {
                UILabel.fontOverride = null;
            }
            RubyIO sys2 = dat.getInstVarBySymbol("@sysfont_ue8");
            if (sys2 != null)
                UILabel.fontOverrideUE8 = sys2.type == 'T';
            RubyIO sys3 = dat.getInstVarBySymbol("@secondary_images");
            if (sys3 != null)
                Application.secondaryImageLoadLocation = sys3.decString();
            RubyIO sys4 = dat.getInstVarBySymbol("@saved_rootpath");
            if (sys4 != null)
                Application.rootPathBackup = sys4.decString();
            return true;
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

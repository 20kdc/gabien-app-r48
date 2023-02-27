/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.uslx.append.*;
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

    // This hides the implied reflection for simplicity
    public LinkedList<FontSizeField> getFields() {
        LinkedList<FontSizeField> fields = new LinkedList<FontSizeField>();
        for (final Field field : FontSizes.class.getFields())
            if (field.getType() == int.class)
                fields.add(new FontSizeField(field));
        return fields;
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
        public final int defValue;
        private final Field intern;

        public FontSizeField(Field i) {
            name = i.getName();
            defValue = i.getAnnotation(FontSizeDefault.class).value();
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
        return ((uiGuessScaleTenths + 5) / 10);
    }
}

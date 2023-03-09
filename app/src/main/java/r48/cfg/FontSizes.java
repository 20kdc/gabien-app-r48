/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.cfg;

import gabien.GaBIEn;
import gabien.uslx.append.*;
import r48.tr.pages.TrFontSizes;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Font size configuration.
 * Created on 1/29/17.
 */
public class FontSizes {
    // I'm unsure how these stay in order, but they do.
    // Beware that the names must correspond to TrFontSizes.

    @FontSizeDefault(16)
    public int schemaPathTextHeight;
    @FontSizeDefault(16)
    public int schemaFieldTextHeight; // also class names
    @FontSizeDefault(16)
    public int schemaArrayAddTextHeight;
    @FontSizeDefault(16)
    public int enumChoiceTextHeight;
    @FontSizeDefault(16)
    public int blobTextHeight;

    @FontSizeDefault(6)
    public int gridTextHeight;
    @FontSizeDefault(16)
    public int tableElementTextHeight;
    @FontSizeDefault(16)
    public int tableSizeTextHeight;
    @FontSizeDefault(16)
    public int tableResizeTextHeight;

    @FontSizeDefault(16)
    public int mapPositionTextHeight;
    @FontSizeDefault(16)
    public int mapInfosTextHeight; // separate tab!
    @FontSizeDefault(16)
    public int mapLayertabTextHeight;

    @FontSizeDefault(16)
    public int eventPickerEntryTextHeight;
    @FontSizeDefault(6)
    public int tilesTabTextHeight;
    @FontSizeDefault(8)
    public int atSubtoolTextHeight;

    @FontSizeDefault(16)
    public int rmaTimeframeTextHeight;
    @FontSizeDefault(16)
    public int rmaCellTextHeight;

    @FontSizeDefault(6)
    public int tonePickerTextHeight;

    @FontSizeDefault(16)
    public int dialogWindowTextHeight;
    @FontSizeDefault(16)
    public int textDialogFieldTextHeight;
    @FontSizeDefault(16)
    public int textDialogDescTextHeight;

    @FontSizeDefault(16)
    public int helpTextHeight;
    @FontSizeDefault(16)
    public int helpLinkHeight;
    @FontSizeDefault(16)
    public int helpPathHeight;

    @FontSizeDefault(16)
    public int inspectorBackTextHeight;
    @FontSizeDefault(8)
    public int inspectorTextHeight;

    @FontSizeDefault(16)
    public int windowFrameHeight;
    @FontSizeDefault(16)
    public int statusBarTextHeight;
    @FontSizeDefault(16)
    public int tabTextHeight;
    @FontSizeDefault(16)
    public int menuTextHeight;

    @FontSizeDefault(8)
    public int maintabsScrollersize;

    @FontSizeDefault(16)
    public int objectDBMonitorTextHeight;
    @FontSizeDefault(16)
    public int fontSizerTextHeight;
    @FontSizeDefault(16)
    public int gSysCoreTextHeight; // Has a special hook needed to make this work
    @FontSizeDefault(16)
    public int launcherTextHeight;
    @FontSizeDefault(16)
    public int imageEditorTextHeight; // Compat. check is performed to ensure this doesn't get reset to 16 - see load

    @FontSizeDefault(8)
    public int mapToolbarScrollersize;
    @FontSizeDefault(8)
    public int tilesTabScrollersize;
    @FontSizeDefault(8)
    public int schemaPagerTabScrollersize;

    @FontSizeDefault(24)
    public int gridScrollersize;
    @FontSizeDefault(8)
    public int cellSelectScrollersize;
    @FontSizeDefault(24)
    public int generalScrollersize;
    @FontSizeDefault(24)
    public int menuScrollersize;

    @FontSizeDefault(10)
    public int uiGuessScaleTenths;
    @FontSizeDefault(10)
    public int uiGridScaleTenths;

    // This hides the implied reflection for simplicity
    public LinkedList<FontSizeField> getFields() {
        LinkedList<FontSizeField> fields = new LinkedList<FontSizeField>();
        for (final Field field : FontSizes.class.getFields())
            if (field.getType() == int.class)
                fields.add(new FontSizeField(field));
        return fields;
    }

    public class FontSizeField implements IConsumer<Integer>, ISupplier<Integer> {
        // untranslated
        public final String name;
        public final int defValue;
        private final Field intern;
        private final Field trPageField;

        public FontSizeField(Field i) {
            name = i.getName();
            defValue = i.getAnnotation(FontSizeDefault.class).value();
            intern = i;
            try {
                trPageField = TrFontSizes.class.getField(name);
            } catch (Exception ex) {
                throw new RuntimeException("error with font size xref " + name);
            }
        }

        public String trName(TrFontSizes fs) {
            try {
                return (String) trPageField.get(fs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void accept(Integer integer) {
            try {
                if (name.equals("gSysCoreTextHeight"))
                    GaBIEn.sysCoreFontSize = integer;
                intern.setInt(FontSizes.this, integer);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Integer get() {
            try {
                return intern.getInt(FontSizes.this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int scaleGuess(int defaultVal) {
        return (defaultVal * uiGuessScaleTenths) / 10;
    }

    public int scaleGrid(int defaultVal) {
        return (defaultVal * uiGridScaleTenths) / 10;
    }

    public int getSpriteScale() {
        return ((uiGuessScaleTenths + 5) / 10);
    }
}

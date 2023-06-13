/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.cfg;

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

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int schemaPathTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int schemaFieldTH; // also class names
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int schemaArrayAddTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int enumChoiceTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int blobTH;

    @FontSizeDefault(6) @FontSizeOName("TH/TextHeight")
    public int gridTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int tableElementTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int tableSizeTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int tableResizeTH;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int mapPositionTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int mapInfosTH; // separate tab!
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int mapLayertabTH;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int eventPickerEntryTH;
    @FontSizeDefault(6) @FontSizeOName("TH/TextHeight")
    public int tilesTabTH;
    @FontSizeDefault(8) @FontSizeOName("TH/TextHeight")
    public int atSubtoolTH;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int rmaTimeframeTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int rmaCellTH;

    @FontSizeDefault(6) @FontSizeOName("TH/TextHeight")
    public int tonePickerTH;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int dialogWindowTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int textDialogFieldTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int textDialogDescTH;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int helpTH;
    @FontSizeDefault(16) @FontSizeOName("H/Height")
    public int helpLinkH;
    @FontSizeDefault(16) @FontSizeOName("H/Height")
    public int helpPathH;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int inspectorBackTH;
    @FontSizeDefault(8) @FontSizeOName("TH/TextHeight")
    public int inspectorTH;

    @FontSizeDefault(16) @FontSizeOName("H/Height")
    public int windowFrameH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int statusBarTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int tabTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int menuTH;

    @FontSizeDefault(8) @FontSizeOName("S/Scrollersize")
    public int maintabsS;

    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int objectDBMonitorTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int fontSizerTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int gSysCoreTH; // Has a special hook needed to make this work
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int launcherTH;
    @FontSizeDefault(16) @FontSizeOName("TH/TextHeight")
    public int imageEditorTH; // Compat. check is performed to ensure this doesn't get reset to 16 - see load

    @FontSizeDefault(8) @FontSizeOName("S/Scrollersize")
    public int mapToolbarS;
    @FontSizeDefault(8) @FontSizeOName("S/Scrollersize")
    public int tilesTabS;
    @FontSizeDefault(8) @FontSizeOName("S/Scrollersize")
    public int schemaPagerTabS;

    @FontSizeDefault(24) @FontSizeOName("S/Scrollersize")
    public int gridS;
    @FontSizeDefault(8) @FontSizeOName("S/Scrollersize")
    public int cellSelectS;
    @FontSizeDefault(24) @FontSizeOName("S/Scrollersize")
    public int generalS;
    @FontSizeDefault(24) @FontSizeOName("S/Scrollersize")
    public int menuS;

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
        // config
        public final String configID;
        public final int defValue;
        private final Field intern;
        private final Field trPageField;

        public FontSizeField(Field i) {
            name = i.getName();
            String ecid = name;
            FontSizeOName tf = i.getAnnotation(FontSizeOName.class);
            if (tf != null) {
                String[] tfp = tf.value().split("/");
                ecid = ecid.replaceAll(tfp[0], tfp[1]);
            }
            configID = ecid;
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

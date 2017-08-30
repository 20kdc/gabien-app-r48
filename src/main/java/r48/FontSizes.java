/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UILabel;
import r48.dbs.TXDB;
import r48.io.R48ObjectBackend;

import java.io.IOException;
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
    // TXDB.get("mapDebugTextHeight")
    public static int mapDebugTextHeight = 6;
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

        if (UILabel.fontOverride != null) {
            String currentEnc = RubyIO.encoding;
            RubyIO.encoding = "UTF-8";

            prepare.addIVar("@sysfont", new RubyIO().setString(UILabel.fontOverride));

            RubyIO.encoding = currentEnc;
        }
        AdHocSaveLoad.save("fonts", prepare);
    }

    public static void load() {
        RubyIO dat = AdHocSaveLoad.load("fonts");
        if (dat != null) {
            for (FontSizeField fsf : getFields()) {
                RubyIO f = dat.getInstVarBySymbol("@" + fsf.name);
                if (f != null)
                    fsf.accept((int) f.fixnumVal);
            }
            RubyIO sys = dat.getInstVarBySymbol("@sysfont");
            if (sys != null) {
                String currentEnc = RubyIO.encoding;
                RubyIO.encoding = "UTF-8";

                UILabel.fontOverride = sys.decString();

                RubyIO.encoding = currentEnc;
            } else {
                UILabel.fontOverride = null;
            }
        }
    }

    public static class FontSizeField implements IConsumer<Integer>, ISupplier<Integer> {
        public final String name;
        private final Field intern;
        public FontSizeField(Field i) {
            // need to translate this somehow
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
}

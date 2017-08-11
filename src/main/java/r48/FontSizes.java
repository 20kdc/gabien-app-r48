/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
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
    public static int schemaPathTextHeight = 16;
    public static int schemaFieldTextHeight = 16; // also class names
    public static int schemaButtonTextHeight = 16;
    public static int schemaArrayAddTextHeight = 16;
    public static int enumChoiceTextHeight = 16;
    public static int blobTextHeight = 16;

    public static int gridTextHeight = 8;
    public static int tableElementTextHeight = 16;
    public static int tableSizeTextHeight = 16;
    public static int tableResizeTextHeight = 16;

    public static int mapPositionTextHeight = 16;
    public static int mapDebugTextHeight = 6;
    public static int mapInfosTextHeight = 16; // separate tab!
    public static int mapLayertabTextHeight = 16;

    public static int eventPickerEntryTextHeight = 16;
    public static int tilesTabTextHeight = 6;
    public static int atSubtoolTextHeight = 8;

    public static int rmaTimeframeFontSize = 8;
    public static int rmaCellFontSize = 16;

    public static int dialogWindowTextHeight = 16;
    public static int textDialogFieldTextHeight = 16;
    public static int textDialogDescTextHeight = 16;

    public static int helpParagraphStartHeight = 16;
    public static int helpTextHeight = 16;
    public static int helpLinkHeight = 16;
    public static int helpPathHeight = 16;

    public static int inspectorBackTextHeight = 16;
    public static int inspectorTextHeight = 8;

    public static int windowFrameHeight = 16;
    public static int statusBarTextHeight = 16;
    public static int tabTextHeight = 16;

    public static int objectDBMonitorTextHeight = 16;
    public static int menuTextHeight = 16;

    public static int fontSizerTextHeight = 16;
    public static int timeWasterTextHeight = 16;

    public static int launcherTextHeight = 16;

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
        R48ObjectBackend rob = new R48ObjectBackend("", ".r48", false);
        try {
            rob.saveObjectToFile("fonts", prepare);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        R48ObjectBackend rob = new R48ObjectBackend("", ".r48", false);
        RubyIO dat = rob.loadObjectFromFile("fonts");
        if (dat != null) {
            for (FontSizeField fsf : getFields()) {
                RubyIO f = dat.getInstVarBySymbol("@" + fsf.name);
                if (f != null)
                    fsf.accept((int) f.fixnumVal);
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

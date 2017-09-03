/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.IGrInDriver;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextBox;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on August 31st 2017.
 */
public class StringLenSchemaElement extends StringSchemaElement {
    public int len;
    public StringLenSchemaElement(String arg, int l) {
        super(arg, '"');
        len = l;
    }

    // The logic goes as thus.
    // It's generally ALWAYS wide. (outside the BMP is generally always emoji)
    //
    boolean isWide(int i) {
        // NON-WIDENESS BY BLOCK:
        // Basic Latin (32-126): NEVER WIDE
        if (isRange(32, 126, i))
            return false;
        // Latin-1 Support, Latin Extended-A, Latin Extended-B, IPA, SML (150-767): NEVER WIDE
        if (isRange(150, 767, i))
            return false;
        // Greek And Coptic, Cyrillic, Cyrillic Supplement (880-1327): NEVER WIDE
        if (isRange(880, 1327, i))
            return false;
        // <SOME STUFF NEEDS TO BE CHECKED HERE>
        // Unified Canadian Aboriginal Syllabics
        if (isRange(5120, 5759, i))
            return false;
        // Runic
        if (isRange(5792, 5887, i))
            return false;
        // Khmer
        if (isRange(6016, 6143, i))
            return false;
        // <CHECK HERE. UCAS-Ext. has 'mixed bag' block, so just ignore that>
        // Tai Le
        if (isRange(6480, 6527, i))
            return false;
        // Ol Chiki, Cyrillic Extension-C (7248-7311): NEVER WIDE
        if (isRange(7248, 7311, i))
            return false;
        // Vedic Extensions, Phonetic Extensions, Phonetic Extensions Supplement, Combining Diacritical Marks Supplement, Latin Extended Additional, Greek Extended, General Punctuation,
        // Superscripts And Subscripts, Currency Symbols
        if (isRange(7376, 8399, i))
            return false;
        // <AT THIS POINT I GIVE UP>
        // Just support halfwidth katakana (Halfwidth and Fullwidth Forms)
        // This is a mixed bag block but with a clear "split"
        if (isRange(65377, 65519, i))
            return false;
        // <NO CODEPOINT PAST HERE OUGHT TO BE CHECKED, IT'S PROBABLY STILL IN FLUX>
        return true;
    }

    private boolean isRange(int i, int i1, int c) {
        return (c >= i) && (c <= i1);
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UITextBox utb = (UITextBox) super.buildHoldingEditor(target, launcher, path);
        UILabel l = new UILabel("-00000", FontSizes.schemaFieldTextHeight) {
            @Override
            public void updateAndRender(int ox, int oy, double DeltaTime, boolean selected, IGrInDriver igd) {
                int l1 = measureText(utb.text);
                Text = Integer.toString(len - l1);
                super.updateAndRender(ox, oy, DeltaTime, selected, igd);
            }
        };
        return new UISplitterLayout(utb, l, false, 1);
    }

    private int measureText(String s) {
        // Java sucks, java sucks again...
        int i = 0;
        int p = 0;
        while (i < s.length()) {
            int codepoint = s.codePointAt(i);
            i += Character.charCount(codepoint);
            p += isWide(codepoint) ? 2 : 1;
        }
        return p;
    }
}
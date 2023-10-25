/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import org.eclipse.jdt.annotation.Nullable;

import r48.app.AppCore;
import r48.dbs.RPGCommand;
import r48.io.data.RORIO;

/**
 * UNDERSTAND: These are estimates ONLY, intended to aid in translation.
 * Created 25th October, 2023.
 */
public abstract class TextAnalyzerCommandClassifier extends AppCore.Csv implements ICommandClassifier.Immutable {
    public TextAnalyzerCommandClassifier(AppCore ac) {
        super(ac);
    }

    public abstract boolean matches(String text);

    @Override
    public boolean matches(@Nullable RPGCommand target, @Nullable RORIO data) {
        if (target == null)
            return false;
        if (data == null)
            return false;
        if (target.textArg == -1)
            return false;
        return matches(data.getIVar("@parameters").getAElem(target.textArg).decString());
    }

    /**
     * Tests each character individually, ORs results basically
     */
    public abstract static class OrCharTest extends TextAnalyzerCommandClassifier {
        public OrCharTest(AppCore ac) {
            super(ac);
        }

        public abstract boolean testChar(char c);

        @Override
        public boolean matches(String text) {
            int len = text.length();
            for (int i = 0; i < len; i++)
                if (testChar(text.charAt(i)))
                    return true;
            return false;
        }
    }

    /**
     * Tests each character individually, ANDs results basically
     */
    public abstract static class AndCharTest extends TextAnalyzerCommandClassifier {
        public AndCharTest(AppCore ac) {
            super(ac);
        }

        public abstract boolean testChar(char c);

        @Override
        public boolean matches(String text) {
            int len = text.length();
            for (int i = 0; i < len; i++)
                if (testChar(text.charAt(i)))
                    return true;
            return false;
        }
    }

    /**
     * So originally there was separation here between Chinese/Japanese and Korean.
     * But apparently there's just overlap anyway.
     */
    public static class CJK extends OrCharTest {
        public CJK(AppCore ac) {
            super(ac);
        }

        @Override
        public String getName() {
            return app.t.u.ccs_cjk;
        }

        @Override
        public boolean testChar(char c) {
            // Hangul Jamo
            if (c >= 0x1100 && c <= 0x11FF)
                return true;
            // Hiragana, Katakana, and Bopomofo, Hangul Compatibility Jamo, Kanbun
            // Bopomofo Extended, CJK Strokes, Katakana Phonetic Extensions
            if (c >= 0x3040 && c <= 0x31FF)
                return true;
            // CJK Unified Ideographs Extension A
            if (c >= 0x3400 && c <= 0x4DBF)
                return true;
            // CJK Unified Ideographs
            if (c >= 0x4E00 && c <= 0x9FFF)
                return true;
            // Hangul Jamo Extended-A
            if (c >= 0xA960 && c <= 0xA97F)
                return true;
            // Hangul Syllables and Hangul Jamo Extended-B
            if (c >= 0xAC00 && c <= 0xD7FF)
                return true;
            // Halfwidth/fullwidth stuff
            if (c >= 0xFF65 && c <= 0xFFDF)
                return true;
            return false;
        }
    }

    /**
     * The text is solely made up of codepoints 0-255.
     */
    public static class Latin1Only extends AndCharTest {
        public Latin1Only(AppCore ac) {
            super(ac);
        }

        @Override
        public String getName() {
            return app.t.u.ccs_latin1Only;
        }

        @Override
        public boolean testChar(char c) {
            return c <= 0xFF;
        }
    }

    /**
     * Latin1Only plus those codepoints which don't strictly scream "Japanese or Korean text".
     * (This can be useful for translation projects working with these characters)
     */
    public static class Latin1AndFullwidthOnly extends AndCharTest {
        public Latin1AndFullwidthOnly(AppCore ac) {
            super(ac);
        }

        @Override
        public String getName() {
            return app.t.u.ccs_latin1AndFullwidthOnly;
        }

        @Override
        public boolean testChar(char c) {
            return (c <= 0xFF) || (c >= 0xFF00 && c <= 0xFF65) || (c >= 0xFFE0 && c <= 0xFFEF);
        }
    }
}

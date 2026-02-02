/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import r48.R48;
import r48.ui.AppUI;

/**
 * Just get this here.
 * This is to allow plugging in more generalized text transforms into USL.
 * UNDERSTAND: These are estimates ONLY, intended to aid in translation.
 * Created 25th October, 2023.
 */
public interface ITextAnalyzer extends IClassifierish<ITextAnalyzer.Instance> {
    /**
     * Instance of a text analyzer. Instances need not be unique.
     */
    interface Instance extends IClassifierish.BaseInstance {
        /**
         * Checks if text matches the analyzer.
         */
        boolean matches(String text);
    }

    /**
     * Immutable analyzers should extend this so they can be used by appropriate logic.
     */
    interface Immutable extends ITextAnalyzer, Instance {
        @Override
        default void setupEditor(@NonNull AppUI U, LinkedList<UIElement> usl, Runnable onEdit) {
        }

        @Override
        default Instance instance(R48 app) {
            return this;
        }
    }

    /**
     * Tests each character individually, ORs results basically
     */
    interface OrCharTest extends ITextAnalyzer.Instance {
        boolean testChar(char c);

        @Override
        default boolean matches(String text) {
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
    public static enum CJK implements ITextAnalyzer.Immutable, OrCharTest {
        I;

        @Override
        public String getName(R48 app) {
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
    public static enum NotLatin1 implements ITextAnalyzer.Immutable, OrCharTest {
        I;

        @Override
        public String getName(R48 app) {
            return app.t.u.ccs_latin1Only;
        }

        @Override
        public boolean testChar(char c) {
            return c > 0xFF;
        }
    }

    /**
     * Latin1Only plus those codepoints which don't strictly scream "Japanese or Korean text".
     * (This can be useful for translation projects working with these characters)
     */
    public static enum NotLatin1OrFullwidth implements ITextAnalyzer.Immutable, OrCharTest {
        I;

        @Override
        public String getName(R48 app) {
            return app.t.u.ccs_latin1AndFullwidthOnly;
        }

        @Override
        public boolean testChar(char c) {
            boolean l1orfw = (c <= 0xFF) || (c >= 0xFF00 && c <= 0xFF65) || (c >= 0xFFE0 && c <= 0xFFEF);
            return !l1orfw;
        }
    }
}

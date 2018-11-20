/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import r48.io.data.IRIO;

/**
 * Proper handling for encoding stuff
 * :E = true : UTF-8
 * :E = false : US-ASCII (safe to alias to UTF-8)
 * :encoding = "SHIFT_JIS" : SHIFT_JIS
 * :encoding = "ASCII-8BIT" : This is weird, but alias it to CP819
 * Created on December 17th 2017
 */
public class RubyEncodingTranslator {
    public static void inject(IRIO rubyIO, String s) {
        rubyIO.rmIVar("jEncoding");
        rubyIO.rmIVar("encoding");
        rubyIO.rmIVar("E");
        if (s.equalsIgnoreCase("UTF-8")) {
            rubyIO.addIVar("E").setBool(true);
            return;
        }
        if (s.equalsIgnoreCase("Cp1252")) {
            forceEncoding(rubyIO, "Windows-1252");
            return;
        }
        // Korean
        if (s.equalsIgnoreCase("MS949")) {
            forceEncoding(rubyIO, "CP949");
            return;
        }
        // Cp943C is described as a superset of SHIFT-JIS *and* MS932 (Japanese Windows), which is why R48 uses it for Japanese.
        // Unfortunately, in this case, not much can be done here apart from pretending SHIFT-JIS and this are the same.
        if (s.equalsIgnoreCase("Cp943C")) {
            forceEncoding(rubyIO, "SHIFT-JIS");
            return;
        }

        // Can't translate, use fallback
        // NOTE: This isn't too critically important, *unless a file from an "old" backend is copied to a "new" backend.*
        rubyIO.addIVar("jEncoding").setSymbol(s);
    }

    // Used to set the encoding iVar as a raw string
    private static void forceEncoding(IRIO rubyIO, String s) {
        rubyIO.addIVar("encoding").setString(s);
    }

    // Returns "Canonical Name for java.io API and java.lang API" as documented on "https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html".
    public static String getStringCharset(IRIO rubyIO) {
        IRIO easy = rubyIO.getIVar("E");
        if (easy != null)
            return "UTF-8";
        IRIO jencoding = rubyIO.getIVar("jEncoding");
        if (jencoding != null)
            return jencoding.getSymbol();
        IRIO encoding = rubyIO.getIVar("encoding");
        if (encoding != null) {
            String s = encoding.decString();
            // Japanese (see above function to explain the mapping)
            if (s.equalsIgnoreCase("SHIFT-JIS"))
                return "Cp943C";
            // Korean
            if (s.equalsIgnoreCase("CP949"))
                return "MS949";
            if (s.equalsIgnoreCase("CP850")) {
                // CP850 - OneShot
                return "Cp850";
            }
            // 1. Check known encoding names
            // 2. Guess the encoding name
            // 3. Throw monitors at user until they tell us the encoding
            try {
                new String(new byte[0], s);
                // let's just pretend this is a good idea, 'kay?
                return s;
            } catch (Exception e) {
            }
            s = s.replace('P', 'p');
            try {
                new String(new byte[0], s);
                // ...just in case.
                return s;
            } catch (Exception e) {
            }
        }

        // Sane default
        return "UTF-8";
    }
}

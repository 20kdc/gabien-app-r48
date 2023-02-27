/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

/**
 * Interface for translation backends.
 * Created 27th February 2023.
 */
public interface ITranslator {
    /**
     * Returns true if the given entry exists.
     */
    boolean has(String context, String text);

    /**
     * Translates the given entry.
     */
    String tr(String context, String text);

    /**
     * Reads the given file (or doesn't, if this is NullTranslator)
     */
    void read(String fn, String pfx);

    /**
     * Dumps the translation tables to a file.
     */
    void dump(String fnPrefix, String ctxPrefix);
}

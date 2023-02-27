/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

/**
 * Implementation of translation that does no translation.
 * Created 27th February 2023.
 */
public class NullTranslator implements ITranslator {
    public NullTranslator() {
    }

    @Override
    public boolean has(String context, String text) {
        return true;
    }

    @Override
    public String tr(String context, String text) {
        return text;
    }

    @Override
    public void read(String fn, String pfx) {
    }

    @Override
    public void dump(String prefix, String fn) {
    }
}

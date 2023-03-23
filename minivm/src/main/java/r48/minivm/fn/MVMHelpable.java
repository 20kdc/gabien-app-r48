/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

/**
 * Convenience
 * Created 28th February 2023.
 */
public abstract class MVMHelpable {
    public final String nameHint;
    public String help;
    public boolean excludeFromHelp;
    public MVMHelpable(String nh) {
        this.nameHint = nh;
    }

    public MVMHelpable attachHelp(String text) {
        help = text;
        return this;
    }
}

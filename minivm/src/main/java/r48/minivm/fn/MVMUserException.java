/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

/**
 * Created 17th March 2023.
 */
@SuppressWarnings("serial")
public class MVMUserException extends RuntimeException {
    public MVMUserException(String msg, Object... extra) {
        super(msg);
        // extra is discarded for now
    }
    @Override
    public String toString() {
        // we don't really need to know it's an MVMUserException do we now
        return getMessage();
    }
}

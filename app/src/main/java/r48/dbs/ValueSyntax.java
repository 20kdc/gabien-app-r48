/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.io.data.DMKey;
import r48.io.data.RORIO;

/**
 * Value syntax. Meant to be used from within EscapedStringSyntax or PathSyntax.
 * Most things are treated as int for compatibility.
 * However, " starts a string (no ending "),
 *  : starts a symbol,
 *  and nil means null.
 * "?" must never become part of this list, and it must never conflict with PathSyntax.
 * Created on 10/06/17.
 */
public class ValueSyntax {
    public static DMKey decode(String unescape) {
        if (unescape.equals("nil"))
            return DMKey.NULL;
        if (unescape.equals("true"))
            return DMKey.TRUE;
        if (unescape.equals("false"))
            return DMKey.FALSE;
        boolean str = unescape.startsWith("$");
        if (str) {
            return DMKey.ofStr(unescape.substring(1));
        } else if (unescape.startsWith(":")) {
            return DMKey.ofSym(unescape.substring(1));
        } else {
            return DMKey.of(Long.parseLong(unescape));
        }
    }

    // Returns "" if unencodable. Note that this is for use in hashes.
    public static String encode(RORIO val) {
        int type = val.getType();
        if (type == '0')
            return "nil";
        if (type == 'T')
            return "true";
        if (type == 'F')
            return "false";
        String v2 = "";
        if (type == '"') {
            v2 = "$" + val.decString();
        } else if (type == ':') {
            v2 = ":" + val.getSymbol();
        } else if (type == 'i') {
            v2 += val.getFX();
        }
        return v2;
    }
}

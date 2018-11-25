/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.ika;

import r48.io.data.IRIO;
import r48.io.data.IRIOFixedObject;
import r48.io.data.IRIOFixnum;

/**
 * Created on November 24, 2018.
 */
public class IkaEvent extends IRIOFixedObject {
    public IRIOFixnum x, y, tox, toy, type, status, scriptId, collisionType;

    public IkaEvent() {
        super("IkachanEvent", new String[] {
                "@x",
                "@y",
                "@tOX",
                "@tOY",
                "@type",
                "@status",
                "@scriptId",
                "@collisionType",
        });
        initialize();
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@x"))
            return x = new IRIOFixnum(0);
        if (sym.equals("@y"))
            return y = new IRIOFixnum(0);
        if (sym.equals("@tOX"))
            return tox = new IRIOFixnum(0);
        if (sym.equals("@tOY"))
            return toy = new IRIOFixnum(0);
        if (sym.equals("@type"))
            return type = new IRIOFixnum(0);
        if (sym.equals("@status"))
            return status = new IRIOFixnum(0);
        if (sym.equals("@scriptId"))
            return scriptId = new IRIOFixnum(0);
        if (sym.equals("@collisionType"))
            return collisionType = new IRIOFixnum(0);
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        if (sym.equals("@x"))
            return x;
        if (sym.equals("@y"))
            return y;
        if (sym.equals("@tOX"))
            return tox;
        if (sym.equals("@tOY"))
            return toy;
        if (sym.equals("@type"))
            return type;
        if (sym.equals("@status"))
            return status;
        if (sym.equals("@scriptId"))
            return scriptId;
        if (sym.equals("@collisionType"))
            return collisionType;
        return null;
    }
}

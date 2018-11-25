/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.ika;

import r48.RubyTable;
import r48.io.data.*;

/**
 * Data Model 2 proof of concept
 * Created on November 22, 2018.
 */
public class IkaMap extends IRIOFixedObject {
    private static String[] vtbl = new String[] {
            "@data",
            "@palette",
            "@events"
    };
    public IRIOFixedUser data, palette;
    public IRIOFixedHash<Integer, IkaEvent> events;
    public final int defaultWidth, defaultHeight;

    public IkaMap(int w, int h) {
        super("IkachanMap", vtbl);
        defaultWidth = w;
        defaultHeight = h;
        initialize();
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@data"))
            return data = new IRIOFixedUser("Table", new RubyTable(3, defaultWidth, defaultHeight, 1, new int[1]).innerBytes);
        if (sym.equals("@palette"))
            return palette = new IRIOFixedUser("Table", new RubyTable(3, 256, 1, 4, new int[4]).innerBytes);
        if (sym.equals("@events"))
            return events = new IRIOFixedHash<Integer, IkaEvent>() {
                @Override
                public Integer convertIRIOtoKey(IRIO i) {
                    return (int) i.getFX();
                }

                @Override
                public IRIO convertKeyToIRIO(Integer i) {
                    return new IRIOFixnum(i);
                }

                @Override
                public IkaEvent newValue() {
                    return new IkaEvent();
                }
            };
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        if (sym.equals("@data"))
            return data;
        if (sym.equals("@palette"))
            return palette;
        if (sym.equals("@events"))
            return events;
        return null;
    }
}

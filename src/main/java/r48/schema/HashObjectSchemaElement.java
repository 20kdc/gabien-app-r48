/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;
import java.util.LinkedList;

public class HashObjectSchemaElement extends SchemaElement {

    public final LinkedList<RubyIO> allowedKeys;
    // disables setDefault
    public final boolean inner;

    public HashObjectSchemaElement(LinkedList<RubyIO> allowedKey, boolean hashObjectInner) {
        allowedKeys = allowedKey;
        inner = hashObjectInner;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return HiddenSchemaElement.makeHiddenElement();
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (SchemaElement.ensureType(target, '{', setDefault && (!inner))) {
            target.hashVal = new HashMap<IRIO, RubyIO>();
            path.changeOccurred(true);
        } else {
            LinkedList<IRIO> keys = new LinkedList<IRIO>();
            for (IRIO key : target.hashVal.keySet()) {
                boolean okay = false;
                for (RubyIO k2 : allowedKeys) {
                    if (IRIO.rubyEquals(key, k2)) {
                        okay = true;
                        break;
                    }
                }
                if (!okay)
                    keys.add(key);
            }
            for (IRIO k : keys)
                target.hashVal.remove(k);
            if (keys.size() > 0)
                path.changeOccurred(true);
        }
    }
}

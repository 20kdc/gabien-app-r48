/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.elements.UIEmpty;
import r48.R48;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * This was probably meant for JSON. Time of introduction unknown.
 */
public class HashObjectSchemaElement extends SchemaElement.Leaf {

    public final HashSet<DMKey> allowedKeys;
    // disables setDefault
    public final boolean inner;

    public HashObjectSchemaElement(R48 app, HashSet<DMKey> allowedKey, boolean hashObjectInner) {
        super(app);
        allowedKeys = allowedKey;
        inner = hashObjectInner;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return new UIEmpty();
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (checkType(target, '{', null, setDefault && (!inner))) {
            target.setHash();
            path.changeOccurred(true);
        } else {
            LinkedList<DMKey> keys = new LinkedList<DMKey>();
            for (DMKey key : target.getHashKeys())
                if (!allowedKeys.contains(key))
                    keys.add(key);
            for (DMKey k : keys)
                target.removeHashVal(k);
            if (keys.size() > 0)
                path.changeOccurred(true);
        }
    }
}

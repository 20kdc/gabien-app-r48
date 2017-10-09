package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;
import java.util.LinkedList;

public class HashObjectSchemaElement extends SchemaElement {

    public final LinkedList<RubyIO> allowedKeys;
    public HashObjectSchemaElement(LinkedList<RubyIO> allowedKey) {
        allowedKeys = allowedKey;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        UIPanel panel = new UIPanel();
        panel.setBounds(new Rect(0, 0, 0, 0));
        return panel;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, '{', setDefault)) {
            target.hashVal = new HashMap<RubyIO, RubyIO>();
            path.changeOccurred(true);
        } else {
            LinkedList<RubyIO> keys = new LinkedList<RubyIO>();
            for (RubyIO key : target.hashVal.keySet()) {
                boolean okay = false;
                for (RubyIO k2 : allowedKeys) {
                    if (RubyIO.rubyEquals(key, k2)) {
                        okay = true;
                        break;
                    }
                }
                if (!okay)
                    keys.add(key);
            }
            for (RubyIO k : keys)
                target.hashVal.remove(k);
            if (keys.size() > 0)
                path.changeOccurred(true);
        }
    }
}

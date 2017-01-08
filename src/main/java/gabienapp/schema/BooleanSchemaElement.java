/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.RubyIO;
import gabienapp.schema.util.SchemaPath;

/**
 * Created on 12/29/16.
 */
public class BooleanSchemaElement implements ISchemaElement {
    public boolean defaultVal = false;
    public BooleanSchemaElement(boolean defVal) {
        defaultVal = defVal;
    }
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        final UITextButton utb = new UITextButton(false, determineTruth(target) ? "True" : "False", null).togglable();
        utb.state = determineTruth(target);
        utb.OnClick = new Runnable() {
            @Override
            public void run() {
                modifyValueTruth(target, utb.state);
                path.changeOccurred(false);
                utb.Text = utb.state ? "True" : "False";
            }
        };
        return utb;
    }

    public boolean determineTruth(RubyIO rubyIO) {
        return rubyIO.type == 'T';
    }
    public boolean modifyValueTruth(RubyIO target, boolean truth) {
        int lastType = target.type;
        target.type = truth ? 'T' : 'F';
        return lastType != target.type;
    }
    public boolean truthInvalid(RubyIO target) {
        if (target.type == 'T')
            return false;
        if (target.type == 'F')
            return false;
        return true;
    }

    @Override
    public int maxHoldingHeight() {
        return 10;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        boolean modified = false;
        if (setDefault) {
            modified = modifyValueTruth(target, defaultVal);
        } else if (truthInvalid(target)) {
            modified = modifyValueTruth(target, defaultVal);
        }
        if (modified)
            path.changeOccurred(true);
    }

}

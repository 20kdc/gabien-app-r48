/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

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
        final UITextButton utb = new UITextButton(FontSizes.schemaButtonTextHeight, determineTruth(target) ? "True" : "False", null).togglable();
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
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
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

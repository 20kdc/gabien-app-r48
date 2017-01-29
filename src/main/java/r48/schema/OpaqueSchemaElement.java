/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.*;
import r48.FontSizes;
import r48.UITest;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

/**
 * Created on 12/28/16.
 */
public class OpaqueSchemaElement implements ISchemaElement {

    public String getMessage() {
        return "Can't edit:";
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, getMessage() + target, new Runnable() {
            @Override
            public void run() {
                launcher.launchOther(new UITest(target));
            }
        });
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // Not a clue, so re-initialize if all else fails.
        if ((target.type == 0) || setDefault) {
            target.setNull();
            path.changeOccurred(true);
        }
    }
}
